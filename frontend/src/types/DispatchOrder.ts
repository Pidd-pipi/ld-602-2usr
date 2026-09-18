export interface DispatchLine {
  id: number;
  supply_item_id: number;
  supply_item_name?: string;
  sku_code?: string;
  unit?: string;
  quantity: number;
  reserved_quantity: number;
  shortage_quantity: number;
}

export interface DispatchReservation {
  id: number;
  line_id: number;
  batch_id: number;
  batch_no?: string;
  expire_at?: string;
  batch_quantity?: number;
  batch_reserved_quantity?: number;
  batch_available_quantity?: number;
  quantity: number;
  status: "RESERVED" | "CONSUMED" | "RELEASED" | string;
  created_at?: string;
  consumed_at?: string;
  released_at?: string;
}

export interface DispatchOrder {
  id: number;
  event_id: number | null;
  source_warehouse_id: number;
  shelter_id: number;
  priority: string;
  status: string;
  requested_by: string;
  approved_by?: string;
  dispatched_at?: string;
  reject_reason?: string;
  cancel_reason?: string;
  created_at?: string;
  submitted_at?: string;
  approved_at?: string;
  received_at?: string;
  rejected_at?: string;
  cancelled_at?: string;
  /** 列表聚合字段 */
  requested_quantity?: number;
  reserved_quantity?: number;
  shortage_quantity?: number;
  /** 详情字段 */
  lines?: DispatchLine[];
  reservations?: DispatchReservation[];
  warehouse?: { id: number; name: string; district: string };
  shelter?: { id: number; name: string; district: string };
}

/** 审批预占不足时后端返回的缺口明细 */
export interface DispatchShortage {
  line_id: number;
  supply_item_id: number;
  supply_item_name: string;
  requested_quantity: number;
  available_quantity: number;
  shortage_quantity: number;
}
