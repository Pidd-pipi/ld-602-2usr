package com.generated.rescueStock.constructors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.generated.rescueStock.models.DispatchEvent;

public final class DispatchEventDtoFactory {

  private DispatchEventDtoFactory() {}

  public static Map<String, Object> toDto(DispatchEvent event) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", event.id);
    dto.put("name", event.name);
    dto.put("disaster_type", event.disasterType);
    dto.put("level", event.level);
    dto.put("status", event.status);
    dto.put("occurred_at", event.occurredAt);
    dto.put("description", event.description);
    return dto;
  }

  public static Map<String, Object> detailDto(DispatchEvent event, List<Map<String, Object>> orders) {
    Map<String, Object> dto = toDto(event);
    dto.put("orders", orders);
    return dto;
  }
}
