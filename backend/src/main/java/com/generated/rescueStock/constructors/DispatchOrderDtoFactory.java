package com.generated.rescueStock.constructors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.generated.rescueStock.models.DispatchLine;
import com.generated.rescueStock.models.DispatchOrder;
import com.generated.rescueStock.models.DispatchReservation;
import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.Shelter;
import com.generated.rescueStock.models.SupplyItem;
import com.generated.rescueStock.models.Warehouse;

/**
 * 调拨单响应 DTO 构造器：列表项、详情（含行、预占明细、批次信息）统一在这里组装，
 * service/controller 不得散写默认结构。
 */
public final class DispatchOrderDtoFactory {

  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private DispatchOrderDtoFactory() {}

  /** 列表项：聚合申请量 / 预占量 / 缺口 */
  public static Map<String, Object> listItem(DispatchOrder order, List<DispatchLine> lines) {
    int requested = lines.stream().mapToInt(l -> l.quantity).sum();
    int reserved = lines.stream().mapToInt(l -> l.reservedQuantity).sum();
    int shortage = lines.stream().mapToInt(l -> l.shortageQuantity).sum();
    Map<String, Object> dto = base(order);
    dto.put("requested_quantity", requested);
    dto.put("reserved_quantity", reserved);
    dto.put("shortage_quantity", shortage);
    return dto;
  }

  /** 详情：单据 + 行（含物资名） + 预占明细（含批次号/到期日/可用量快照） */
  public static Map<String, Object> detail(
      DispatchOrder order,
      Warehouse warehouse,
      Shelter shelter,
      List<DispatchLine> lines,
      Map<Long, SupplyItem> itemMap,
      List<DispatchReservation> reservations,
      Map<Long, InventoryBatch> batchMap) {
    Map<String, Object> dto = base(order);

    dto.put("warehouse", warehouse == null ? null : WarehouseDtoFactory.summary(warehouse));
    dto.put("shelter", shelter == null ? null : ShelterDtoFactory.summary(shelter));

    List<Map<String, Object>> lineDtos = new ArrayList<>();
    for (DispatchLine line : lines) {
      Map<String, Object> lineDto = new LinkedHashMap<>();
      lineDto.put("id", line.id);
      lineDto.put("supply_item_id", line.supplyItemId);
      SupplyItem item = itemMap.get(line.supplyItemId);
      lineDto.put("supply_item_name", item == null ? null : item.name);
      lineDto.put("sku_code", item == null ? null : item.skuCode);
      lineDto.put("unit", item == null ? null : item.unit);
      lineDto.put("quantity", line.quantity);
      lineDto.put("reserved_quantity", line.reservedQuantity);
      lineDto.put("shortage_quantity", line.shortageQuantity);
      lineDtos.add(lineDto);
    }
    dto.put("lines", lineDtos);

    List<Map<String, Object>> reservationDtos = new ArrayList<>();
    for (DispatchReservation r : reservations) {
      Map<String, Object> rDto = new LinkedHashMap<>();
      rDto.put("id", r.id);
      rDto.put("line_id", r.lineId);
      rDto.put("batch_id", r.batchId);
      rDto.put("quantity", r.quantity);
      rDto.put("status", r.status);
      rDto.put("created_at", fmt(r.createdAt));
      rDto.put("consumed_at", fmt(r.consumedAt));
      rDto.put("released_at", fmt(r.releasedAt));
      InventoryBatch batch = batchMap.get(r.batchId);
      if (batch != null) {
        rDto.put("batch_no", batch.batchNo);
        rDto.put("expire_at", fmt(batch.expireAt));
        rDto.put("batch_quantity", batch.quantity);
        rDto.put("batch_reserved_quantity", batch.reservedQuantity);
        rDto.put("batch_available_quantity", batch.quantity - batch.reservedQuantity);
      }
      reservationDtos.add(rDto);
    }
    dto.put("reservations", reservationDtos);

    int requested = lines.stream().mapToInt(l -> l.quantity).sum();
    int reserved = lines.stream().mapToInt(l -> l.reservedQuantity).sum();
    int shortage = lines.stream().mapToInt(l -> l.shortageQuantity).sum();
    dto.put("requested_quantity", requested);
    dto.put("reserved_quantity", reserved);
    dto.put("shortage_quantity", shortage);
    return dto;
  }

  private static Map<String, Object> base(DispatchOrder o) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", o.id);
    dto.put("event_id", o.eventId);
    dto.put("source_warehouse_id", o.sourceWarehouseId);
    dto.put("shelter_id", o.shelterId);
    dto.put("priority", o.priority);
    dto.put("status", o.status);
    dto.put("requested_by", o.requestedBy);
    dto.put("approved_by", o.approvedBy);
    dto.put("reject_reason", o.rejectReason);
    dto.put("cancel_reason", o.cancelReason);
    dto.put("created_at", fmt(o.createdAt));
    dto.put("submitted_at", fmt(o.submittedAt));
    dto.put("approved_at", fmt(o.approvedAt));
    dto.put("dispatched_at", fmt(o.dispatchedAt));
    dto.put("received_at", fmt(o.receivedAt));
    dto.put("rejected_at", fmt(o.rejectedAt));
    dto.put("cancelled_at", fmt(o.cancelledAt));
    return dto;
  }

  private static String fmt(LocalDateTime time) {
    return time == null ? null : FMT.format(time);
  }
}
