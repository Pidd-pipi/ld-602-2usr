package com.generated.rescueStock.models;

public class SupplyItem {
  public long id;
  public String skuCode;
  public String name;
  public String category;
  public String unit;
  public int safetyStock;
  public int expireDays;
  public String storageRequirement;

  public SupplyItem() {}

  public SupplyItem(long id, String skuCode, String name, String category, String unit, int safetyStock,
      int expireDays, String storageRequirement) {
    this.id = id;
    this.skuCode = skuCode;
    this.name = name;
    this.category = category;
    this.unit = unit;
    this.safetyStock = safetyStock;
    this.expireDays = expireDays;
    this.storageRequirement = storageRequirement;
  }
}
