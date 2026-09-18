import type { CreateDispatchOrderPayload } from "../api/DispatchOrder";
import type { DispatchOrder } from "../types/DispatchOrder";

export const createDefaultDispatchOrder = (overrides: Partial<DispatchOrder> = {}): DispatchOrder => ({
  id: 0,
  event_id: 1,
  source_warehouse_id: 1,
  shelter_id: 1,
  priority: "MEDIUM",
  status: "DRAFT",
  requested_by: "",
  approved_by: null,
  dispatched_at: null,
  created_at: "",
  lines: [],
  total_requested: 0,
  total_reserved: 0,
  total_available: 0,
  total_shortage: 0,
  ...overrides
});

// 新建调拨单表单：默认一条物资行，页面只允许在此基础上增删。
export const createDispatchOrderForm = (overrides: Partial<CreateDispatchOrderPayload> = {}): CreateDispatchOrderPayload => ({
  event_id: 1,
  source_warehouse_id: 1,
  shelter_id: 1,
  priority: "MEDIUM",
  requested_by: "",
  lines: [{ supply_item_id: 1, quantity: 1 }],
  ...overrides
});

export const createDispatchOrderResponse = createDefaultDispatchOrder;
