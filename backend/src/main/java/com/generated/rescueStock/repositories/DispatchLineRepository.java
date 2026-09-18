package com.generated.rescueStock.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.generated.rescueStock.models.DispatchLine;

@Repository
public class DispatchLineRepository {

  private final JdbcTemplate jdbc;

  public DispatchLineRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static final RowMapper<DispatchLine> MAPPER = (rs, n) -> {
    DispatchLine l = new DispatchLine();
    l.id = rs.getLong("id");
    l.orderId = rs.getLong("order_id");
    l.supplyItemId = rs.getLong("supply_item_id");
    l.quantity = rs.getInt("quantity");
    l.reservedQuantity = rs.getInt("reserved_quantity");
    l.shortageQuantity = rs.getInt("shortage_quantity");
    return l;
  };

  private static final String COLUMNS =
      "id, order_id, supply_item_id, quantity, reserved_quantity, shortage_quantity";

  public List<DispatchLine> findByOrder(Long orderId) {
    return jdbc.query(
        "SELECT " + COLUMNS + " FROM dispatch_line WHERE order_id = ? ORDER BY id", MAPPER, orderId);
  }

  /** 一次查多个调拨单的行（列表页聚合预占/缺口用） */
  public List<DispatchLine> findByOrders(List<Long> orderIds) {
    if (orderIds == null || orderIds.isEmpty()) {
      return List.of();
    }
    String placeholders = String.join(",", orderIds.stream().map(x -> "?").toList());
    return jdbc.query(
        "SELECT " + COLUMNS + " FROM dispatch_line WHERE order_id IN (" + placeholders
            + ") ORDER BY order_id, id",
        MAPPER, orderIds.toArray());
  }

  public Long insert(DispatchLine line) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc.update(con -> {
      var ps = con.prepareStatement(
          "INSERT INTO dispatch_line (order_id, supply_item_id, quantity, reserved_quantity, shortage_quantity) "
              + "VALUES (?,?,?,?,?)",
          new String[] {"id"});
      ps.setLong(1, line.orderId);
      ps.setLong(2, line.supplyItemId);
      ps.setInt(3, line.quantity);
      ps.setInt(4, line.reservedQuantity);
      ps.setInt(5, line.shortageQuantity);
      return ps;
    }, keyHolder);
    return keyHolder.getKey() == null ? null : keyHolder.getKey().longValue();
  }

  /** 审批完成后回填该行的预占量/缺口 */
  public void updateReservation(Long lineId, int reservedQuantity, int shortageQuantity) {
    jdbc.update(
        "UPDATE dispatch_line SET reserved_quantity = ?, shortage_quantity = ? WHERE id = ?",
        reservedQuantity, shortageQuantity, lineId);
  }

  /** 驳回/取消时清空行上的预占 */
  public void clearReservation(Long orderId) {
    jdbc.update(
        "UPDATE dispatch_line SET reserved_quantity = 0, shortage_quantity = 0 WHERE order_id = ?",
        orderId);
  }
}
