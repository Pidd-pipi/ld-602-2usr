package com.generated.rescueStock.constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import com.generated.rescueStock.models.InventoryBatch;

public final class InventoryBatchDtoFactory {

  private InventoryBatchDtoFactory() {}

  public static Map<String, Object> toDto(InventoryBatch batch, String itemName, String warehouseName) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", batch.id);
    dto.put("warehouse_id", batch.warehouseId);
    dto.put("warehouse_name", warehouseName);
    dto.put("supply_item_id", batch.supplyItemId);
    dto.put("supply_item_name", itemName);
    dto.put("batch_no", batch.batchNo);
    dto.put("quantity", batch.quantity);
    dto.put("reserved_quantity", batch.reservedQuantity);
    dto.put("available_quantity", batch.available());
    dto.put("expire_at", batch.expireAt);
    dto.put("inbound_source", batch.inboundSource);
    dto.put("quality_status", batch.qualityStatus);
    return dto;
  }
}
