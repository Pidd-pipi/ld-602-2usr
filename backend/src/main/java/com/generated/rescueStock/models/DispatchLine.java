package com.generated.rescueStock.models;

/** 调拨单行：申请量 quantity；审批后回填预占量与缺口 */
public class DispatchLine {
  public Long id;
  public Long orderId;
  public Long supplyItemId;
  public Integer quantity;
  public Integer reservedQuantity;
  public Integer shortageQuantity;
}
