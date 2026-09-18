import type { DispatchOrder } from "./DispatchOrder";

export interface DisasterEvent {
  id: number;
  name: string;
  event_type: string;
  district: string;
  level: string;
  occurred_at: string;
  status: string;
  description: string;
}

export interface DisasterEventDetail extends DisasterEvent {
  dispatch_orders: DispatchOrder[];
  total_requested_quantity: number;
  total_reserved_quantity: number;
  total_shortage_quantity: number;
}
