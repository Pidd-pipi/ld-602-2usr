package com.generated.rescueStock.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generated.rescueStock.services.SupplyItemService;

@RestController
@RequestMapping("/api/supply-item")
public class SupplyItemController {

  private final SupplyItemService service;

  public SupplyItemController(SupplyItemService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }
}
