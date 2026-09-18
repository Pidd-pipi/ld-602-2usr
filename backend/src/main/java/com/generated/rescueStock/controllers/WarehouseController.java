package com.generated.rescueStock.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generated.rescueStock.services.WarehouseService;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

  private final WarehouseService service;

  public WarehouseController(WarehouseService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }
}
