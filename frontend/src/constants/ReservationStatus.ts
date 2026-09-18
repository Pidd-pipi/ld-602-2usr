/** 批次预占明细状态：已预占 / 已转实扣 / 已释放 */
export const ReservationStatus = ["RESERVED", "CONSUMED", "RELEASED"] as const;
export type ReservationStatus = (typeof ReservationStatus)[number];
export const ReservationStatusText: Record<ReservationStatus, string> = {
  RESERVED: "预占中",
  CONSUMED: "已实扣",
  RELEASED: "已释放"
};

/** 库存流水类型 */
export const TransactionType = ["RESERVE", "RELEASE", "CONSUME"] as const;
export type TransactionType = (typeof TransactionType)[number];
export const TransactionTypeText: Record<TransactionType, string> = {
  RESERVE: "预占",
  RELEASE: "释放",
  CONSUME: "实扣"
};
