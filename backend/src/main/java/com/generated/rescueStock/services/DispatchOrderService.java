package com.generated.rescueStock.services;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;
import com.generated.rescueStock.constants.DispatchStatus;
import com.generated.rescueStock.constants.LogTemplates;
import com.generated.rescueStock.constants.ReservationStatus;
import com.generated.rescueStock.constructors.DispatchOrderDtoFactory;
import com.generated.rescueStock.middlewares.AuditLogMiddleware;
import com.generated.rescueStock.models.DispatchLine;
import com.generated.rescueStock.models.DispatchOrder;
import com.generated.rescueStock.models.DispatchReservation;
import com.generated.rescueStock.models.DispatchStatusEvent;
import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.SupplyItem;
import com.generated.rescueStock.repositories.DispatchEventRepository;
import com.generated.rescueStock.repositories.DispatchOrderRepository;
import com.generated.rescueStock.repositories.DispatchReservationRepository;
import com.generated.rescueStock.repositories.InventoryBatchRepository;
import com.generated.rescueStock.repositories.ShelterRepository;
import com.generated.rescueStock.repositories.SupplyItemRepository;
import com.generated.rescueStock.repositories.WarehouseRepository;
import com.generated.rescueStock.types.DispatchOrderPayload;
import com.generated.rescueStock.types.ServiceException;

// 调拨库存预占闭环：
// 审批通过 -> 按最早到期先出（同到期按批次号升序）从来源仓可用批次预占，不足则整单失败；
// 出库确认 -> 预占转实扣；驳回/取消 -> 释放全部预占。
// inventoryLock 保证“检查可用 + 扣减可用”原子化：同一批次并发审批只有一单能成功。
@Service
public class DispatchOrderService {
  private static final String QUALIFIED = "QUALIFIED";

  private final DispatchOrderRepository orderRepo;
  private final DispatchReservationRepository reservationRepo;
  private final InventoryBatchRepository batchRepo;
  private final DispatchEventRepository eventRepo;
  private final WarehouseRepository warehouseRepo;
  private final ShelterRepository shelterRepo;
  private final SupplyItemRepository supplyItemRepo;
  private final AuditLogMiddleware audit;
  private final ReentrantLock inventoryLock = new ReentrantLock(true);

  public DispatchOrderService(DispatchOrderRepository orderRepo, DispatchReservationRepository reservationRepo,
      InventoryBatchRepository batchRepo, DispatchEventRepository eventRepo, WarehouseRepository warehouseRepo,
      ShelterRepository shelterRepo, SupplyItemRepository supplyItemRepo, AuditLogMiddleware audit) {
    this.orderRepo = orderRepo;
    this.reservationRepo = reservationRepo;
    this.batchRepo = batchRepo;
    this.eventRepo = eventRepo;
    this.warehouseRepo = warehouseRepo;
    this.shelterRepo = shelterRepo;
    this.supplyItemRepo = supplyItemRepo;
    this.audit = audit;
  }

