package com.generated.rescueStock.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.generated.rescueStock.constructors.WarehouseDtoFactory;
import com.generated.rescueStock.models.Warehouse;
import com.generated.rescueStock.repositories.WarehouseRepository;

@Service
public class WarehouseService {
  private final WarehouseRepository repo;

  public WarehouseService(WarehouseRepository repo) {
    this.repo = repo;
  }

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> rows = new ArrayList<>();
    for (Warehouse warehouse : repo.findAll()) {
      rows.add(WarehouseDtoFactory.toDto(warehouse));
    }
    return rows;
  }
}
