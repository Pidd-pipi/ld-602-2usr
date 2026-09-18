// 审批失败时后端返回的缺口明细（HTTP 409 details.shortages）。
export interface StockShortage {
  supply_item_id: number;
  supply_item_name?: string;
  requested_quantity: number;
  available_quantity: number;
  shortage_quantity: number;
}
