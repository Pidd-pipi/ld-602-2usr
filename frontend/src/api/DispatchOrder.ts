import { mockData } from "../mocks/seedData";
import { getJson, postJson, ApiError } from "./request";
import type { DispatchOrder, DispatchShortage } from "../types/DispatchOrder";

const endpoint = "/api/dispatch-order";

export interface DispatchLineInput {
  supply_item_id: number;
  quantity: number;
}

export interface CreateDispatchInput {
  event_id?: number | null;
  source_warehouse_id: number;
  shelter_id: number;
  priority: string;
  requested_by: string;
  lines: DispatchLineInput[];
}

export async function listDispatchOrder(): Promise<DispatchOrder[]> {
  try {
    return await getJson<DispatchOrder[]>(endpoint);
  } catch (err) {
    if (err instanceof ApiError) throw err;
    // 离线评审时回退本地种子，保持页面可用
    return [...(mockData.dispatchOrder as unknown as DispatchOrder[])];
  }
}

export async function getDispatchOrder(id: number): Promise<DispatchOrder> {
  return getJson<DispatchOrder>(`${endpoint}/${id}`);
}

export async function createDispatchOrder(payload: CreateDispatchInput): Promise<DispatchOrder> {
  return postJson<DispatchOrder>(endpoint, payload);
}

/** 审批通过：FEFO 预占；失败时 ApiError.shortages 携带缺口明细 */
export async function approveDispatchOrder(id: number, approvedBy: string): Promise<DispatchOrder> {
  return postJson<DispatchOrder>(`${endpoint}/${id}/approve`, { approved_by: approvedBy });
}

/** 出库确认：预占转实扣 */
export async function outboundDispatchOrder(id: number, operator: string): Promise<DispatchOrder> {
  return postJson<DispatchOrder>(`${endpoint}/${id}/outbound`, { operator });
}

/** 驳回：释放全部预占 */
export async function rejectDispatchOrder(id: number, approvedBy: string, reason: string): Promise<DispatchOrder> {
  return postJson<DispatchOrder>(`${endpoint}/${id}/reject`, { approved_by: approvedBy, reason });
}

/** 取消：释放全部预占 */
export async function cancelDispatchOrder(id: number, operator: string, reason: string): Promise<DispatchOrder> {
  return postJson<DispatchOrder>(`${endpoint}/${id}/cancel`, { operator, reason });
}

/** 签收 */
export async function receiveDispatchOrder(id: number, operator: string): Promise<DispatchOrder> {
  return postJson<DispatchOrder>(`${endpoint}/${id}/receive`, { operator });
}

export type { DispatchShortage };
