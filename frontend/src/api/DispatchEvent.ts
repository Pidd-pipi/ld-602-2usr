import { mockData } from "../mocks/seedData";
import type { DispatchEvent } from "../types/DispatchEvent";
import { request } from "../utils/http";

const endpoint = "/api/dispatch-event";

export async function listDispatchEvent(): Promise<DispatchEvent[]> {
  try {
    return await request<DispatchEvent[]>(endpoint);
  } catch {
    // Local mock fallback keeps the UI available during offline review.
    return [...(mockData.dispatchEvent as unknown as DispatchEvent[])];
  }
}

// 事件详情：后端聚合该事件下调拨单的预占/可用/缺口，与调拨页同源。
export function getDispatchEvent(id: number): Promise<DispatchEvent> {
  return request<DispatchEvent>(`${endpoint}/${id}`);
}
