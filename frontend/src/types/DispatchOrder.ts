import type { DispatchLine } from "./DispatchLine";
import type { DispatchReservation } from "./DispatchReservation";

export interface DispatchStatusEvent {
  status: string;
  actor: string;
  at: string;
  note: string;
}

export interface DispatchOrder {
  id: number;
  event_id: number;
  event_name?: string;
  source_warehouse_id: number;
  source_warehouse_name?: string;
  shelter_id: number;
  shelter_name?: string;
  priority: string;
  status: string;
  requested_by: string;
  approved_by: string | null;
  dispatched_at: string | null;
  created_at?: string;
  lines: DispatchLine[];
  reservations?: DispatchReservation[];
  history?: DispatchStatusEvent[];
  total_requested: number;
  total_reserved: number;
  total_available: number;
  total_shortage: number;
}
