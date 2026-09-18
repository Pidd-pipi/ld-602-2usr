// 调拨单状态机：DRAFT -> SUBMITTED -> APPROVED(预占中) -> DISPATCHED(已实扣) -> RECEIVED
// REJECTED / CANCELLED 会释放全部预占。与后端 constants/DispatchStatus.java 保持一致。
export const DispatchStatus = ["DRAFT","SUBMITTED","APPROVED","DISPATCHED","RECEIVED","REJECTED","CANCELLED"] as const;
export type DispatchStatus = (typeof DispatchStatus)[number];
export const DispatchStatusText: Record<DispatchStatus, string> = {
  DRAFT: "草稿",
  SUBMITTED: "待审批",
  APPROVED: "已审批·预占中",
  DISPATCHED: "已出库",
  RECEIVED: "已签收",
  REJECTED: "已驳回",
  CANCELLED: "已取消"
};
