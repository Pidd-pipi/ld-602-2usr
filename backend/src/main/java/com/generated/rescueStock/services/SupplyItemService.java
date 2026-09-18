package com.generated.rescueStock.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.generated.rescueStock.constructors.SupplyItemDtoFactory;
import com.generated.rescueStock.models.SupplyItem;
import com.generated.rescueStock.repositories.SupplyItemRepository;

@Service
public class SupplyItemService {
  private final SupplyItemRepository repo;

  public SupplyItemService(SupplyItemRepository repo) {
    this.repo = repo;
  }

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> rows = new ArrayList<>();
    for (SupplyItem item : repo.findAll()) {
      rows.add(SupplyItemDtoFactory.toDto(item));
    }
    return rows;
  }
}
