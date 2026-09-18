package com.generated.rescueStock.services;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.generated.rescueStock.constructors.InventoryBatchDtoFactory;
import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.SupplyItem;
import com.generated.rescueStock.repositories.InventoryBatchRepository;
import com.generated.rescueStock.repositories.SupplyItemRepository;

@Service
public class InventoryBatchService {

  private final InventoryBatchRepository repo;
  private final SupplyItemRepository supplyItemRepo;

  public InventoryBatchService(InventoryBatchRepository repo, SupplyItemRepository supplyItemRepo) {
    this.repo = repo;
    this.supplyItemRepo = supplyItemRepo;
  }

  public List<Map<String, Object>> list() {
    return repo.findAll().stream().map(b -> InventoryBatchDtoFactory.toDto(b, supplyItemRepo.findById(b.supplyItemId))).toList();
  }

  /** 调拨页：按来源仓库查看各批次 总量/预占/可用 */
  public List<Map<String, Object>> listByWarehouse(Long warehouseId) {
    List<InventoryBatch> batches = repo.findByWarehouse(warehouseId);
    return batches.stream()
        .map(b -> InventoryBatchDtoFactory.toDto(b, supplyItemRepo.findById(b.supplyItemId)))
        .toList();
  }

  /** 按仓库+物资汇总可用量（创建调拨单时前端预检查看缺口） */
  public Map<String, Object> availability(Long warehouseId, Long supplyItemId) {
    SupplyItem item = supplyItemRepo.findById(supplyItemId);
    List<InventoryBatch> batches = repo.findByWarehouse(warehouseId).stream()
        .filter(b -> b.supplyItemId.equals(supplyItemId))
        .toList();
    int quantity = batches.stream().mapToInt(b -> b.quantity).sum();
    int reserved = batches.stream().mapToInt(b -> b.reservedQuantity).sum();
    return Map.of(
        "warehouse_id", warehouseId,
        "supply_item_id", supplyItemId,
        "supply_item_name", item == null ? "" : item.name,
        "quantity", quantity,
        "reserved_quantity", reserved,
        "available_quantity", quantity - reserved,
        "batches", batches.stream().map(InventoryBatchDtoFactory::toDto).toList());
  }
}
