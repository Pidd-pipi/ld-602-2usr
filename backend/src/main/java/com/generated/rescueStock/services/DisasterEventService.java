package com.generated.rescueStock.services;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.generated.rescueStock.constructors.DispatchOrderDtoFactory;
import com.generated.rescueStock.models.DispatchLine;
import com.generated.rescueStock.models.DispatchOrder;
import com.generated.rescueStock.models.DisasterEvent;
import com.generated.rescueStock.repositories.DispatchLineRepository;
import com.generated.rescueStock.repositories.DispatchOrderRepository;
import com.generated.rescueStock.repositories.DisasterEventRepository;

/** 灾害事件：事件详情同步聚合关联调拨单的预占/缺口 */
@Service
public class DisasterEventService {

  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final DisasterEventRepository eventRepo;
  private final DispatchOrderRepository orderRepo;
  private final DispatchLineRepository lineRepo;

  public DisasterEventService(DisasterEventRepository eventRepo,
                              DispatchOrderRepository orderRepo,
                              DispatchLineRepository lineRepo) {
    this.eventRepo = eventRepo;
    this.orderRepo = orderRepo;
    this.lineRepo = lineRepo;
  }

  public List<Map<String, Object>> list() {
    return eventRepo.findAll().stream().map(this::summary).toList();
  }

  private Map<String, Object> summary(DisasterEvent e) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", e.id);
    dto.put("name", e.name);
    dto.put("event_type", e.eventType);
    dto.put("district", e.district);
    dto.put("level", e.level);
    dto.put("status", e.status);
    dto.put("description", e.description);
    dto.put("occurred_at", e.occurredAt == null ? null : FMT.format(e.occurredAt));
    return dto;
  }

  public Map<String, Object> detail(Long eventId) {
    DisasterEvent event = eventRepo.findById(eventId);
    List<DispatchOrder> orders = orderRepo.findByEvent(eventId);
    List<Long> orderIds = orders.stream().map(o -> o.id).toList();

    java.util.HashMap<Long, List<DispatchLine>> lineMap = new java.util.HashMap<>();
    for (DispatchLine line : lineRepo.findByOrders(orderIds)) {
      lineMap.computeIfAbsent(line.orderId, k -> new java.util.ArrayList<>()).add(line);
    }

    int requested = 0;
    int reserved = 0;
    int shortage = 0;
    List<Map<String, Object>> orderDtos = new java.util.ArrayList<>();
    for (DispatchOrder order : orders) {
      List<DispatchLine> lines = lineMap.getOrDefault(order.id, List.of());
      orderDtos.add(DispatchOrderDtoFactory.listItem(order, lines));
      requested += lines.stream().mapToInt(l -> l.quantity).sum();
      reserved += lines.stream().mapToInt(l -> l.reservedQuantity).sum();
      shortage += lines.stream().mapToInt(l -> l.shortageQuantity).sum();
    }

    Map<String, Object> dto = summary(event);
    dto.put("dispatch_orders", orderDtos);
    dto.put("total_requested_quantity", requested);
    dto.put("total_reserved_quantity", reserved);
    dto.put("total_shortage_quantity", shortage);
    return dto;
  }
}
