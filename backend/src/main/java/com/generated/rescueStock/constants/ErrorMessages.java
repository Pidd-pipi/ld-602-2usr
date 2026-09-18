package com.generated.rescueStock.constants;

public final class ErrorMessages {
  public static final String AUTH_REQUIRED = "missing token";
  public static final String RBAC_DENIED = "role denied";
  public static final String VALIDATION_FAILED = "调拨单字段缺失或格式错误";
  public static final String ORDER_NOT_FOUND = "调拨单不存在";
  public static final String EVENT_NOT_FOUND = "事件不存在";
  public static final String INVALID_STATUS_TRANSITION = "当前状态不允许执行该操作";
  public static final String INSUFFICIENT_STOCK = "来源仓库可用库存不足，整单审批失败，未占用任何批次";
  public static final String INTERNAL_ERROR = "服务内部错误";
}
