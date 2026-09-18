package com.generated.rescueStock.models;

import java.time.LocalDateTime;

/** 调拨单聚合根 */
public class DispatchOrder {
  public Long id;
  public Long eventId;
  public Long sourceWarehouseId;
  public Long shelterId;
  public String priority;
  public String status;
  public String requestedBy;
  public String approvedBy;
  public LocalDateTime createdAt;
  public LocalDateTime updatedAt;
  public LocalDateTime submittedAt;
  public LocalDateTime approvedAt;
  public LocalDateTime dispatchedAt;
  public LocalDateTime receivedAt;
  public LocalDateTime rejectedAt;
  public LocalDateTime cancelledAt;
  public String rejectReason;
  public String cancelReason;
  public Integer version;
}
