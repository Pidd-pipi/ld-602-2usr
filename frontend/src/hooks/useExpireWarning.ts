import { computed, type Ref } from "vue";
import type { InventoryBatch } from "../types/InventoryBatch";

/**
 * 临期预警：按到期日升序（与 FEFO 分配口径一致），
 * 90 天内到期视为临期；顺带暴露每个批次的可用量（总量 - 预占）。
 */
export function useExpireWarning(batches: Ref<InventoryBatch[]> | InventoryBatch[], withinDays = 90) {
  const list = computed<InventoryBatch[]>(() => {
    const rows = Array.isArray(batches) ? batches : batches.value;
    const cutoff = Date.now() + withinDays * 24 * 60 * 60 * 1000;
    return [...rows]
      .filter((b) => {
        const t = new Date(b.expire_at).getTime();
        return Number.isFinite(t) && t <= cutoff;
      })
      .sort((a, b) => new Date(a.expire_at).getTime() - new Date(b.expire_at).getTime());
  });

  const count = computed(() => list.value.length);

  return { expiringBatches: list, count };
}