  private record Allocation(DispatchLine line, InventoryBatch batch, int take) {}

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> rows = new ArrayList<>();
    for (DispatchOrder order : orderRepo.findAll()) {
      rows.add(summarize(order));
    }
    return rows;
  }

  public List<Map<String, Object>> listByEvent(long eventId) {
    List<Map<String, Object>> rows = new ArrayList<>();
    for (DispatchOrder order : orderRepo.findByEventId(eventId)) {
      rows.add(summarize(order));
    }
    return rows;
  }

  public Map<String, Object> detail(long id) {
    DispatchOrder order = mustGet(id);
    List<Map<String, Object>> reservations = new ArrayList<>();
    for (DispatchReservation reservation : reservationRepo.findByOrderId(order.id)) {
      InventoryBatch batch = batchRepo.findById(reservation.batchId);
      reservations.add(DispatchOrderDtoFactory.reservationDto(reservation,
          batch == null ? null : batch.batchNo, batch == null ? null : batch.expireAt));
    }
    List<Map<String, Object>> history = new ArrayList<>();
    for (DispatchStatusEvent event : order.history) {
      history.add(DispatchOrderDtoFactory.historyDto(event));
    }
    return DispatchOrderDtoFactory.detail(summarize(order), reservations, history);
  }

  public Map<String, Object> create(DispatchOrderPayload payload) {
    if (payload == null || payload.lines() == null || payload.lines().isEmpty()) {
      throw ServiceException.validation("调拨单至少包含一条物资行");
    }
    if (eventRepo.findById(payload.eventId()) == null) {
      throw ServiceException.validation("关联事件不存在: " + payload.eventId());
    }
    if (warehouseRepo.findById(payload.sourceWarehouseId()) == null) {
      throw ServiceException.validation("来源仓库不存在: " + payload.sourceWarehouseId());
    }
    if (shelterRepo.findById(payload.shelterId()) == null) {
      throw ServiceException.validation("安置点不存在: " + payload.shelterId());
    }
    for (DispatchOrderPayload.Line line : payload.lines()) {
      if (line.quantity() <= 0) {
        throw ServiceException.validation("物资行数量必须大于 0");
      }
      if (supplyItemRepo.findById(line.supplyItemId()) == null) {
        throw ServiceException.validation("物资不存在: " + line.supplyItemId());
      }
    }
    String now = Instant.now().toString();
    String requestedBy = payload.requestedBy() == null || payload.requestedBy().isBlank() ? "system"
        : payload.requestedBy();
    DispatchOrder order = new DispatchOrder(orderRepo.nextOrderId(), payload.eventId(), payload.sourceWarehouseId(),
        payload.shelterId(), payload.priority() == null || payload.priority().isBlank() ? "MEDIUM" : payload.priority(),
        DispatchStatus.DRAFT.name(), requestedBy, null, null, now);
    for (DispatchOrderPayload.Line line : payload.lines()) {
      order.lines.add(new DispatchLine(orderRepo.nextLineId(), order.id, line.supplyItemId(), line.quantity()));
    }
    order.history.add(new DispatchStatusEvent(DispatchStatus.DRAFT.name(), requestedBy, now, "创建调拨单"));
    orderRepo.save(order);
    audit.record(requestedBy, LogTemplates.DISPATCH_CREATE, "DispatchOrder", order.id,
        "创建调拨单，共 " + order.lines.size() + " 条物资行");
    return detail(order.id);
  }

  public Map<String, Object> submit(long id, String operator) {
    inventoryLock.lock();
    try {
      DispatchOrder order = mustGet(id);
      requireStatus(order, List.of(DispatchStatus.DRAFT), "submit");
      transition(order, DispatchStatus.SUBMITTED, operator, "提交审批");
      audit.record(operator, LogTemplates.DISPATCH_SUBMIT, "DispatchOrder", order.id, "调拨单提交审批");
      return detail(order.id);
    } finally {
      inventoryLock.unlock();
    }
  }

  // 审批通过：FEFO 分配预占；任一物资行不足则整单失败，不占用任何批次。
  public Map<String, Object> approve(long id, String operator) {
    inventoryLock.lock();
    try {
      DispatchOrder order = mustGet(id);
      requireStatus(order, List.of(DispatchStatus.SUBMITTED), "approve");

      List<Allocation> allocations = new ArrayList<>();
      List<Map<String, Object>> shortages = new ArrayList<>();
      Map<Long, Integer> pending = new HashMap<>(); // 本单内已计划预占，避免多行重复计算可用量
      for (DispatchLine line : order.lines) {
        List<InventoryBatch> candidates = allocatableBatches(order.sourceWarehouseId, line.supplyItemId);
        int remaining = line.quantity;
        int lineAvailable = 0;
        for (InventoryBatch batch : candidates) {
          int allocatable = batch.available() - pending.getOrDefault(batch.id, 0);
          lineAvailable += Math.max(0, allocatable);
        }
        for (InventoryBatch batch : candidates) {
          if (remaining <= 0) {
            break;
          }
          int allocatable = batch.available() - pending.getOrDefault(batch.id, 0);
          if (allocatable <= 0) {
            continue;
          }
          int take = Math.min(allocatable, remaining);
          allocations.add(new Allocation(line, batch, take));
          pending.merge(batch.id, take, Integer::sum);
          remaining -= take;
        }
        if (remaining > 0) {
          shortages.add(DispatchOrderDtoFactory.shortageDto(line, itemName(line.supplyItemId), lineAvailable,
              remaining));
        }
      }
      if (!shortages.isEmpty()) {
        audit.record(operator, LogTemplates.DISPATCH_APPROVE_SHORTAGE, "DispatchOrder", order.id,
            "缺口 " + shortages.size() + " 条物资行，整单未占用任何批次");
        throw ServiceException.insufficientStock(shortages);
      }

      String now = Instant.now().toString();
      int reservedTotal = 0;
      for (Allocation allocation : allocations) {
        allocation.batch().reservedQuantity += allocation.take();
        batchRepo.save(allocation.batch());
        reservationRepo.save(new DispatchReservation(reservationRepo.nextId(), order.id, allocation.line().id,
            allocation.batch().id, allocation.take(), ReservationStatus.RESERVED.name(), now));
        reservedTotal += allocation.take();
      }
      transition(order, DispatchStatus.APPROVED, operator, "审批通过，预占 " + reservedTotal + " 件物资");
      audit.record(operator, LogTemplates.DISPATCH_APPROVED, "DispatchOrder", order.id,
          "预占 " + allocations.size() + " 个批次共 " + reservedTotal + " 件");
      return detail(order.id);
    } finally {
      inventoryLock.unlock();
    }
  }

  public Map<String, Object> reject(long id, String operator) {
    inventoryLock.lock();
    try {
      DispatchOrder order = mustGet(id);
      requireStatus(order, List.of(DispatchStatus.SUBMITTED, DispatchStatus.APPROVED), "reject");
      int released = releaseAll(order);
      transition(order, DispatchStatus.REJECTED, operator, "驳回调拨单，释放预占 " + released + " 件");
      audit.record(operator, LogTemplates.DISPATCH_REJECTED, "DispatchOrder", order.id, "释放预占 " + released + " 件");
      return detail(order.id);
    } finally {
      inventoryLock.unlock();
    }
  }

  public Map<String, Object> cancel(long id, String operator) {
    inventoryLock.lock();
    try {
      DispatchOrder order = mustGet(id);
      requireStatus(order,
          List.of(DispatchStatus.DRAFT, DispatchStatus.SUBMITTED, DispatchStatus.APPROVED), "cancel");
      int released = releaseAll(order);
      transition(order, DispatchStatus.CANCELLED, operator, "取消调拨单，释放预占 " + released + " 件");
      audit.record(operator, LogTemplates.DISPATCH_CANCELLED, "DispatchOrder", order.id, "释放预占 " + released + " 件");
      return detail(order.id);
    } finally {
      inventoryLock.unlock();
    }
  }

  // 出库确认：预占转实扣，批次账面库存与预占量同步扣减。
  public Map<String, Object> confirmOutbound(long id, String operator) {
    inventoryLock.lock();
    try {
      DispatchOrder order = mustGet(id);
      requireStatus(order, List.of(DispatchStatus.APPROVED), "dispatch");
      List<DispatchReservation> active = reservationRepo.findActiveByOrderId(order.id);
      int consumed = 0;
      for (DispatchReservation reservation : active) {
        InventoryBatch batch = batchRepo.findById(reservation.batchId);
        if (batch != null) {
          batch.quantity -= reservation.quantity;
          batch.reservedQuantity -= reservation.quantity;
          batchRepo.save(batch);
        }
        reservation.status = ReservationStatus.CONSUMED.name();
        reservationRepo.save(reservation);
        consumed += reservation.quantity;
      }
      order.dispatchedAt = Instant.now().toString();
      transition(order, DispatchStatus.DISPATCHED, operator, "出库确认，预占转实扣 " + consumed + " 件");
      audit.record(operator, LogTemplates.DISPATCH_OUTBOUND, "DispatchOrder", order.id, "实扣 " + consumed + " 件");
      return detail(order.id);
    } finally {
      inventoryLock.unlock();
    }
  }

  public Map<String, Object> receive(long id, String operator) {
    inventoryLock.lock();
    try {
      DispatchOrder order = mustGet(id);
      requireStatus(order, List.of(DispatchStatus.DISPATCHED), "receive");
      transition(order, DispatchStatus.RECEIVED, operator, "安置点签收完成");
      audit.record(operator, LogTemplates.DISPATCH_RECEIVED, "DispatchOrder", order.id, "调拨单签收完成");
      return detail(order.id);
    } finally {
      inventoryLock.unlock();
    }
  }

  // ---- 内部辅助 ----

  private DispatchOrder mustGet(long id) {
    DispatchOrder order = orderRepo.findById(id);
    if (order == null) {
      throw ServiceException.orderNotFound(id);
    }
    return order;
  }

  private void requireStatus(DispatchOrder order, List<DispatchStatus> allowed, String action) {
    for (DispatchStatus status : allowed) {
      if (status.name().equals(order.status)) {
        return;
      }
    }
    throw ServiceException.invalidStatus(order.status, action);
  }

  private void transition(DispatchOrder order, DispatchStatus next, String operator, String note) {
    order.status = next.name();
    String actor = operator == null || operator.isBlank() ? "system" : operator;
    order.history.add(new DispatchStatusEvent(next.name(), actor, Instant.now().toString(), note));
    orderRepo.save(order);
  }

  private int releaseAll(DispatchOrder order) {
    int released = 0;
    for (DispatchReservation reservation : reservationRepo.findActiveByOrderId(order.id)) {
      InventoryBatch batch = batchRepo.findById(reservation.batchId);
      if (batch != null) {
        batch.reservedQuantity = Math.max(0, batch.reservedQuantity - reservation.quantity);
        batchRepo.save(batch);
      }
      reservation.status = ReservationStatus.RELEASED.name();
      reservationRepo.save(reservation);
      released += reservation.quantity;
    }
    return released;
  }

  // 可分配批次：来源仓 + 合格 + 有可用量，按最早到期先出，同到期按批次号升序。
  private List<InventoryBatch> allocatableBatches(long warehouseId, long supplyItemId) {
    List<InventoryBatch> candidates = new ArrayList<>();
    for (InventoryBatch batch : batchRepo.findByWarehouseAndItem(warehouseId, supplyItemId)) {
      if (QUALIFIED.equals(batch.qualityStatus) && batch.available() > 0) {
        candidates.add(batch);
      }
    }
    candidates.sort(Comparator.comparing(this::expireDate)
        .thenComparing(batch -> batch.batchNo, Comparator.nullsLast(Comparator.naturalOrder())));
    return candidates;
  }

  private LocalDate expireDate(InventoryBatch batch) {
    try {
      return LocalDate.parse(batch.expireAt);
    } catch (Exception ex) {
      return LocalDate.MAX;
    }
  }

  private int availableOf(long warehouseId, long supplyItemId) {
    int total = 0;
    for (InventoryBatch batch : batchRepo.findByWarehouseAndItem(warehouseId, supplyItemId)) {
      if (QUALIFIED.equals(batch.qualityStatus)) {
        total += Math.max(0, batch.available());
      }
    }
    return total;
  }

  private Map<String, Object> summarize(DispatchOrder order) {
    List<Map<String, Object>> lines = new ArrayList<>();
    for (DispatchLine line : order.lines) {
      int reserved = reservationRepo.sumByLineAndStatus(line.id, ReservationStatus.RESERVED);
      int consumed = reservationRepo.sumByLineAndStatus(line.id, ReservationStatus.CONSUMED);
      int available = availableOf(order.sourceWarehouseId, line.supplyItemId);
      int shortage;
      if (DispatchStatus.APPROVED.name().equals(order.status) || DispatchStatus.DISPATCHED.name().equals(order.status)
          || DispatchStatus.RECEIVED.name().equals(order.status)) {
        shortage = Math.max(0, line.quantity - reserved - consumed);
      } else if (DispatchStatus.REJECTED.name().equals(order.status)
          || DispatchStatus.CANCELLED.name().equals(order.status)) {
        shortage = 0;
      } else {
        // DRAFT / SUBMITTED：按当前可用量预演缺口，与审批失败返回的缺口口径一致。
        shortage = Math.max(0, line.quantity - available);
      }
      SupplyItem item = supplyItemRepo.findById(line.supplyItemId);
      lines.add(DispatchOrderDtoFactory.lineDto(line, item == null ? null : item.name,
          item == null ? null : item.unit, reserved, consumed, available, shortage));
    }
    return DispatchOrderDtoFactory.summary(order, eventName(order.eventId), warehouseName(order.sourceWarehouseId),
        shelterName(order.shelterId), lines);
  }

  private String eventName(long id) {
    return eventRepo.findById(id) == null ? null : eventRepo.findById(id).name;
  }

  private String warehouseName(long id) {
    return warehouseRepo.findById(id) == null ? null : warehouseRepo.findById(id).name;
  }

  private String shelterName(long id) {
    return shelterRepo.findById(id) == null ? null : shelterRepo.findById(id).name;
  }

  private String itemName(long id) {
    return supplyItemRepo.findById(id) == null ? null : supplyItemRepo.findById(id).name;
  }
}
