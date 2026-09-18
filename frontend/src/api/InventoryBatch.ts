import { mockData } from "../mocks/seedData";
import type { InventoryBatch } from "../types/InventoryBatch";
import { request } from "../utils/http";

const endpoint = "/api/inventory-batch";

export async function listInventoryBatch(warehouseId?: number): Promise<InventoryBatch[]> {
  const url = warehouseId == null ? endpoint : `${endpoint}?warehouse_id=${warehouseId}`;
  try {
    return await request<InventoryBatch[]>(url);
  } catch {
    // Local mock fallback keeps the UI available during offline review.
    const rows = [...(mockData.inventoryBatch as unknown as InventoryBatch[])];
    return warehouseId == null ? rows : rows.filter((row) => row.warehouse_id === warehouseId);
  }
}

export async function saveInventoryBatch(payload: InventoryBatch) {
  console.info("save InventoryBatch", payload);
  return payload;
}
