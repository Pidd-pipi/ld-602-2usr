package com.generated.rescueStock.models;

import java.util.ArrayList;
import java.util.List;

// 调拨单：聚合 DispatchLine，审批时预占 InventoryBatch。
public class DispatchOrder {
  public long id;
  public long eventId;
  public long sourceWarehouseId;
  public long shelterId;
  public String priority;
  public String status; // DispatchStatus
  public String requestedBy;
  public String approvedBy;
  public String dispatchedAt;
  public String createdAt;
  public List<DispatchLine> lines = new ArrayList<>();
  public List<DispatchStatusEvent> history = new ArrayList<>();

  public DispatchOrder() {}

  public DispatchOrder(long id, long eventId, long sourceWarehouseId, long shelterId, String priority,
      String status, String requestedBy, String approvedBy, String dispatchedAt, String createdAt) {
    this.id = id;
    this.eventId = eventId;
    this.sourceWarehouseId = sourceWarehouseId;
    this.shelterId = shelterId;
    this.priority = priority;
    this.status = status;
    this.requestedBy = requestedBy;
    this.approvedBy = approvedBy;
    this.dispatchedAt = dispatchedAt;
    this.createdAt = createdAt;
  }
}
