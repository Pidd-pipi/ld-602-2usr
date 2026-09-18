package com.generated.rescueStock.services;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.generated.rescueStock.constants.DispatchStatus;
import com.generated.rescueStock.constants.ErrorCodes;
import com.generated.rescueStock.constants.ErrorMessages;
import com.generated.rescueStock.constructors.DispatchOrderDtoFactory;
import com.generated.rescueStock.exceptions.ApiException;
import com.generated.rescueStock.middlewares.AuditLogMiddleware;
import com.generated.rescueStock.models.DispatchLine;
import com.generated.rescueStock.models.DispatchOrder;
import com.generated.rescueStock.models.DispatchReservation;
import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.InventoryTransaction;
import com.generated.rescueStock.models.Shelter;
import com.generated.rescueStock.models.SupplyItem;
import com.generated.rescueStock.models.Warehouse;
import com.generated.rescueStock.constants.ReservationStatus;
import com.generated.rescueStock.constants.TransactionType;
import com.generated.rescueStock.repositories.DispatchLineRepository;
import com.generated.rescueStock.repositories.DispatchOrderRepository;
import com.generated.rescueStock.repositories.DispatchReservationRepository;
import com.generated.rescueStock.repositories.DisasterEventRepository;
import com.generated.rescueStock.repositories.InventoryBatchRepository;
import com.generated.rescueStock.repositories.InventoryTransactionRepository;
import com.generated.rescueStock.repositories.ShelterRepository;
import com.generated.rescueStock.repositories.SupplyItemRepository;
import com.generated.rescueStock.repositories.WarehouseRepository;
import com.generated.rescueStock.types.ApprovePayload;
import com.generated.rescueStock.types.CancelPayload;
import com.generated.rescueStock.types.DispatchLinePayload;
import com.generated.rescueStock.types.DispatchOrderPayload;
import com.generated.rescueStock.types.OutboundPayload;
import com.generated.rescueStock.types.RejectPayload;

/**
 * 调拨库存预占闭环：
 * 审批通过 -> FEFO 分配并预占（不足整单失败、不占用任何批次）
 * 出库确认 -> 预占转实扣
 * 驳回/取消 -> 全部释放
 * 同一批次并发审批 -> 行锁保证仅一单成功
 */
@Service
public class DispatchOrderService {

  private final DispatchOrderRepository orderRepo;
  private final DispatchLineRepository lineRepo;
  private final DispatchReservationRepository reservationRepo;
  private final InventoryBatchRepository batchRepo;
  private final InventoryTransactionRepository txnRepo;
  private final WarehouseRepository warehouseRepo;
  private final ShelterRepository shelterRepo;
  private final SupplyItemRepository supplyItemRepo;
  private final DisasterEventRepository eventRepo;
  private final BatchAllocationService allocationService;
  private final AuditLogMiddleware auditLog;

  public DispatchOrderService(DispatchOrderRepository orderRepo,
                              DispatchLineRepository lineRepo,
                              DispatchReservationRepository reservationRepo,
                              InventoryBatchRepository batchRepo,
                              InventoryTransactionRepository txnRepo,
                              WarehouseRepository warehouseRepo,
                              ShelterRepository shelterRepo,
                              SupplyItemRepository supplyItemRepo,
                              DisasterEventRepository eventRepo,
                              BatchAllocationService allocationService,
                              AuditLogMiddleware auditLog) {
    this.orderRepo = orderRepo;
    this.lineRepo = lineRepo;
    this.reservationRepo = reservationRepo;
    this.batchRepo = batchRepo;
    this.txnRepo = txnRepo;
    this.warehouseRepo = warehouseRepo;
    this.shelterRepo = shelterRepo;
    this.supplyItemRepo = supplyItemRepo;
    this.eventRepo = eventRepo;
    this.allocationService = allocationService;
    this.auditLog = auditLog;
  }

  // ---------------- 查询（刷新后回读） ----------------

  public List<Map<String, Object>> list() {
    List<DispatchOrder> orders = orderRepo.findAll();
    List<Long> orderIds = orders.stream().map(o -> o.id).toList();
    Map<Long, List<DispatchLine>> lineMap = groupLines(lineRepo.findByOrders(orderIds));
    List<Map<String, Object>> result = new ArrayList<>();
    for (DispatchOrder order : orders) {
      result.add(DispatchOrderDtoFactory.listItem(order, lineMap.getOrDefault(order.id, List.of())));
    }
    return result;
  }

