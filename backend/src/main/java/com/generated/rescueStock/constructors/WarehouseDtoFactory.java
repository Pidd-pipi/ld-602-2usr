package com.generated.rescueStock.constructors;

import java.util.LinkedHashMap;
import java.util.Map;

import com.generated.rescueStock.models.Warehouse;

public final class WarehouseDtoFactory {

  private WarehouseDtoFactory() {}

  public static Map<String, Object> summary(Warehouse w) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", w.id);
    dto.put("name", w.name);
    dto.put("district", w.district);
    return dto;
  }

  public static Map<String, Object> toDto(Warehouse w) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", w.id);
    dto.put("name", w.name);
    dto.put("district", w.district);
    dto.put("address", w.address);
    dto.put("manager_id", w.managerId);
    dto.put("capacity_level", w.capacityLevel);
    dto.put("contact_phone", w.contactPhone);
    dto.put("status", w.status);
    return dto;
  }
}
