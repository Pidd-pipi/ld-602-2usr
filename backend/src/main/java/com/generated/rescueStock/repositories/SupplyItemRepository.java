package com.generated.rescueStock.repositories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import com.generated.rescueStock.models.SupplyItem;

@Repository
public class SupplyItemRepository {
  private final Map<Long, SupplyItem> store = new ConcurrentHashMap<>();

  public List<SupplyItem> findAll() {
    List<SupplyItem> rows = new ArrayList<>(store.values());
    rows.sort(Comparator.comparingLong(i -> i.id));
    return rows;
  }

  public SupplyItem findById(long id) {
    return store.get(id);
  }

  public SupplyItem save(SupplyItem item) {
    store.put(item.id, item);
    return item;
  }
}