  public Map<String, Object> detail(Long id) {
    DispatchOrder order = mustFind(id);
    Warehouse warehouse = warehouseRepo.findById(order.sourceWarehouseId);
    Shelter shelter = shelterRepo.findById(order.shelterId);
    List<DispatchLine> lines = lineRepo.findByOrder(id);
    Map<Long, SupplyItem> itemMap = loadItems(lines);
    List<DispatchReservation> reservations = reservationRepo.findByOrder(id);
    Map<Long, InventoryBatch> batchMap = loadBatches(reservations);
    return DispatchOrderDtoFactory.detail(order, warehouse, shelter, lines, itemMap, reservations, batchMap);
  }

  // ---------------- 创建 ----------------

  @Transactional
  public Map<String, Object> create(DispatchOrderPayload payload) {
    validateCreate(payload);
    DispatchOrder order = new DispatchOrder();
    order.eventId = payload.eventId();
    order.sourceWarehouseId = payload.sourceWarehouseId();
    order.shelterId = payload.shelterId();
    order.priority = payload.priority() == null || payload.priority().isBlank() ? "NORMAL" : payload.priority();
    order.status = DispatchStatus.SUBMITTED.name();
    order.requestedBy = payload.requestedBy() == null || payload.requestedBy().isBlank()
        ? "street-admin" : payload.requestedBy();
    Long orderId = orderRepo.insert(order);
    order.id = orderId;

    for (DispatchLinePayload linePayload : payload.lines()) {
      if (linePayload.supplyItemId() == null || linePayload.quantity() == null
          || linePayload.quantity() <= 0) {
        throw error(ErrorCodes.VALIDATION_FAILED,
            MessageFormat.format(ErrorMessages.VALIDATION_FAILED, "调拨行物资或数量非法"),
            HttpStatus.BAD_REQUEST);
      }
      DispatchLine line = new DispatchLine();
      line.orderId = orderId;
      line.supplyItemId = linePayload.supplyItemId();
      line.quantity = linePayload.quantity();
      line.reservedQuantity = 0;
      line.shortageQuantity = 0;
      lineRepo.insert(line);
    }
    auditLog.dispatchCreate(order.requestedBy, orderId, order.sourceWarehouseId, payload.lines().size());
    return detail(orderId);
  }

  // ---------------- 审批通过：FEFO 预占 ----------------

