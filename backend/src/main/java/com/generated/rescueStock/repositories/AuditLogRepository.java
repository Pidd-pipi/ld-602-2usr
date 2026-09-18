package com.generated.rescueStock.repositories;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** 审计日志落库（auditLogMiddleware 调用） */
@Repository
public class AuditLogRepository {

  private final JdbcTemplate jdbc;

  public AuditLogRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void record(String actor, String action, String targetType, String targetId, String detail) {
    jdbc.update(
        "INSERT INTO audit_log (actor, action, target_type, target_id, detail, created_at) "
            + "VALUES (?,?,?,?,?,?)",
        actor, action, targetType, targetId, detail, Timestamp.valueOf(LocalDateTime.now()));
  }
}
