package com.generated.rescueStock.models;

// 库存批次：quantity 为账面库存，reservedQuantity 为调拨预占量，
// 可用量 = quantity - reservedQuantity，仅 QUALIFIED 批次参与预占分配。
public class InventoryBatch {
  public long id;
  public long warehouseId;
  public long supplyItemId;
  public String batchNo;
  public int quantity;
  public int reservedQuantity;
  public String expireAt;
  public String inboundSource;
  public String qualityStatus;

  public InventoryBatch() {}

  public InventoryBatch(long id, long warehouseId, long supplyItemId, String batchNo, int quantity,
      int reservedQuantity, String expireAt, String inboundSource, String qualityStatus) {
    this.id = id;
    this.warehouseId = warehouseId;
    this.supplyItemId = supplyItemId;
    this.batchNo = batchNo;
    this.quantity = quantity;
    this.reservedQuantity = reservedQuantity;
    this.expireAt = expireAt;
    this.inboundSource = inboundSource;
    this.qualityStatus = qualityStatus;
  }

  public int available() {
    return quantity - reservedQuantity;
  }
}
