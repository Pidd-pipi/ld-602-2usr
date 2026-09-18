package com.generated.rescueStock.constants;

/**
 * 错误消息模板。{0} 等占位符由 service/controller 用 MessageFormat 填充。
 * service 与 controller 必须分别包装异常，禁止只在全局处理器里吞掉。
 */
public final class ErrorMessages {
  public static final String AUTH_REQUIRED = "缺少登录令牌，请先登录";
  public static final String RBAC_DENIED = "当前角色无权执行该操作";
  public static final String VALIDATION_FAILED = "入参校验失败：{0}";
  public static final String RATE_LIMITED = "请求过于频繁，请稍后再试";
  public static final String DISPATCH_NOT_FOUND = "调拨单不存在：id={0}";
  public static final String DISPATCH_STATUS_CONFLICT = "调拨单 {0} 当前状态 {1} 不允许执行 {2}";
  public static final String RESERVATION_SHORTAGE =
      "来源仓库可用库存不足，整单预占失败，缺口 {0} 件，未占用任何批次";
  public static final String RESERVATION_SHORTAGE_LINE =
      "物资[{0}]申请 {1} 件，可用 {2} 件，缺口 {3} 件";
  public static final String RESERVATION_NOT_READY = "调拨单 {0} 尚未完成预占，无法出库确认";
  public static final String CONCURRENT_APPROVAL =
      "同一批次存在并发审批，本单未抢占到库存，请刷新后重试";
  public static final String INTERNAL_ERROR = "服务内部错误，请联系管理员";

  private ErrorMessages() {}
}
