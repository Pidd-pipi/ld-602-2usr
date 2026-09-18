import { defineStore } from "pinia";
import {
  approveDispatchOrder,
  cancelDispatchOrder,
  createDispatchOrder,
  getDispatchOrder,
  listDispatchOrder,
  outboundDispatchOrder,
  receiveDispatchOrder,
  rejectDispatchOrder,
  type CreateDispatchInput
} from "../api/DispatchOrder";
import { ApiError } from "../api/request";
import type { DispatchOrder, DispatchShortage } from "../types/DispatchOrder";

interface State {
  rows: DispatchOrder[];
  current: DispatchOrder | null;
  loading: boolean;
  acting: boolean;
  error: string;
  /** 最近一次审批缺口（RESERVATION_SHORTAGE 时回填，整单失败不占任何批次） */
  shortages: DispatchShortage[];
}

export const useDispatchOrderStore = defineStore("dispatchOrder", {
  state: (): State => ({
    rows: [],
    current: null,
    loading: false,
    acting: false,
    error: "",
    shortages: []
  }),
  actions: {
    async load() {
      this.loading = true;
      this.error = "";
      try {
        this.rows = await listDispatchOrder();
      } catch (err) {
        this.error = err instanceof Error ? err.message : String(err);
      } finally {
        this.loading = false;
      }
    },

    /** 刷新后仍能回读：按 id 重新拉详情（含预占明细与行缺口） */
    async loadDetail(id: number) {
      this.loading = true;
      this.error = "";
      try {
        this.current = await getDispatchOrder(id);
        this.shortages = [];
      } catch (err) {
        this.error = err instanceof Error ? err.message : String(err);
        this.current = null;
      } finally {
        this.loading = false;
      }
      return this.current;
    },

    async create(payload: CreateDispatchInput) {
      this.acting = true;
      this.error = "";
      try {
        const created = await createDispatchOrder(payload);
        await this.load();
        return created;
      } finally {
        this.acting = false;
      }
    },

    /** 审批通过（FEFO 预占）。缺口时抛出 ApiError，页面读取 .shortages 展示 */
    async approve(id: number, approvedBy: string) {
      return this.runAction(() => approveDispatchOrder(id, approvedBy), id);
    },

    async confirmOutbound(id: number, operator: string) {
      return this.runAction(() => outboundDispatchOrder(id, operator), id);
    },

    async reject(id: number, approvedBy: string, reason: string) {
      return this.runAction(() => rejectDispatchOrder(id, approvedBy, reason), id);
    },

    async cancel(id: number, operator: string, reason: string) {
      return this.runAction(() => cancelDispatchOrder(id, operator, reason), id);
    },

    async receive(id: number, operator: string) {
      return this.runAction(() => receiveDispatchOrder(id, operator), id);
    },

    async runAction(action: () => Promise<DispatchOrder>, detailId: number) {
      this.acting = true;
      this.error = "";
      this.shortages = [];
      try {
        const result = await action();
        this.current = result;
        await this.load();
        // 列表刷新后再回读详情，保证“刷新后仍能回读”
        await this.loadDetail(detailId);
        return result;
      } catch (err) {
        if (err instanceof ApiError) {
          this.error = err.message;
          if (err.code === "RESERVATION_SHORTAGE" && Array.isArray(err.shortages)) {
            this.shortages = err.shortages as DispatchShortage[];
          }
        } else {
          this.error = err instanceof Error ? err.message : String(err);
        }
        throw err;
      } finally {
        this.acting = false;
      }
    },

    clearError() {
      this.error = "";
      this.shortages = [];
    }
  }
});
