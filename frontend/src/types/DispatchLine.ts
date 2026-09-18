// 调拨单行：请求量、已预占、已实扣、当前可用与缺口均由服务端按批次实时计算。
export interface DispatchLine {
  id: number;
  dispatch_order_id: number;
  supply_item_id: number;
  supply_item_name?: string;
  unit?: string;
  requested_quantity: number;
  reserved_quantity: number;
  consumed_quantity: number;
  available_quantity: number;
  shortage_quantity: number;
}
