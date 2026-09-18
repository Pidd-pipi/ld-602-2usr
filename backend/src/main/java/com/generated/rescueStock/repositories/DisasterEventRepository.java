package com.generated.rescueStock.repositories;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.generated.rescueStock.models.DisasterEvent;

@Repository
public class DisasterEventRepository {

  private final JdbcTemplate jdbc;

  public DisasterEventRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static final RowMapper<DisasterEvent> MAPPER = (rs, n) -> {
    DisasterEvent e = new DisasterEvent();
    e.id = rs.getLong("id");
    e.name = rs.getString("name");
    e.eventType = rs.getString("event_type");
    e.district = rs.getString("district");
    e.level = rs.getString("level");
    Timestamp ts = rs.getTimestamp("occurred_at");
    e.occurredAt = ts == null ? null : ts.toLocalDateTime();
    e.status = rs.getString("status");
    e.description = rs.getString("description");
    return e;
  };

  public List<DisasterEvent> findAll() {
    return jdbc.query(
        "SELECT id, name, event_type, district, level, occurred_at, status, description "
            + "FROM disaster_event ORDER BY occurred_at DESC, id DESC",
        MAPPER);
  }

  public DisasterEvent findById(Long id) {
    List<DisasterEvent> rows = jdbc.query(
        "SELECT id, name, event_type, district, level, occurred_at, status, description "
            + "FROM disaster_event WHERE id = ?",
        MAPPER, id);
    return rows.isEmpty() ? null : rows.get(0);
  }
}
