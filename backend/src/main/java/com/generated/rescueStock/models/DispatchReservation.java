package com.generated.rescueStock.models;

import java.time.LocalDateTime;

/** 批次预占明细：审批写入 RESERVED，出库转 CONSUMED，驳回/取消转 RELEASED */
public class DispatchReservation {
  public Long id;
  public Long orderId;
  public Long lineId;
  public Long batchId;
  public Integer quantity;
  public String status;
  public LocalDateTime createdAt;
  public LocalDateTime consumedAt;
  public LocalDateTime releasedAt;
}
