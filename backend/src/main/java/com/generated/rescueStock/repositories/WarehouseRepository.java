package com.generated.rescueStock.repositories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import com.generated.rescueStock.models.Warehouse;

@Repository
public class WarehouseRepository {
  private final Map<Long, Warehouse> store = new ConcurrentHashMap<>();

  public List<Warehouse> findAll() {
    List<Warehouse> rows = new ArrayList<>(store.values());
    rows.sort(Comparator.comparingLong(w -> w.id));
    return rows;
  }

  public Warehouse findById(long id) {
    return store.get(id);
  }

  public Warehouse save(Warehouse warehouse) {
    store.put(warehouse.id, warehouse);
    return warehouse;
  }
}
