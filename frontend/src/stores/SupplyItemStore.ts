import { defineStore } from "pinia";
import { listSupplyItem } from "../api/SupplyItem";
import type { SupplyItem } from "../types/SupplyItem";

export const useSupplyItemStore = defineStore("supplyItem", {
  state: () => ({ rows: [] as SupplyItem[], loading: false }),
  actions: {
    async load() {
      this.loading = true;
      try {
        this.rows = await listSupplyItem();
      } finally {
        this.loading = false;
      }
    }
  }
});
