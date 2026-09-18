import { defineStore } from "pinia";
import { listInventoryBatch } from "../api/InventoryBatch";
import type { InventoryBatch } from "../types/InventoryBatch";

interface State {
  rows: InventoryBatch[];
  loading: boolean;
}

export const useInventoryBatchStore = defineStore("inventoryBatch", {
  state: (): State => ({ rows: [], loading: false }),
  getters: {
    /** 调拨页汇总：总库存 / 已预占 / 可用 */
    totals(state) {
      return {
        quantity: state.rows.reduce((sum, b) => sum + b.quantity, 0),
        reserved: state.rows.reduce((sum, b) => sum + b.reserved_quantity, 0),
        available: state.rows.reduce((sum, b) => sum + b.available_quantity, 0)
      };
    }
  },
  actions: {
    async load(warehouseId?: number) {
      this.loading = true;
      try {
        this.rows = await listInventoryBatch(warehouseId);
      } finally {
        this.loading = false;
      }
    }
  }
});
