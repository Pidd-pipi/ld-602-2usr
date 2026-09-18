import { mockData } from "../mocks/seedData";
import { getJson, ApiError } from "./request";
import type { InventoryBatch } from "../types/InventoryBatch";

const endpoint = "/api/inventory-batch";

export async function listInventoryBatch(warehouseId?: number): Promise<InventoryBatch[]> {
  const url = warehouseId ? `${endpoint}?warehouse_id=${warehouseId}` : endpoint;
  try {
    return await getJson<InventoryBatch[]>(url);
  } catch (err) {
    if (err instanceof ApiError) throw err;
    return [...(mockData.inventoryBatch as unknown as InventoryBatch[])];
  }
}

export interface BatchAvailability {
  warehouse_id: number;
  supply_item_id: number;
  supply_item_name: string;
  quantity: number;
  reserved_quantity: number;
  available_quantity: number;
  batches: InventoryBatch[];
}

export async function getBatchAvailability(warehouseId: number, supplyItemId: number): Promise<BatchAvailability> {
  return getJson<BatchAvailability>(
    `${endpoint}/availability?warehouse_id=${warehouseId}&supply_item_id=${supplyItemId}`);
}
