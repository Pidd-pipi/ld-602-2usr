package com.generated.rescueStock.repositories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import com.generated.rescueStock.models.DispatchEvent;

@Repository
public class DispatchEventRepository {
  private final Map<Long, DispatchEvent> store = new ConcurrentHashMap<>();

  public List<DispatchEvent> findAll() {
    List<DispatchEvent> rows = new ArrayList<>(store.values());
    rows.sort(Comparator.comparingLong(e -> e.id));
    return rows;
  }

  public DispatchEvent findById(long id) {
    return store.get(id);
  }

  public DispatchEvent save(DispatchEvent event) {
    store.put(event.id, event);
    return event;
  }
}
