package com.generated.rescueStock.repositories;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.generated.rescueStock.models.InventoryTransaction;

/** 库存流水：RESERVE / RELEASE / CONSUME 与预占闭环双写 */
@Repository
public class InventoryTransactionRepository {

  private final JdbcTemplate jdbc;

  public InventoryTransactionRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void record(InventoryTransaction txn) {
    jdbc.update(
        "INSERT INTO inventory_transaction (batch_id, order_id, supply_item_id, warehouse_id, "
            + "change_type, change_quantity, balance_after, reserved_after, created_at, actor) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?)",
        txn.batchId, txn.orderId, txn.supplyItemId, txn.warehouseId,
        txn.changeType, txn.changeQuantity, txn.balanceAfter, txn.reservedAfter,
        Timestamp.valueOf(LocalDateTime.now()), txn.actor);
  }
}
