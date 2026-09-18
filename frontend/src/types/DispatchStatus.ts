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
