package com.generated.rescueStock.services;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.generated.rescueStock.constructors.SupplyItemDtoFactory;
import com.generated.rescueStock.repositories.SupplyItemRepository;

@Service
public class SupplyItemService {

  private final SupplyItemRepository repo;

  public SupplyItemService(SupplyItemRepository repo) {
    this.repo = repo;
  }

  public List<Map<String, Object>> list() {
    return repo.findAll().stream().map(SupplyItemDtoFactory::toDto).toList();
  }
}