  @Transactional
  public Map<String, Object> approve(Long id, ApprovePayload payload) {
    String actor = payload == null || payload.approvedBy() == null || payload.approvedBy().isBlank()
        ? "approver" : payload.approvedBy();

    // 1. 锁定调拨单行：同一单的并发审批在此排队后由状态条件更新挡住
    DispatchOrder order = orderRepo.lockByIdForUpdate(id);
    if (order == null) {
      throw notFound(id);
    }
    if (!DispatchStatus.SUBMITTED.name().equals(order.status)) {
      throw conflict(order, "审批通过");
    }

    List<DispatchLine> lines = lineRepo.findByOrder(id);
    if (lines.isEmpty()) {
      throw error(ErrorCodes.VALIDATION_FAILED,
          MessageFormat.format(ErrorMessages.VALIDATION_FAILED, "调拨单没有明细行"),
          HttpStatus.BAD_REQUEST);
    }

    // 2. 在本事务内逐行锁定来源仓库的可用批次（FOR UPDATE），先做整单可行性计算
    Map<Long, BatchAllocationService.AllocationPlan> planByLine = new LinkedHashMap<>();
    List<Map<String, Object>> shortages = new ArrayList<>();
    boolean fulfilled = true;

    for (DispatchLine line : lines) {
      List<InventoryBatch> locked =
          batchRepo.lockAvailableBatchesForUpdate(order.sourceWarehouseId, line.supplyItemId);
      BatchAllocationService.AllocationPlan plan = allocationService.plan(locked, line.quantity);
      planByLine.put(line.id, plan);
      if (!plan.isFulfilled()) {
        fulfilled = false;
        SupplyItem item = supplyItemRepo.findById(line.supplyItemId);
        String itemName = item == null ? String.valueOf(line.supplyItemId) : item.name;
        int available = locked.stream().mapToInt(b -> b.quantity - b.reservedQuantity).sum();
        Map<String, Object> shortageRow = new LinkedHashMap<>();
        shortageRow.put("line_id", line.id);
        shortageRow.put("supply_item_id", line.supplyItemId);
        shortageRow.put("supply_item_name", itemName);
        shortageRow.put("requested_quantity", line.quantity);
        shortageRow.put("available_quantity", available);
        shortageRow.put("shortage_quantity", plan.getShortage());
        shortages.add(shortageRow);
        auditLog.dispatchShortage(actor, id, itemName, plan.getShortage());
      }
    }

    // 3. 任意一行不足：整单失败。此前只有 SELECT 锁，没有任何批次写入，回滚后不占用任何批次
    if (!fulfilled) {
      int totalShortage = shortages.stream()
          .mapToInt(s -> (Integer) s.get("shortage_quantity")).sum();
      throw new ApiException(
          ErrorCodes.RESERVATION_SHORTAGE,
          MessageFormat.format(ErrorMessages.RESERVATION_SHORTAGE, totalShortage),
          HttpStatus.CONFLICT.value(),
          shortages);
    }

    // 4. 全部满足：逐批次落预占（与库存流水双写），回填行预占量
    int batchCount = 0;
    int reservedTotal = 0;
    for (DispatchLine line : lines) {
      BatchAllocationService.AllocationPlan plan = planByLine.get(line.id);
      int lineReserved = 0;
      for (BatchAllocationService.BatchAllocation alloc : plan.getAllocations()) {
        InventoryBatch batch = alloc.batch();
        int updated = batchRepo.addReserved(batch.id, alloc.quantity(), batch.version);
        if (updated == 0) {
          // 行锁保护下仍被外部改动：按并发审批冲突处理
          throw error(ErrorCodes.CONCURRENT_APPROVAL, ErrorMessages.CONCURRENT_APPROVAL,
              HttpStatus.CONFLICT);
        }
        reservationRepo.insert(id, line.id, batch.id, alloc.quantity());
        recordTxn(batch, TransactionType.RESERVE, alloc.quantity(), order.id, actor);
        lineReserved += alloc.quantity();
        reservedTotal += alloc.quantity();
        batchCount++;
      }
      lineRepo.updateReservation(line.id, lineReserved, 0);
    }

    // 5. 状态 SUBMITTED -> APPROVED（条件更新，并发审批仅一单成功）
    order.status = DispatchStatus.APPROVED.name();
    order.approvedBy = actor;
    order.approvedAt = java.time.LocalDateTime.now();
    int changed = orderRepo.compareAndUpdateStatus(order, DispatchStatus.SUBMITTED.name());
    if (changed == 0) {
      throw error(ErrorCodes.CONCURRENT_APPROVAL, ErrorMessages.CONCURRENT_APPROVAL,
          HttpStatus.CONFLICT);
    }

    auditLog.dispatchApprove(actor, id, batchCount, reservedTotal);
    return detail(id);
  }

  // ---------------- 出库确认：预占转实扣 ----------------

  @Transactional
  public Map<String, Object> confirmOutbound(Long id, OutboundPayload payload) {
    String actor = payload == null || payload.operator() == null || payload.operator().isBlank()
        ? "warehouse-keeper" : payload.operator();

    DispatchOrder order = orderRepo.lockByIdForUpdate(id);
    if (order == null) {
      throw notFound(id);
    }
    if (!DispatchStatus.APPROVED.name().equals(order.status)) {
      if (DispatchStatus.SUBMITTED.name().equals(order.status)) {
        throw error(ErrorCodes.RESERVATION_NOT_READY,
            MessageFormat.format(ErrorMessages.RESERVATION_NOT_READY, id),
            HttpStatus.CONFLICT);
      }
      throw conflict(order, "出库确认");
    }

    List<DispatchReservation> active = reservationRepo.findActiveByOrderForUpdate(id);
    if (active.isEmpty()) {
      throw error(ErrorCodes.RESERVATION_NOT_READY,
          MessageFormat.format(ErrorMessages.RESERVATION_NOT_READY, id),
          HttpStatus.CONFLICT);
    }

    int batchCount = 0;
    int consumedTotal = 0;
    for (DispatchReservation r : active) {
      InventoryBatch batch = batchRepo.findById(r.batchId);
      if (batch == null || batchRepo.consume(r.batchId, r.quantity) == 0) {
        throw error(ErrorCodes.CONCURRENT_APPROVAL, ErrorMessages.CONCURRENT_APPROVAL,
            HttpStatus.CONFLICT);
      }
      recordTxn(batch, TransactionType.CONSUME, r.quantity, order.id, actor);
      batchCount++;
      consumedTotal += r.quantity;
    }
    reservationRepo.markConsumed(id);

    order.status = DispatchStatus.DISPATCHED.name();
    order.dispatchedAt = java.time.LocalDateTime.now();
    if (orderRepo.compareAndUpdateStatus(order, DispatchStatus.APPROVED.name()) == 0) {
      throw conflict(order, "出库确认");
    }

    auditLog.dispatchOutbound(actor, id, batchCount, consumedTotal);
    return detail(id);
  }

