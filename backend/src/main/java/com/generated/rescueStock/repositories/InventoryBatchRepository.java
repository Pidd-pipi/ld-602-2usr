package com.generated.rescueStock.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.generated.rescueStock.constants.QualityStatus;
import com.generated.rescueStock.models.InventoryBatch;

/**
 * 库存批次数据访问。
 * 分配走 FEFO：ORDER BY expire_at ASC, batch_no ASC，并对命中行加 FOR UPDATE 行锁。
 */
@Repository
public class InventoryBatchRepository {

  private final JdbcTemplate jdbc;

  public InventoryBatchRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static final RowMapper<InventoryBatch> MAPPER = new BatchRowMapper();

  public List<InventoryBatch> findAll() {
    return jdbc.query(
        "SELECT id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, "
            + "expire_at, inbound_source, quality_status, version FROM inventory_batch ORDER BY id",
        MAPPER);
  }

  public List<InventoryBatch> findByWarehouse(Long warehouseId) {
    return jdbc.query(
        "SELECT id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, "
            + "expire_at, inbound_source, quality_status, version "
            + "FROM inventory_batch WHERE warehouse_id = ? ORDER BY supply_item_id, expire_at, batch_no",
        MAPPER, warehouseId);
  }

  public InventoryBatch findById(Long id) {
    List<InventoryBatch> rows = jdbc.query(
        "SELECT id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, "
            + "expire_at, inbound_source, quality_status, version FROM inventory_batch WHERE id = ?",
        MAPPER, id);
    return rows.isEmpty() ? null : rows.get(0);
  }

  /**
   * 在事务内锁定来源仓库指定物资的合格批次并按 FEFO 排序返回。
   * 最早到期先出；保质期（到期日）相同按批次号升序。
   */
  public List<InventoryBatch> lockAvailableBatchesForUpdate(Long warehouseId, Long supplyItemId) {
    return jdbc.query(
        "SELECT id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, "
            + "expire_at, inbound_source, quality_status, version FROM inventory_batch "
            + "WHERE warehouse_id = ? AND supply_item_id = ? AND quality_status = ? "
            + "AND quantity - reserved_quantity > 0 "
            + "ORDER BY expire_at ASC, batch_no ASC "
            + "FOR UPDATE",
        MAPPER, warehouseId, supplyItemId, QualityStatus.QUALIFIED.name());
  }

  /** 预占：reserved_quantity += delta，version += 1 */
  public int addReserved(Long batchId, int delta, int expectedVersion) {
    return jdbc.update(
        "UPDATE inventory_batch SET reserved_quantity = reserved_quantity + ?, version = version + 1 "
            + "WHERE id = ? AND version = ? AND quantity - reserved_quantity >= ?",
        delta, batchId, expectedVersion, delta);
  }

  /** 释放预占：reserved_quantity -= delta */
  public int releaseReserved(Long batchId, int delta) {
    return jdbc.update(
        "UPDATE inventory_batch SET reserved_quantity = reserved_quantity - ?, version = version + 1 "
            + "WHERE id = ? AND reserved_quantity >= ?",
        delta, batchId, delta);
  }

  /** 出库实扣：总量与预占量同时扣减 */
  public int consume(Long batchId, int delta) {
    return jdbc.update(
        "UPDATE inventory_batch SET quantity = quantity - ?, reserved_quantity = reserved_quantity - ?, "
            + "version = version + 1 WHERE id = ? AND quantity >= ? AND reserved_quantity >= ?",
        delta, delta, batchId, delta, delta);
  }

  private static final class BatchRowMapper implements RowMapper<InventoryBatch> {
    @Override
    public InventoryBatch mapRow(ResultSet rs, int rowNum) throws SQLException {
      InventoryBatch b = new InventoryBatch();
      b.id = rs.getLong("id");
      b.warehouseId = rs.getLong("warehouse_id");
      b.supplyItemId = rs.getLong("supply_item_id");
      b.batchNo = rs.getString("batch_no");
      b.quantity = rs.getInt("quantity");
      b.reservedQuantity = rs.getInt("reserved_quantity");
      java.sql.Timestamp ts = rs.getTimestamp("expire_at");
      b.expireAt = ts == null ? null : ts.toLocalDateTime();
      b.inboundSource = rs.getString("inbound_source");
      b.qualityStatus = rs.getString("quality_status");
      b.version = rs.getInt("version");
      return b;
    }
  }
}
