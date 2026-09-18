package com.generated.rescueStock.repositories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import com.generated.rescueStock.models.DispatchOrder;

@Repository
public class DispatchOrderRepository {
  private final Map<Long, DispatchOrder> store = new ConcurrentHashMap<>();
  private final AtomicLong orderSeq = new AtomicLong(0);
  private final AtomicLong lineSeq = new AtomicLong(0);

  public List<DispatchOrder> findAll() {
    List<DispatchOrder> rows = new ArrayList<>(store.values());
    rows.sort(Comparator.comparingLong(o -> o.id));
    return rows;
  }

  public DispatchOrder findById(long id) {
    return store.get(id);
  }

  public List<DispatchOrder> findByEventId(long eventId) {
    List<DispatchOrder> rows = new ArrayList<>();
    for (DispatchOrder order : store.values()) {
      if (order.eventId == eventId) {
        rows.add(order);
      }
    }
    rows.sort(Comparator.comparingLong(o -> o.id));
    return rows;
  }

  public DispatchOrder save(DispatchOrder order) {
    store.put(order.id, order);
    return order;
  }

  public long nextOrderId() {
    return orderSeq.incrementAndGet();
  }

  public long nextLineId() {
    return lineSeq.incrementAndGet();
  }

  // 种子数据写入后同步序列，避免主键冲突。
  public void ensureSeqAbove(long orderId, long lineId) {
    orderSeq.accumulateAndGet(orderId, Math::max);
    lineSeq.accumulateAndGet(lineId, Math::max);
  }
}
