package com.generated.rescueStock.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.generated.rescueStock.routes.InventoryBatchRoutes;
import com.generated.rescueStock.services.InventoryBatchService;

@RestController
@RequestMapping(InventoryBatchRoutes.PATH)
public class InventoryBatchController {
  private final InventoryBatchService service;

  public InventoryBatchController(InventoryBatchService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list(@RequestParam(name = "warehouse_id", required = false) Long warehouseId) {
    return service.list(warehouseId);
  }
}
