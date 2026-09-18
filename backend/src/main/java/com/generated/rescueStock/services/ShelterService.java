package com.generated.rescueStock.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.generated.rescueStock.constructors.ShelterDtoFactory;
import com.generated.rescueStock.models.Shelter;
import com.generated.rescueStock.repositories.ShelterRepository;

@Service
public class ShelterService {
  private final ShelterRepository repo;

  public ShelterService(ShelterRepository repo) {
    this.repo = repo;
  }

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> rows = new ArrayList<>();
    for (Shelter shelter : repo.findAll()) {
      rows.add(ShelterDtoFactory.toDto(shelter));
    }
    return rows;
  }
}
