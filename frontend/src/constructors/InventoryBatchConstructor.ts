import type { InventoryBatch } from "../types/InventoryBatch";

export const createDefaultInventoryBatch = (overrides: Partial<InventoryBatch> = {}): InventoryBatch => ({
  id: 0,
  warehouse_id: 0,
  supply_item_id: 0,
  batch_no: "",
  quantity: 0,
  reserved_quantity: 0,
  available_quantity: 0,
  expire_at: "",
  inbound_source: "",
  quality_status: "QUALIFIED",
  ...overrides
});

export const createInventoryBatchForm = createDefaultInventoryBatch;
export const createInventoryBatchResponse = createDefaultInventoryBatch;
