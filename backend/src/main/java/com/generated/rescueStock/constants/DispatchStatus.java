package com.generated.rescueStock.constants;

// 调拨单状态机：DRAFT -> SUBMITTED -> APPROVED(已预占) -> DISPATCHED(预占转实扣) -> RECEIVED
// 驳回(REJECTED)/取消(CANCELLED) 会释放全部预占。
public enum DispatchStatus { DRAFT, SUBMITTED, APPROVED, DISPATCHED, RECEIVED, REJECTED, CANCELLED }
