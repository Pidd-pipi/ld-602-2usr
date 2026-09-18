import { defineStore } from "pinia";
import { actOnDispatchOrder, createDispatchOrder, getDispatchOrder, listDispatchOrder } from "../api/DispatchOrder";
import type { CreateDispatchOrderPayload, DispatchAction } from "../api/DispatchOrder";
import type { DispatchOrder } from "../types/DispatchOrder";
import type { StockShortage } from "../types/StockShortage";
import { ApiError } from "../utils/http";

// 调拨单 store：每个写动作成功后重新拉取列表与详情，保证刷新前后数据一致。
export const useDispatchOrderStore = defineStore("dispatchOrder", {
  state: () => ({
    rows: [] as DispatchOrder[],
    current: null as DispatchOrder | null,
    loading: false,
    acting: false,
    lastError: null as string | null,
    lastShortages: [] as StockShortage[]
  }),
  actions: {
    async load() {
      this.loading = true;
      try {
        this.rows = await listDispatchOrder();
      } finally {
        this.loading = false;
      }
    },
    async loadDetail(id: number) {
      this.current = await getDispatchOrder(id);
    },
    async create(payload: CreateDispatchOrderPayload) {
      this.acting = true;
      this.lastError = null;
      try {
        const created = await createDispatchOrder(payload);
        await this.load();
        this.current = created;
        return created;
      } finally {
        this.acting = false;
      }
    },
    // 提交/审批/驳回/取消/出库/签收统一入口；库存不足时记录缺口明细并抛给页面展示。
    async act(id: number, action: DispatchAction, operator: string) {
      this.acting = true;
      this.lastError = null;
      this.lastShortages = [];
      try {
        const updated = await actOnDispatchOrder(id, action, operator);
        await this.load();
        this.current = updated;
        return updated;
      } catch (error) {
        if (error instanceof ApiError) {
          this.lastError = error.message;
          this.lastShortages = error.shortages;
          await this.load();
          // 并发审批失败等场景下，同步刷新展开中的明细，可用量以服务端为准。
          if (this.current?.id === id) {
            await this.loadDetail(id);
          }
        }
        throw error;
      } finally {
        this.acting = false;
      }
    }
  }
});
