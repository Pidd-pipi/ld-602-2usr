package com.generated.rescueStock.repositories;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.generated.rescueStock.models.SupplyItem;

@Repository
public class SupplyItemRepository {

  private final JdbcTemplate jdbc;

  public SupplyItemRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static final RowMapper<SupplyItem> MAPPER = (rs, n) -> {
    SupplyItem item = new SupplyItem();
    item.id = rs.getLong("id");
    item.skuCode = rs.getString("sku_code");
    item.name = rs.getString("name");
    item.category = rs.getString("category");
    item.unit = rs.getString("unit");
    item.safetyStock = rs.getInt("safety_stock");
    item.expireDays = rs.getInt("expire_days");
    item.storageRequirement = rs.getString("storage_requirement");
    return item;
  };

  public List<SupplyItem> findAll() {
    return jdbc.query(
        "SELECT id, sku_code, name, category, unit, safety_stock, expire_days, storage_requirement "
            + "FROM supply_item ORDER BY id",
        MAPPER);
  }

  public SupplyItem findById(Long id) {
    List<SupplyItem> rows = jdbc.query(
        "SELECT id, sku_code, name, category, unit, safety_stock, expire_days, storage_requirement "
            + "FROM supply_item WHERE id = ?",
        MAPPER, id);
    return rows.isEmpty() ? null : rows.get(0);
  }
}
