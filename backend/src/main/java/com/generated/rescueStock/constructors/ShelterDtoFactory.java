package com.generated.rescueStock.constructors;

import java.util.LinkedHashMap;
import java.util.Map;

import com.generated.rescueStock.models.Shelter;

public final class ShelterDtoFactory {

  private ShelterDtoFactory() {}

  public static Map<String, Object> summary(Shelter s) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", s.id);
    dto.put("name", s.name);
    dto.put("district", s.district);
    return dto;
  }

  public static Map<String, Object> toDto(Shelter s) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", s.id);
    dto.put("name", s.name);
    dto.put("district", s.district);
    dto.put("capacity", s.capacity);
    dto.put("current_population", s.currentPopulation);
    dto.put("contact_person", s.contactPerson);
    dto.put("risk_level", s.riskLevel);
    dto.put("open_status", s.openStatus);
    return dto;
  }
}
