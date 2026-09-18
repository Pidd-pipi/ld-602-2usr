package com.generated.rescueStock.constants;

/** 错误码：认证、鉴权、入参、状态机、预占缺口、并发冲突等 */
public final class ErrorCodes {
  public static final String AUTH_REQUIRED = "AUTH_REQUIRED";
  public static final String RBAC_DENIED = "RBAC_DENIED";
  public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
  public static final String RATE_LIMITED = "RATE_LIMITED";
  public static final String DISPATCH_NOT_FOUND = "DISPATCH_NOT_FOUND";
  public static final String DISPATCH_STATUS_CONFLICT = "DISPATCH_STATUS_CONFLICT";
  public static final String RESERVATION_SHORTAGE = "RESERVATION_SHORTAGE";
  public static final String RESERVATION_NOT_READY = "RESERVATION_NOT_READY";
  public static final String CONCURRENT_APPROVAL = "CONCURRENT_APPROVAL";
  public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

  private ErrorCodes() {}
}
