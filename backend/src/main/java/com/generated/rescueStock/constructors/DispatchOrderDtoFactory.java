package com.generated.rescueStock.constructors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.generated.rescueStock.models.DispatchLine;
import com.generated.rescueStock.models.DispatchOrder;
import com.generated.rescueStock.models.DispatchReservation;
import com.generated.rescueStock.models.DispatchStatusEvent;

// 调拨单响应 DTO 构造器：统一 snake_case 输出，页面/事件详情共用。
public final class DispatchOrderDtoFactory {

  private DispatchOrderDtoFactory() {}

  public static Map<String, Object> lineDto(DispatchLine line, String itemName, String unit, int reserved,
      int consumed, int available, int shortage) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", line.id);
    dto.put("dispatch_order_id", line.dispatchOrderId);
    dto.put("supply_item_id", line.supplyItemId);
    dto.put("supply_item_name", itemName);
    dto.put("unit", unit);
    dto.put("requested_quantity", line.quantity);
    dto.put("reserved_quantity", reserved);
    dto.put("consumed_quantity", consumed);
    dto.put("available_quantity", available);
    dto.put("shortage_quantity", shortage);
    return dto;
  }

  public static Map<String, Object> summary(DispatchOrder order, String eventName, String warehouseName,
      String shelterName, List<Map<String, Object>> lines) {
    int totalRequested = 0;
    int totalReserved = 0;
    int totalAvailable = 0;
    int totalShortage = 0;
    for (Map<String, Object> line : lines) {
      totalRequested += (int) line.get("requested_quantity");
      totalReserved += (int) line.get("reserved_quantity");
      totalAvailable += (int) line.get("available_quantity");
      totalShortage += (int) line.get("shortage_quantity");
    }
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", order.id);
    dto.put("event_id", order.eventId);
    dto.put("event_name", eventName);
    dto.put("source_warehouse_id", order.sourceWarehouseId);
    dto.put("source_warehouse_name", warehouseName);
    dto.put("shelter_id", order.shelterId);
    dto.put("shelter_name", shelterName);
    dto.put("priority", order.priority);
    dto.put("status", order.status);
    dto.put("requested_by", order.requestedBy);
    dto.put("approved_by", order.approvedBy);
    dto.put("dispatched_at", order.dispatchedAt);
    dto.put("created_at", order.createdAt);
    dto.put("lines", lines);
    dto.put("total_requested", totalRequested);
    dto.put("total_reserved", totalReserved);
    dto.put("total_available", totalAvailable);
    dto.put("total_shortage", totalShortage);
    return dto;
  }

  public static Map<String, Object> detail(Map<String, Object> summary, List<Map<String, Object>> reservations,
      List<Map<String, Object>> history) {
    Map<String, Object> dto = new LinkedHashMap<>(summary);
    dto.put("reservations", reservations);
    dto.put("history", history);
    return dto;
  }

  public static Map<String, Object> reservationDto(DispatchReservation reservation, String batchNo,
      String expireAt) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", reservation.id);
    dto.put("dispatch_order_id", reservation.dispatchOrderId);
    dto.put("dispatch_line_id", reservation.dispatchLineId);
    dto.put("batch_id", reservation.batchId);
    dto.put("batch_no", batchNo);
    dto.put("expire_at", expireAt);
    dto.put("quantity", reservation.quantity);
    dto.put("status", reservation.status);
    dto.put("created_at", reservation.createdAt);
    return dto;
  }

  public static Map<String, Object> historyDto(DispatchStatusEvent event) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("status", event.status);
    dto.put("actor", event.actor);
    dto.put("at", event.at);
    dto.put("note", event.note);
    return dto;
  }

  public static Map<String, Object> shortageDto(DispatchLine line, String itemName, int available, int shortage) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("supply_item_id", line.supplyItemId);
    dto.put("supply_item_name", itemName);
    dto.put("requested_quantity", line.quantity);
    dto.put("available_quantity", available);
    dto.put("shortage_quantity", shortage);
    return dto;
  }
}
