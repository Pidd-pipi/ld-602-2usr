package com.generated.rescueStock.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.generated.rescueStock.models.DispatchOrder;

@Repository
public class DispatchOrderRepository {

  private final JdbcTemplate jdbc;

  public DispatchOrderRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static final RowMapper<DispatchOrder> MAPPER = new OrderRowMapper();

  private static final String COLUMNS =
      "id, event_id, source_warehouse_id, shelter_id, priority, status, requested_by, approved_by, "
          + "created_at, updated_at, submitted_at, approved_at, dispatched_at, received_at, "
          + "rejected_at, cancelled_at, reject_reason, cancel_reason, version";

  public List<DispatchOrder> findAll() {
    return jdbc.query("SELECT " + COLUMNS + " FROM dispatch_order ORDER BY id DESC", MAPPER);
  }

  public List<DispatchOrder> findByEvent(Long eventId) {
    return jdbc.query(
        "SELECT " + COLUMNS + " FROM dispatch_order WHERE event_id = ? ORDER BY id", MAPPER, eventId);
  }

  public DispatchOrder findById(Long id) {
    List<DispatchOrder> rows =
        jdbc.query("SELECT " + COLUMNS + " FROM dispatch_order WHERE id = ?", MAPPER, id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /** 事务内对调拨单行加锁，防止同一单并发审批 */
  public DispatchOrder lockByIdForUpdate(Long id) {
    List<DispatchOrder> rows = jdbc.query(
        "SELECT " + COLUMNS + " FROM dispatch_order WHERE id = ? FOR UPDATE", MAPPER, id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  public Long insert(DispatchOrder o) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO dispatch_order (event_id, source_warehouse_id, shelter_id, priority, status, "
              + "requested_by, approved_by, created_at, updated_at, submitted_at) "
              + "VALUES (?,?,?,?,?,?,?,?,?,?)",
          new String[] {"id"});
      ps.setObject(1, o.eventId);
      ps.setLong(2, o.sourceWarehouseId);
      ps.setLong(3, o.shelterId);
      ps.setString(4, o.priority);
      ps.setString(5, o.status);
      ps.setString(6, o.requestedBy);
      ps.setObject(7, o.approvedBy);
      Timestamp now = Timestamp.valueOf(LocalDateTime.now());
      ps.setTimestamp(8, now);
      ps.setTimestamp(9, now);
      ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
      return ps;
    }, keyHolder);
    return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
  }

  /** 乐观状态条件更新：状态非预期（例如已被并发审批）时返回 0 */
  public int compareAndUpdateStatus(DispatchOrder o, String expectedStatus) {
    return jdbc.update(
        "UPDATE dispatch_order SET status = ?, approved_by = ?, updated_at = ?, "
            + "approved_at = COALESCE(approved_at, ?), dispatched_at = COALESCE(dispatched_at, ?), "
            + "received_at = COALESCE(received_at, ?), rejected_at = COALESCE(rejected_at, ?), "
            + "cancelled_at = COALESCE(cancelled_at, ?), reject_reason = ?, cancel_reason = ? "
            + "WHERE id = ? AND status = ?",
        o.status, o.approvedBy, Timestamp.valueOf(LocalDateTime.now()),
        o.approvedAt == null ? null : Timestamp.valueOf(o.approvedAt),
        o.dispatchedAt == null ? null : Timestamp.valueOf(o.dispatchedAt),
        o.receivedAt == null ? null : Timestamp.valueOf(o.receivedAt),
        o.rejectedAt == null ? null : Timestamp.valueOf(o.rejectedAt),
        o.cancelledAt == null ? null : Timestamp.valueOf(o.cancelledAt),
        o.rejectReason, o.cancelReason, o.id, expectedStatus);
  }

  private static final class OrderRowMapper implements RowMapper<DispatchOrder> {
    @Override
    public DispatchOrder mapRow(ResultSet rs, int rowNum) throws SQLException {
      DispatchOrder o = new DispatchOrder();
      o.id = rs.getLong("id");
      long eventId = rs.getLong("event_id");
      o.eventId = rs.wasNull() ? null : eventId;
      o.sourceWarehouseId = rs.getLong("source_warehouse_id");
      o.shelterId = rs.getLong("shelter_id");
      o.priority = rs.getString("priority");
      o.status = rs.getString("status");
      o.requestedBy = rs.getString("requested_by");
      o.approvedBy = rs.getString("approved_by");
      o.createdAt = toLdt(rs.getTimestamp("created_at"));
      o.updatedAt = toLdt(rs.getTimestamp("updated_at"));
      o.submittedAt = toLdt(rs.getTimestamp("submitted_at"));
      o.approvedAt = toLdt(rs.getTimestamp("approved_at"));
      o.dispatchedAt = toLdt(rs.getTimestamp("dispatched_at"));
      o.receivedAt = toLdt(rs.getTimestamp("received_at"));
      o.rejectedAt = toLdt(rs.getTimestamp("rejected_at"));
      o.cancelledAt = toLdt(rs.getTimestamp("cancelled_at"));
      o.rejectReason = rs.getString("reject_reason");
      o.cancelReason = rs.getString("cancel_reason");
      o.version = rs.getInt("version");
      return o;
    }

    private LocalDateTime toLdt(Timestamp ts) {
      return ts == null ? null : ts.toLocalDateTime();
    }
  }
}
