export interface InventoryBatch {
  id: number;
  warehouse_id: number;
  warehouse_name?: string;
  supply_item_id: number;
  supply_item_name?: string;
  batch_no: string;
  quantity: number;
  reserved_quantity: number;
  available_quantity: number;
  expire_at: string;
  inbound_source: string;
  quality_status: string;
}
