package com.generated.rescueStock.constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import com.generated.rescueStock.models.Shelter;

public final class ShelterDtoFactory {

  private ShelterDtoFactory() {}

  public static Map<String, Object> toDto(Shelter shelter) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", shelter.id);
    dto.put("name", shelter.name);
    dto.put("district", shelter.district);
    dto.put("capacity", shelter.capacity);
    dto.put("current_population", shelter.currentPopulation);
    dto.put("contact_person", shelter.contactPerson);
    dto.put("risk_level", shelter.riskLevel);
    dto.put("open_status", shelter.openStatus);
    return dto;
  }
}
