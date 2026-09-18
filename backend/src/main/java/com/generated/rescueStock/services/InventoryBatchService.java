package com.generated.rescueStock.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.generated.rescueStock.constructors.InventoryBatchDtoFactory;
import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.SupplyItem;
import com.generated.rescueStock.models.Warehouse;
import com.generated.rescueStock.repositories.InventoryBatchRepository;
import com.generated.rescueStock.repositories.SupplyItemRepository;
import com.generated.rescueStock.repositories.WarehouseRepository;

@Service
public class InventoryBatchService {
  private final InventoryBatchRepository repo;
  private final SupplyItemRepository supplyItemRepo;
  private final WarehouseRepository warehouseRepo;

  public InventoryBatchService(InventoryBatchRepository repo, SupplyItemRepository supplyItemRepo,
      WarehouseRepository warehouseRepo) {
    this.repo = repo;
    this.supplyItemRepo = supplyItemRepo;
    this.warehouseRepo = warehouseRepo;
  }

  public List<Map<String, Object>> list(Long warehouseId) {
    List<InventoryBatch> batches = warehouseId == null ? repo.findAll() : repo.findByWarehouse(warehouseId);
    List<Map<String, Object>> rows = new ArrayList<>();
    for (InventoryBatch batch : batches) {
      SupplyItem item = supplyItemRepo.findById(batch.supplyItemId);
      Warehouse warehouse = warehouseRepo.findById(batch.warehouseId);
      rows.add(InventoryBatchDtoFactory.toDto(batch, item == null ? null : item.name,
          warehouse == null ? null : warehouse.name));
    }
    return rows;
  }
}
