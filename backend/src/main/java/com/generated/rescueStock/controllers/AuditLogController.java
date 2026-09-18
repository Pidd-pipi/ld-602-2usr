package com.generated.rescueStock.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.rescueStock.middlewares.AuditLogMiddleware;
import com.generated.rescueStock.routes.AuditLogRoutes;

@RestController
@RequestMapping(AuditLogRoutes.PATH)
public class AuditLogController {
  private final AuditLogMiddleware auditLog;

  public AuditLogController(AuditLogMiddleware auditLog) {
    this.auditLog = auditLog;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return auditLog.list();
  }
}
