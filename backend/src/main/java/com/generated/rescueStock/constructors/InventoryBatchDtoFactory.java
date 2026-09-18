package com.generated.rescueStock.constructors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.SupplyItem;

/** 批次响应构造器：附带 available_quantity = quantity - reserved_quantity（调拨页展示可用/预占） */
public final class InventoryBatchDtoFactory {

  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private InventoryBatchDtoFactory() {}

  public static Map<String, Object> toDto(InventoryBatch batch) {
    return toDto(batch, null);
  }

  public static Map<String, Object> toDto(InventoryBatch batch, SupplyItem item) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", batch.id);
    dto.put("warehouse_id", batch.warehouseId);
    dto.put("supply_item_id", batch.supplyItemId);
    dto.put("batch_no", batch.batchNo);
    dto.put("quantity", batch.quantity);
    dto.put("reserved_quantity", batch.reservedQuantity);
    dto.put("available_quantity", batch.quantity - batch.reservedQuantity);
    dto.put("expire_at", fmt(batch.expireAt));
    dto.put("inbound_source", batch.inboundSource);
    dto.put("quality_status", batch.qualityStatus);
    dto.put("version", batch.version);
    if (item != null) {
      dto.put("supply_item_name", item.name);
      dto.put("sku_code", item.skuCode);
      dto.put("category", item.category);
      dto.put("unit", item.unit);
    }
    return dto;
  }

  private static String fmt(LocalDateTime time) {
    return time == null ? null : FMT.format(time);
  }
}
