package com.generated.rescueStock.repositories;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.generated.rescueStock.models.Warehouse;

@Repository
public class WarehouseRepository {

  private final JdbcTemplate jdbc;

  public WarehouseRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static final RowMapper<Warehouse> MAPPER = (rs, n) -> {
    Warehouse w = new Warehouse();
    w.id = rs.getLong("id");
    w.name = rs.getString("name");
    w.district = rs.getString("district");
    w.address = rs.getString("address");
    w.managerId = rs.getLong("manager_id");
    w.capacityLevel = rs.getInt("capacity_level");
    w.contactPhone = rs.getString("contact_phone");
    w.status = rs.getString("status");
    return w;
  };

  public List<Warehouse> findAll() {
    return jdbc.query(
        "SELECT id, name, district, address, manager_id, capacity_level, contact_phone, status "
            + "FROM warehouse ORDER BY id",
        MAPPER);
  }

  public Warehouse findById(Long id) {
    List<Warehouse> rows = jdbc.query(
        "SELECT id, name, district, address, manager_id, capacity_level, contact_phone, status "
            + "FROM warehouse WHERE id = ?",
        MAPPER, id);
    return rows.isEmpty() ? null : rows.get(0);
  }
}
