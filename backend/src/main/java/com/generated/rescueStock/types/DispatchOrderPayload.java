package com.generated.rescueStock.types;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

// 创建调拨单请求体：聚合多条物资申请行。
public record DispatchOrderPayload(
    @JsonProperty("event_id") long eventId,
    @JsonProperty("source_warehouse_id") long sourceWarehouseId,
    @JsonProperty("shelter_id") long shelterId,
    @JsonProperty("priority") String priority,
    @JsonProperty("requested_by") String requestedBy,
    @JsonProperty("lines") List<Line> lines) {

  public record Line(
      @JsonProperty("supply_item_id") long supplyItemId,
      @JsonProperty("quantity") int quantity) {}
}
