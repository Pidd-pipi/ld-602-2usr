package com.generated.rescueStock.types;

/** 调拨单行入参：物资 + 申请数量 */
public record DispatchLinePayload(Long supplyItemId, Integer quantity) {
}
