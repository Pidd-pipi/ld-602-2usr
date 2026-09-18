import { defineStore } from "pinia";
import { listWarehouse } from "../api/Warehouse";
import type { Warehouse } from "../types/Warehouse";

export const useWarehouseStore = defineStore("warehouse", {
  state: () => ({ rows: [] as Warehouse[], loading: false }),
  actions: {
    async load() {
      this.loading = true;
      try {
        this.rows = await listWarehouse();
      } finally {
        this.loading = false;
      }
    }
  }
});
