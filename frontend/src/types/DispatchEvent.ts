import type { DispatchOrder } from "./DispatchOrder";

// 灾害事件：详情页聚合其下调拨单，与调拨页共用同一份预占数据。
export interface DispatchEvent {
  id: number;
  name: string;
  disaster_type: string;
  level: string;
  status: string;
  occurred_at: string;
  description: string;
  orders?: DispatchOrder[];
}
