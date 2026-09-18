package com.generated.rescueStock.models;

/** 库存批次：quantity 为批次总量，reservedQuantity 为已预占量，可用 = quantity - reservedQuantity */
public class InventoryBatch {
  public Long id;
  public Long warehouseId;
  public Long supplyItemId;
  public String batchNo;
  public Integer quantity;
  public Integer reservedQuantity;
  public java.time.LocalDateTime expireAt;
  public String inboundSource;
  public String qualityStatus;
  public Integer version;
}
