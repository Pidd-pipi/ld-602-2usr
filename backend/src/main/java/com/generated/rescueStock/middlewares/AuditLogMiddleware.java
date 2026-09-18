package com.generated.rescueStock.middlewares;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.generated.rescueStock.constants.LogTemplates;
import com.generated.rescueStock.repositories.AuditLogRepository;

/**
 * 操作日志中间件：所有写操作（创建/审批/出库/驳回/取消/签收）都经过这里落 audit_log，
 * 与库存流水（inventory_transaction）形成双写留痕。
 */
@Component
public class AuditLogMiddleware {

  private static final Logger log = LoggerFactory.getLogger(AuditLogMiddleware.class);

  private final AuditLogRepository auditLogRepository;

  public AuditLogMiddleware(AuditLogRepository auditLogRepository) {
    this.auditLogRepository = auditLogRepository;
  }

  public void log(String actor, String action, String targetType, Long targetId, String template, Object... args) {
    String detail = MessageFormat.format(template, args);
    try {
      auditLogRepository.record(actor, action, targetType, String.valueOf(targetId), detail);
    } catch (Exception ex) {
      // 审计失败不吞业务异常，仅记录错误日志
      log.error("write audit log failed: action={}, target={}/{}", action, targetType, targetId, ex);
    }
  }

  public void dispatchCreate(String actor, Long orderId, Long warehouseId, int lineCount) {
    log(actor, "DISPATCH_CREATE", "DispatchOrder", orderId, LogTemplates.DISPATCH_CREATE,
        orderId, warehouseId, lineCount);
  }

  public void dispatchApprove(String actor, Long orderId, int batchCount, int reservedTotal) {
    log(actor, "DISPATCH_APPROVE", "DispatchOrder", orderId, LogTemplates.DISPATCH_APPROVE,
        orderId, actor, batchCount, reservedTotal);
  }

  public void dispatchShortage(String actor, Long orderId, String supplyName, int shortage) {
    log(actor, "DISPATCH_SHORTAGE", "DispatchOrder", orderId, LogTemplates.DISPATCH_SHORTAGE,
        orderId, supplyName, shortage);
  }

  public void dispatchReject(String actor, Long orderId, String reason, int releasedTotal) {
    log(actor, "DISPATCH_REJECT", "DispatchOrder", orderId, LogTemplates.DISPATCH_REJECT,
        orderId, actor, reason, releasedTotal);
  }

  public void dispatchCancel(String actor, Long orderId, String reason, int releasedTotal) {
    log(actor, "DISPATCH_CANCEL", "DispatchOrder", orderId, LogTemplates.DISPATCH_CANCEL,
        orderId, actor, reason, releasedTotal);
  }

  public void dispatchOutbound(String actor, Long orderId, int batchCount, int consumedTotal) {
    log(actor, "DISPATCH_CONFIRM_OUTBOUND", "DispatchOrder", orderId,
        LogTemplates.DISPATCH_CONFIRM_OUTBOUND, orderId, actor, batchCount, consumedTotal);
  }

  public void dispatchReceive(String actor, Long orderId) {
    log(actor, "DISPATCH_RECEIVE", "DispatchOrder", orderId, LogTemplates.DISPATCH_RECEIVE,
        orderId, actor);
  }
}
