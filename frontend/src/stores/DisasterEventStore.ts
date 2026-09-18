import { defineStore } from "pinia";
import { getDisasterEvent, listDisasterEvent } from "../api/DisasterEvent";
import type { DisasterEvent, DisasterEventDetail } from "../types/DisasterEvent";

interface State {
  rows: DisasterEvent[];
  current: DisasterEventDetail | null;
  loading: boolean;
}

export const useDisasterEventStore = defineStore("disasterEvent", {
  state: (): State => ({ rows: [], current: null, loading: false }),
  actions: {
    async load() {
      this.loading = true;
      try {
        this.rows = await listDisasterEvent();
      } finally {
        this.loading = false;
      }
    },
    /** 事件详情同步关联调拨单的预占/缺口 */
    async loadDetail(id: number) {
      this.loading = true;
      try {
        this.current = await getDisasterEvent(id);
      } finally {
        this.loading = false;
      }
      return this.current;
    }
  }
});
