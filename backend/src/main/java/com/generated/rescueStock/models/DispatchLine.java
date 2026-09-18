package com.generated.rescueStock.models;

// 调拨单行：某物资的申请数量。
public class DispatchLine {
  public long id;
  public long dispatchOrderId;
  public long supplyItemId;
  public int quantity;

  public DispatchLine() {}

  public DispatchLine(long id, long dispatchOrderId, long supplyItemId, int quantity) {
    this.id = id;
    this.dispatchOrderId = dispatchOrderId;
    this.supplyItemId = supplyItemId;
    this.quantity = quantity;
  }
}
