package com.generated.rescueStock.models;

import java.time.LocalDateTime;

/** 库存流水（与预占/实扣双写留痕） */
public class InventoryTransaction {
  public Long id;
  public Long batchId;
  public Long orderId;
  public Long supplyItemId;
  public Long warehouseId;
  public String changeType;
  public Integer changeQuantity;
  public Integer balanceAfter;
  public Integer reservedAfter;
  public LocalDateTime createdAt;
  public String actor;
}
