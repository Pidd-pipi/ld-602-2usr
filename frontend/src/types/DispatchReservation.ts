// 库存预占记录：RESERVED 预占中 / CONSUMED 已实扣 / RELEASED 已释放。
export interface DispatchReservation {
  id: number;
  dispatch_order_id: number;
  dispatch_line_id: number;
  batch_id: number;
  batch_no?: string;
  expire_at?: string;
  quantity: number;
  status: string;
  created_at: string;
}
