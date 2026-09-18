package com.generated.rescueStock.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.generated.rescueStock.services.InventoryBatchService;

@RestController
@RequestMapping("/api/inventory-batch")
public class InventoryBatchController {

  private final InventoryBatchService service;

  public InventoryBatchController(InventoryBatchService service) {
    this.service = service;
  }

  /** 全部批次；?warehouse_id= 按仓库过滤（调拨页选来源仓后看可用/预占） */
  @GetMapping
  public List<Map<String, Object>> list(@RequestParam(name = "warehouse_id", required = false) Long warehouseId) {
    if (warehouseId != null) {
      return service.listByWarehouse(warehouseId);
    }
    return service.list();
  }

  /** 按仓库+物资汇总：总量 / 预占 / 可用 / 缺口判断 */
  @GetMapping("/availability")
  public Map<String, Object> availability(@RequestParam("warehouse_id") Long warehouseId,
                                          @RequestParam("supply_item_id") Long supplyItemId) {
    return service.availability(warehouseId, supplyItemId);
  }
}
