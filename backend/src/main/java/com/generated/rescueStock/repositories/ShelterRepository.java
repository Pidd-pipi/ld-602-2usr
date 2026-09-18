package com.generated.rescueStock.repositories;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.generated.rescueStock.models.Shelter;

@Repository
public class ShelterRepository {

  private final JdbcTemplate jdbc;

  public ShelterRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static final RowMapper<Shelter> MAPPER = (rs, n) -> {
    Shelter s = new Shelter();
    s.id = rs.getLong("id");
    s.name = rs.getString("name");
    s.district = rs.getString("district");
    s.capacity = rs.getInt("capacity");
    s.currentPopulation = rs.getInt("current_population");
    s.contactPerson = rs.getString("contact_person");
    s.riskLevel = rs.getString("risk_level");
    s.openStatus = rs.getString("open_status");
    return s;
  };

  public List<Shelter> findAll() {
    return jdbc.query(
        "SELECT id, name, district, capacity, current_population, contact_person, risk_level, open_status "
            + "FROM shelter ORDER BY id",
        MAPPER);
  }

  public Shelter findById(Long id) {
    List<Shelter> rows = jdbc.query(
        "SELECT id, name, district, capacity, current_population, contact_person, risk_level, open_status "
            + "FROM shelter WHERE id = ?",
        MAPPER, id);
    return rows.isEmpty() ? null : rows.get(0);
  }
}