  // ---------------- 驳回：释放全部预占 ----------------

  @Transactional
  public Map<String, Object> reject(Long id, RejectPayload payload) {
    String actor = payload == null || payload.approvedBy() == null || payload.approvedBy().isBlank()
        ? "approver" : payload.approvedBy();
    String reason = payload == null || payload.reason() == null ? "" : payload.reason();

    DispatchOrder order = orderRepo.lockByIdForUpdate(id);
    if (order == null) {
      throw notFound(id);
    }
    if (!DispatchStatus.SUBMITTED.name().equals(order.status)
        && !DispatchStatus.APPROVED.name().equals(order.status)) {
      throw conflict(order, "驳回");
    }
    String fromStatus = order.status;

    int released = releaseAllReservations(order, actor);

    order.status = DispatchStatus.REJECTED.name();
    order.approvedBy = actor;
    order.rejectedAt = java.time.LocalDateTime.now();
    order.rejectReason = reason;
    if (orderRepo.compareAndUpdateStatus(order, fromStatus) == 0) {
      throw conflict(order, "驳回");
    }

    auditLog.dispatchReject(actor, id, reason, released);
    return detail(id);
  }

  // ---------------- 取消：释放全部预占 ----------------

  @Transactional
  public Map<String, Object> cancel(Long id, CancelPayload payload) {
    String actor = payload == null || payload.operator() == null || payload.operator().isBlank()
        ? "street-admin" : payload.operator();
    String reason = payload == null || payload.reason() == null ? "" : payload.reason();

    DispatchOrder order = orderRepo.lockByIdForUpdate(id);
    if (order == null) {
      throw notFound(id);
    }
    if (!DispatchStatus.SUBMITTED.name().equals(order.status)
        && !DispatchStatus.APPROVED.name().equals(order.status)) {
      throw conflict(order, "取消");
    }
    String fromStatus = order.status;

    int released = releaseAllReservations(order, actor);

    order.status = DispatchStatus.CANCELLED.name();
    order.cancelledAt = java.time.LocalDateTime.now();
    order.cancelReason = reason;
    if (orderRepo.compareAndUpdateStatus(order, fromStatus) == 0) {
      throw conflict(order, "取消");
    }

    auditLog.dispatchCancel(actor, id, reason, released);
    return detail(id);
  }

  // ---------------- 签收（仅状态流转，不再动库存） ----------------

  @Transactional
  public Map<String, Object> receive(Long id, OutboundPayload payload) {
    String actor = payload == null || payload.operator() == null || payload.operator().isBlank()
        ? "shelter-staff" : payload.operator();
    DispatchOrder order = mustFind(id);
    if (!DispatchStatus.DISPATCHED.name().equals(order.status)) {
      throw conflict(order, "签收");
    }
    order.status = DispatchStatus.RECEIVED.name();
    order.receivedAt = java.time.LocalDateTime.now();
    if (orderRepo.compareAndUpdateStatus(order, DispatchStatus.DISPATCHED.name()) == 0) {
      throw conflict(order, "签收");
    }
    auditLog.dispatchReceive(actor, id);
    return detail(id);
  }

  // ---------------- 内部方法 ----------------

  /** 释放一张单上所有 RESERVED 预占：回补批次预占量 + RELEASE 流水 + 明细置 RELEASED + 行清零 */
  private int releaseAllReservations(DispatchOrder order, String actor) {
    List<DispatchReservation> active = reservationRepo.findActiveByOrderForUpdate(order.id);
    int releasedTotal = 0;
    for (DispatchReservation r : active) {
      InventoryBatch batch = batchRepo.findById(r.batchId);
      if (batch == null || batchRepo.releaseReserved(r.batchId, r.quantity) == 0) {
        throw error(ErrorCodes.CONCURRENT_APPROVAL, ErrorMessages.CONCURRENT_APPROVAL,
            HttpStatus.CONFLICT);
      }
      recordTxn(batch, TransactionType.RELEASE, r.quantity, order.id, actor);
      releasedTotal += r.quantity;
    }
    reservationRepo.markReleased(order.id);
    lineRepo.clearReservation(order.id);
    return releasedTotal;
  }

