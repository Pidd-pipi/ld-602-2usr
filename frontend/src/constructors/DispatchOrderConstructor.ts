import type { DispatchOrder, DispatchLine, DispatchReservation } from "../types/DispatchOrder";
import type { CreateDispatchInput, DispatchLineInput } from "../api/DispatchOrder";

/** 详情/列表默认结构：页面与 store 不得散写空对象 */
export const createDefaultDispatchOrder = (overrides: Partial<DispatchOrder> = {}): DispatchOrder => ({
  id: 0,
  event_id: null,
  source_warehouse_id: 0,
  shelter_id: 0,
  priority: "NORMAL",
  status: "SUBMITTED",
  requested_by: "street-admin",
  approved_by: "",
  requested_quantity: 0,
  reserved_quantity: 0,
  shortage_quantity: 0,
  lines: [],
  reservations: [],
  ...overrides
});

export const createDefaultDispatchLine = (overrides: Partial<DispatchLine> = {}): DispatchLine => ({
  id: 0,
  supply_item_id: 0,
  quantity: 0,
  reserved_quantity: 0,
  shortage_quantity: 0,
  ...overrides
});

export const createDefaultReservation =
  (overrides: Partial<DispatchReservation> = {}): DispatchReservation => ({
    id: 0,
    line_id: 0,
    batch_id: 0,
    quantity: 0,
    status: "RESERVED",
    ...overrides
  });

/** 新建调拨单表单：提交审批的请求体必须经过这里构造 */
export const createDispatchOrderForm = (
  fields: {
    eventId?: number | null;
    sourceWarehouseId: number;
    shelterId: number;
    priority?: string;
    requestedBy: string;
    lines: DispatchLineInput[];
  }
): CreateDispatchInput => ({
  event_id: fields.eventId ?? null,
  source_warehouse_id: fields.sourceWarehouseId,
  shelter_id: fields.shelterId,
  priority: fields.priority ?? "NORMAL",
  requested_by: fields.requestedBy,
  lines: fields.lines
});

export const createDispatchOrderResponse = createDefaultDispatchOrder;
