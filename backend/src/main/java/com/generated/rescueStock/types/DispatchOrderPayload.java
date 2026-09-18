package com.generated.rescueStock.types;

import java.util.List;

/** 创建调拨单入参 */
public record DispatchOrderPayload(
    Long eventId,
    Long sourceWarehouseId,
    Long shelterId,
    String priority,
    String requestedBy,
    List<DispatchLinePayload> lines) {
}
