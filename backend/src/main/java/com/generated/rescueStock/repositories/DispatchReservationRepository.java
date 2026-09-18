package com.generated.rescueStock.repositories;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import com.generated.rescueStock.constants.ReservationStatus;
import com.generated.rescueStock.models.DispatchReservation;

@Repository
public class DispatchReservationRepository {
  private final Map<Long, DispatchReservation> store = new ConcurrentHashMap<>();
  private final AtomicLong seq = new AtomicLong(0);

  public List<DispatchReservation> findAll() {
    List<DispatchReservation> rows = new ArrayList<>(store.values());
    rows.sort(Comparator.comparingLong(r -> r.id));
    return rows;
  }

  public List<DispatchReservation> findByOrderId(long orderId) {
    List<DispatchReservation> rows = new ArrayList<>();
    for (DispatchReservation reservation : store.values()) {
      if (reservation.dispatchOrderId == orderId) {
        rows.add(reservation);
      }
    }
    rows.sort(Comparator.comparingLong(r -> r.id));
    return rows;
  }

  public List<DispatchReservation> findActiveByOrderId(long orderId) {
    List<DispatchReservation> rows = new ArrayList<>();
    for (DispatchReservation reservation : findByOrderId(orderId)) {
      if (ReservationStatus.RESERVED.name().equals(reservation.status)) {
        rows.add(reservation);
      }
    }
    return rows;
  }

  public int sumByLineAndStatus(long lineId, ReservationStatus status) {
    int total = 0;
    for (DispatchReservation reservation : store.values()) {
      if (reservation.dispatchLineId == lineId && status.name().equals(reservation.status)) {
        total += reservation.quantity;
      }
    }
    return total;
  }

  public DispatchReservation save(DispatchReservation reservation) {
    store.put(reservation.id, reservation);
    return reservation;
  }

  public long nextId() {
    return seq.incrementAndGet();
  }

  public void ensureSeqAbove(long id) {
    seq.accumulateAndGet(id, Math::max);
  }
}
