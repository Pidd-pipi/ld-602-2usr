<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useWarehouseStore } from "../stores/WarehouseStore";
import { useInventoryBatchStore } from "../stores/InventoryBatchStore";
import BatchTable from "../components/common/BatchTable.vue";
import ExpireWarningList from "../components/common/ExpireWarningList.vue";
import { formatNumber } from "../utils/formatters";
import type { InventoryBatch } from "../types/InventoryBatch";

const warehouseStore = useWarehouseStore();
const batchStore = useInventoryBatchStore();
const selectedId = ref<number | null>(null);

onMounted(async () => {
  await warehouseStore.load();
  selectedId.value = warehouseStore.rows[0]?.id ?? null;
});

watch(selectedId, (id) => {
  if (id) batchStore.load(id);
});

const currentWarehouse = computed(() => warehouseStore.rows.find((w) => w.id === selectedId.value));
const batches = computed(() => batchStore.rows);
const totals = computed(() => batchStore.totals);

// 本仓临期：90 天内到期（FEFO 口径同源）
const expiring = computed<InventoryBatch[]>(() => {
  const cutoff = Date.now() + 90 * 24 * 60 * 60 * 1000;
  return batches.value
    .filter((b) => new Date(b.expire_at).getTime() <= cutoff)
    .sort((a, b) => new Date(a.expire_at).getTime() - new Date(b.expire_at).getTime());
});
</script>

<template>
  <section class="warehouses-page">
    <div class="page-head">
      <div>
        <p class="eyebrow">WAREHOUSES · 批次库存</p>
        <h2>仓库库存</h2>
      </div>
      <select v-model="selectedId">
        <option v-for="w in warehouseStore.rows" :key="w.id" :value="w.id">{{ w.name }}</option>
      </select>
    </div>

    <div class="metrics" v-if="currentWarehouse">
      <div class="stat"><span>容量等级</span><strong>{{ currentWarehouse.capacity_level }} 级</strong></div>
      <div class="stat"><span>批次总量</span><strong>{{ formatNumber(totals.quantity) }}</strong></div>
      <div class="stat"><span>已预占</span><strong class="reserved">{{ formatNumber(totals.reserved) }}</strong></div>
      <div class="stat"><span>可用库存</span><strong class="available">{{ formatNumber(totals.available) }}</strong></div>
    </div>

    <div class="grid">
      <div class="panel">
        <h3>批次明细（可用 = 总量 - 预占）</h3>
        <BatchTable :batches="batches" :loading="batchStore.loading" />
      </div>
      <div class="panel">
        <h3>临期预警（90 天内）</h3>
        <ExpireWarningList :batches="expiring" />
      </div>
    </div>
  </section>
</template>

<style scoped>
.warehouses-page { display: grid; gap: 16px; }
.eyebrow { margin: 0; color: #7d4d18; font-size: 11px; font-weight: 800; letter-spacing: .08em; }
h2 { margin: 4px 0; font-size: 24px; }
.page-head { display: flex; justify-content: space-between; align-items: flex-end; }
select { border: 1px solid #c9c4b4; border-radius: 6px; padding: 8px 10px; font: inherit; }
.metrics { display: grid; grid-template-columns: repeat(4, minmax(0,1fr)); gap: 12px; }
.stat { background: #fbfaf4; border: 1px solid #d8d6c8; border-radius: 8px; padding: 14px 16px; display: grid; gap: 4px; }
.stat span { color: #596257; font-size: 12px; }
.stat strong { font-size: 22px; color: #274335; }
.reserved { color: #1d3f91 !important; }
.available { color: #244b31 !important; }
.grid { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; align-items: start; }
.panel { background: #fff; border: 1px solid #d8d6c8; border-radius: 10px; padding: 18px; }
h3 { margin: 0 0 12px; font-size: 16px; }
@media (max-width: 1000px) { .grid, .metrics { grid-template-columns: 1fr 1fr; } }
</style>
