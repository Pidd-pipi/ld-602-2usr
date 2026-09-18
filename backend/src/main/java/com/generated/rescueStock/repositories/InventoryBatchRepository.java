package com.generated.rescueStock.repositories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import com.generated.rescueStock.models.InventoryBatch;

// 进程内库存批次仓库：预占/实扣直接修改批次对象，由 DispatchOrderService 的锁保证并发安全。
@Repository
public class InventoryBatchRepository {
  private final Map<Long, InventoryBatch> store = new ConcurrentHashMap<>();

  public List<InventoryBatch> findAll() {
    List<InventoryBatch> rows = new ArrayList<>(store.values());
    rows.sort(Comparator.comparingLong(b -> b.id));
    return rows;
  }

  public InventoryBatch findById(long id) {
    return store.get(id);
  }

  public List<InventoryBatch> findByWarehouseAndItem(long warehouseId, long supplyItemId) {
    List<InventoryBatch> rows = new ArrayList<>();
    for (InventoryBatch batch : store.values()) {
      if (batch.warehouseId == warehouseId && batch.supplyItemId == supplyItemId) {
        rows.add(batch);
      }
    }
    rows.sort(Comparator.comparingLong(b -> b.id));
    return rows;
  }

  public List<InventoryBatch> findByWarehouse(long warehouseId) {
    List<InventoryBatch> rows = new ArrayList<>();
    for (InventoryBatch batch : store.values()) {
      if (batch.warehouseId == warehouseId) {
        rows.add(batch);
      }
    }
    rows.sort(Comparator.comparingLong(b -> b.id));
    return rows;
  }

  public InventoryBatch save(InventoryBatch batch) {
    store.put(batch.id, batch);
    return batch;
  }
}
