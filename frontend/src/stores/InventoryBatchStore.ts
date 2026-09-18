import { defineStore } from "pinia";
import { listInventoryBatch } from "../api/InventoryBatch";
import type { InventoryBatch } from "../types/InventoryBatch";

export const useInventoryBatchStore = defineStore("inventoryBatch", {
  state: () => ({ rows: [] as InventoryBatch[], loading: false }),
  getters: {
    // 全仓合格批次可用总量，用于调拨页“可用”汇总卡片。
    totalAvailable: (state) =>
      state.rows
        .filter((row) => row.quality_status === "QUALIFIED")
        .reduce((sum, row) => sum + (row.available_quantity ?? 0), 0),
    totalReserved: (state) => state.rows.reduce((sum, row) => sum + (row.reserved_quantity ?? 0), 0)
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