  private void recordTxn(InventoryBatch batch, TransactionType type, int quantity, Long orderId, String actor) {
    InventoryTransaction txn = new InventoryTransaction();
    txn.batchId = batch.id;
    txn.orderId = orderId;
    txn.supplyItemId = batch.supplyItemId;
    txn.warehouseId = batch.warehouseId;
    txn.changeType = type.name();
    txn.changeQuantity = quantity;
    InventoryBatch fresh = batchRepo.findById(batch.id);
    txn.balanceAfter = fresh.quantity;
    txn.reservedAfter = fresh.reservedQuantity;
    txn.actor = actor;
    txnRepo.record(txn);
  }

  private void validateCreate(DispatchOrderPayload payload) {
    if (payload == null) {
      throw error(ErrorCodes.VALIDATION_FAILED,
          MessageFormat.format(ErrorMessages.VALIDATION_FAILED, "请求体为空"), HttpStatus.BAD_REQUEST);
    }
    if (payload.sourceWarehouseId() == null
        || warehouseRepo.findById(payload.sourceWarehouseId()) == null) {
      throw error(ErrorCodes.VALIDATION_FAILED,
          MessageFormat.format(ErrorMessages.VALIDATION_FAILED, "来源仓库不存在"),
          HttpStatus.BAD_REQUEST);
    }
    if (payload.shelterId() == null || shelterRepo.findById(payload.shelterId()) == null) {
      throw error(ErrorCodes.VALIDATION_FAILED,
          MessageFormat.format(ErrorMessages.VALIDATION_FAILED, "目标避难点不存在"),
          HttpStatus.BAD_REQUEST);
    }
    if (payload.eventId() != null && eventRepo.findById(payload.eventId()) == null) {
      throw error(ErrorCodes.VALIDATION_FAILED,
          MessageFormat.format(ErrorMessages.VALIDATION_FAILED, "关联灾害事件不存在"),
          HttpStatus.BAD_REQUEST);
    }
    if (payload.lines() == null || payload.lines().isEmpty()) {
      throw error(ErrorCodes.VALIDATION_FAILED,
          MessageFormat.format(ErrorMessages.VALIDATION_FAILED, "至少一行调拨明细"),
          HttpStatus.BAD_REQUEST);
    }
  }

  private DispatchOrder mustFind(Long id) {
    DispatchOrder order = orderRepo.findById(id);
    if (order == null) {
      throw notFound(id);
    }
    return order;
  }

  private ApiException notFound(Long id) {
    return error(ErrorCodes.DISPATCH_NOT_FOUND,
        MessageFormat.format(ErrorMessages.DISPATCH_NOT_FOUND, id), HttpStatus.NOT_FOUND);
  }

  private ApiException conflict(DispatchOrder order, String action) {
    return error(ErrorCodes.DISPATCH_STATUS_CONFLICT,
        MessageFormat.format(ErrorMessages.DISPATCH_STATUS_CONFLICT, order.id, order.status, action),
        HttpStatus.CONFLICT);
  }

  private ApiException error(String code, String message, HttpStatus status) {
    return new ApiException(code, message, status.value());
  }

  private Map<Long, List<DispatchLine>> groupLines(List<DispatchLine> all) {
    Map<Long, List<DispatchLine>> map = new HashMap<>();
    for (DispatchLine line : all) {
      map.computeIfAbsent(line.orderId, k -> new ArrayList<>()).add(line);
    }
    return map;
  }

  private Map<Long, SupplyItem> loadItems(List<DispatchLine> lines) {
    Map<Long, SupplyItem> map = new HashMap<>();
    for (DispatchLine line : lines) {
      map.computeIfAbsent(line.supplyItemId, supplyItemRepo::findById);
    }
    return map;
  }

  private Map<Long, InventoryBatch> loadBatches(List<DispatchReservation> reservations) {
    Map<Long, InventoryBatch> map = new HashMap<>();
    for (DispatchReservation r : reservations) {
      map.computeIfAbsent(r.batchId, batchRepo::findById);
    }
    return map;
  }
}
