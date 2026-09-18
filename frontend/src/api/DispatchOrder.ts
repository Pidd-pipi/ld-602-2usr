import { mockData } from "../mocks/seedData";
import type { DispatchOrder } from "../types/DispatchOrder";
import { postJson, request } from "../utils/http";

const endpoint = "/api/dispatch-order";

// 调拨单流转动作，与后端 DispatchOrderRoutes 一一对应。
export type DispatchAction = "submit" | "approve" | "reject" | "cancel" | "dispatch" | "receive";

export interface CreateDispatchOrderLine {
  supply_item_id: number;
  quantity: number;
}

export interface CreateDispatchOrderPayload {
  event_id: number;
  source_warehouse_id: number;
  shelter_id: number;
  priority: string;
  requested_by: string;
  lines: CreateDispatchOrderLine[];
}

export async function listDispatchOrder(): Promise<DispatchOrder[]> {
  try {
    return await request<DispatchOrder[]>(endpoint);
  } catch {
    // Local mock fallback keeps the UI available during offline review.
    return [...(mockData.dispatchOrder as unknown as DispatchOrder[])];
  }
}

export function getDispatchOrder(id: number): Promise<DispatchOrder> {
  return request<DispatchOrder>(`${endpoint}/${id}`);
}

export function createDispatchOrder(payload: CreateDispatchOrderPayload): Promise<DispatchOrder> {
  return postJson<DispatchOrder>(endpoint, payload);
}

// 审批/出库/取消等动作：库存不足时抛出带 shortages 明细的 ApiError。
export function actOnDispatchOrder(id: number, action: DispatchAction, operator: string): Promise<DispatchOrder> {
  return postJson<DispatchOrder>(`${endpoint}/${id}/${action}`, { operator });
}
