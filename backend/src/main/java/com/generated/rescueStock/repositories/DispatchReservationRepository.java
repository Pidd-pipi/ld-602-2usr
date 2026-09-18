package com.generated.rescueStock.repositories;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.generated.rescueStock.models.DispatchReservation;

@Repository
public class DispatchReservationRepository {

  private final JdbcTemplate jdbc;

  public DispatchReservationRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static final String COLUMNS =
      "id, order_id, line_id, batch_id, quantity, status, created_at, consumed_at, released_at";

  private static final RowMapper<DispatchReservation> MAPPER = (rs, n) -> {
    DispatchReservation r = new DispatchReservation();
    r.id = rs.getLong("id");
    r.orderId = rs.getLong("order_id");
    r.lineId = rs.getLong("line_id");
    r.batchId = rs.getLong("batch_id");
    r.quantity = rs.getInt("quantity");
    r.status = rs.getString("status");
    Timestamp c = rs.getTimestamp("created_at");
    r.createdAt = c == null ? null : c.toLocalDateTime();
    Timestamp u = rs.getTimestamp("consumed_at");
    r.consumedAt = u == null ? null : u.toLocalDateTime();
    Timestamp rl = rs.getTimestamp("released_at");
    r.releasedAt = rl == null ? null : rl.toLocalDateTime();
    return r;
  };

  public List<DispatchReservation> findByOrder(Long orderId) {
    return jdbc.query(
        "SELECT " + COLUMNS + " FROM dispatch_reservation WHERE order_id = ? ORDER BY id",
        MAPPER, orderId);
  }

  public List<DispatchReservation> findActiveByOrderForUpdate(Long orderId) {
    return jdbc.query(
        "SELECT " + COLUMNS + " FROM dispatch_reservation WHERE order_id = ? AND status = 'RESERVED' "
            + "FOR UPDATE",
        MAPPER, orderId);
  }

  public void insert(Long orderId, Long lineId, Long batchId, int quantity) {
    jdbc.update(
        "INSERT INTO dispatch_reservation (order_id, line_id, batch_id, quantity, status, created_at) "
            + "VALUES (?,?,?,?,'RESERVED',?)",
        orderId, lineId, batchId, quantity, Timestamp.valueOf(LocalDateTime.now()));
  }

  /** 出库确认：本单全部预占 -> CONSUMED */
  public int markConsumed(Long orderId) {
    return jdbc.update(
        "UPDATE dispatch_reservation SET status = 'CONSUMED', consumed_at = ? "
            + "WHERE order_id = ? AND status = 'RESERVED'",
        Timestamp.valueOf(LocalDateTime.now()), orderId);
  }

  /** 驳回/取消：本单全部预占 -> RELEASED */
  public int markReleased(Long orderId) {
    return jdbc.update(
        "UPDATE dispatch_reservation SET status = 'RELEASED', released_at = ? "
            + "WHERE order_id = ? AND status = 'RESERVED'",
        Timestamp.valueOf(LocalDateTime.now()), orderId);
  }

  /** 刷新后仍可回读：含已释放/已实扣的历史明细 */
  public List<DispatchReservation> findHistoryByBatch(Long batchId) {
    return jdbc.query(
        "SELECT " + COLUMNS + " FROM dispatch_reservation WHERE batch_id = ? ORDER BY id DESC",
        MAPPER, batchId);
  }
}
