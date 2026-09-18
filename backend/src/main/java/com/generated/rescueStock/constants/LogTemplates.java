package com.generated.rescueStock.constants;

public final class LogTemplates {
  public static final String CREATE = "create";
  public static final String UPDATE = "update";
  public static final String STATUS = "status";
  public static final String EXPORT = "export";
  // 调拨预占闭环日志模板
  public static final String DISPATCH_CREATE = "调拨单创建";
  public static final String DISPATCH_SUBMIT = "调拨单提交审批";
  public static final String DISPATCH_APPROVED = "调拨单审批通过，按最早到期先出完成库存预占";
  public static final String DISPATCH_APPROVE_SHORTAGE = "调拨单审批失败：可用库存不足，未占用任何批次";
  public static final String DISPATCH_REJECTED = "调拨单驳回，释放全部预占";
  public static final String DISPATCH_CANCELLED = "调拨单取消，释放全部预占";
  public static final String DISPATCH_OUTBOUND = "调拨单出库确认，预占转为实扣";
  public static final String DISPATCH_RECEIVED = "调拨单签收完成";
}
