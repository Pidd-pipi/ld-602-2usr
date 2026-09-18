package com.generated.rescueStock.constants;

/**
 * 日志模板集中存放，每个实体至少 4 条；所有写操作都要经 AuditLogMiddleware 落审计日志。
 * 字段/流程变更时必须同步改这里与调用处。
 */
public final class LogTemplates {
  // 通用
  public static final String CREATE = "{0} 创建：id={1}";
  public static final String UPDATE = "{0} 更新：id={1}，字段：{2}";
  public static final String STATUS = "{0} 状态变更：id={1}，{2} -> {3}";
  public static final String EXPORT = "{0} 导出：筛选条件 {1}";

  // 调拨单：至少 4 条
  public static final String DISPATCH_CREATE = "调拨单创建：id={0}，来源仓 {1}，共 {2} 行";
  public static final String DISPATCH_SUBMIT = "调拨单提交审批：id={0}";
  public static final String DISPATCH_APPROVE =
      "调拨单审批通过并预占：id={0}，审批人 {1}，占用批次 {2} 个，预占 {3} 件";
  public static final String DISPATCH_REJECT = "调拨单驳回：id={0}，审批人 {1}，原因：{2}，释放预占 {3} 件";
  public static final String DISPATCH_CANCEL = "调拨单取消：id={0}，操作人 {1}，原因：{2}，释放预占 {3} 件";
  public static final String DISPATCH_CONFIRM_OUTBOUND =
      "调拨单出库确认，预占转实扣：id={0}，操作人 {1}，实扣批次 {2} 个，共 {3} 件";
  public static final String DISPATCH_RECEIVE = "调拨单签收：id={0}，确认人 {1}";
  public static final String DISPATCH_SHORTAGE =
      "调拨单预占整单失败：id={0}，物资[{1}]缺口 {2} 件，未占用任何批次";

  // 库存批次
  public static final String BATCH_CREATE = "库存批次入库：id={0}，批次号 {1}，数量 {2}";
  public static final String BATCH_UPDATE = "库存批次更新：id={0}，字段：{1}";
  public static final String BATCH_STATUS = "库存批次质量状态变更：id={0}，{1} -> {2}";
  public static final String BATCH_EXPORT = "库存批次导出：仓库 {0}，物资 {1}";

  // 其余实体保留 4 条
  public static final String WAREHOUSE_CREATE = "应急仓库建档：id={0}，名称 {1}";
  public static final String WAREHOUSE_UPDATE = "应急仓库维护：id={0}，字段：{1}";
  public static final String WAREHOUSE_STATUS = "应急仓库启停用：id={0}，{1} -> {2}";
  public static final String WAREHOUSE_EXPORT = "应急仓库导出：区域 {0}";
  public static final String SHELTER_CREATE = "避难安置点建档：id={0}，名称 {1}";
  public static final String SHELTER_UPDATE = "避难安置点维护：id={0}，字段：{1}";
  public static final String SHELTER_STATUS = "避难安置点开放状态变更：id={0}，{1} -> {2}";
  public static final String SHELTER_EXPORT = "避难安置点导出：区域 {0}";
  public static final String SUPPLY_CREATE = "应急物资建档：id={0}，SKU {1}";
  public static final String SUPPLY_UPDATE = "应急物资维护：id={0}，字段：{1}";
  public static final String SUPPLY_EXPORT = "应急物资导出：分类 {0}";

  private LogTemplates() {}
}
