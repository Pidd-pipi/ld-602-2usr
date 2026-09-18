import { defineStore } from "pinia";
import { getDispatchEvent, listDispatchEvent } from "../api/DispatchEvent";
import { mockData } from "../mocks/seedData";
import type { DispatchEvent } from "../types/DispatchEvent";
import type { DispatchOrder } from "../types/DispatchOrder";

export const useDispatchEventStore = defineStore("dispatchEvent", {
  state: () => ({
    rows: [] as DispatchEvent[],
    current: null as DispatchEvent | null,
    loading: false
  }),
  actions: {
    async load() {
      this.loading = true;
      try {
        this.rows = await listDispatchEvent();
      } finally {
        this.loading = false;
      }
    },
    async loadDetail(id: number) {
      try {
        this.current = await getDispatchEvent(id);
      } catch {
        // 离线兜底：用本地种子数据拼装事件详情，保持页面可演示。
        const event = (mockData.dispatchEvent as unknown as DispatchEvent[]).find((row) => row.id === id);
        const orders = (mockData.dispatchOrder as unknown as DispatchOrder[]).filter((row) => row.event_id === id);
        this.current = event ? { ...event, orders } : null;
      }
    }
  }
});
