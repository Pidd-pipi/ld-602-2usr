// 调拨优先级：创建调拨单时选择。
export const DispatchPriority = ["HIGH", "MEDIUM", "LOW"] as const;
export type DispatchPriority = (typeof DispatchPriority)[number];
export const DispatchPriorityText: Record<DispatchPriority, string> = {
  HIGH: "紧急",
  MEDIUM: "常规",
  LOW: "一般"
};
