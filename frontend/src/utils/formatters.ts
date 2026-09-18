import { DispatchStatusText } from "../constants/DispatchStatus";
import { ReservationStatusText, TransactionTypeText } from "../constants/ReservationStatus";

export const formatDate = (value?: string) =>
  value ? new Date(value).toLocaleString("zh-CN") : "-";

/** 状态文本：优先匹配调拨/预占/流水中文案，缺省回退英文枚举 */
export const formatStatus = (value: string) =>
  DispatchStatusText[value as keyof typeof DispatchStatusText]
  ?? ReservationStatusText[value as keyof typeof ReservationStatusText]
  ?? TransactionTypeText[value as keyof typeof TransactionTypeText]
  ?? value.replace(/_/g, " ");

export const formatNumber = (value?: number) =>
  new Intl.NumberFormat("zh-CN").format(value ?? 0);

export const formatRisk = (value: string) =>
  ({ LOW: "低", MEDIUM: "中", HIGH: "高", CRITICAL: "严重", EXTREME: "极高" }[value] ?? value);

/** 可用量着色口径：0 紧张，低于安全值提醒 */
export const availableLevel = (available: number, safety = 0) =>
  available <= 0 ? "none" : available <= safety ? "low" : "ok";
