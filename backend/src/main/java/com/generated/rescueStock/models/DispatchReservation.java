package com.generated.rescueStock.models;

// 库存预占记录：审批通过时按批次生成，出库转实扣，驳回/取消释放。
public class DispatchReservation {
  public long id;
  public long dispatchOrderId;
  public long dispatchLineId;
  public long batchId;
  public int quantity;
  public String status; // ReservationStatus
  public String createdAt;

  public DispatchReservation() {}

  public DispatchReservation(long id, long dispatchOrderId, long dispatchLineId, long batchId, int quantity,
      String status, String createdAt) {
    this.id = id;
    this.dispatchOrderId = dispatchOrderId;
    this.dispatchLineId = dispatchLineId;
    this.batchId = batchId;
    this.quantity = quantity;
    this.status = status;
    this.createdAt = createdAt;
  }
}
