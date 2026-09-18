package com.generated.rescueStock.repositories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import com.generated.rescueStock.models.Shelter;

@Repository
public class ShelterRepository {
  private final Map<Long, Shelter> store = new ConcurrentHashMap<>();

  public List<Shelter> findAll() {
    List<Shelter> rows = new ArrayList<>(store.values());
    rows.sort(Comparator.comparingLong(s -> s.id));
    return rows;
  }

  public Shelter findById(long id) {
    return store.get(id);
  }

  public Shelter save(Shelter shelter) {
    store.put(shelter.id, shelter);
    return shelter;
  }
}
