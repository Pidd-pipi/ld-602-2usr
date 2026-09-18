<script setup lang="ts">
import { computed, onMounted } from "vue";
import { useWarehouseStore } from "../stores/WarehouseStore";
import { useInventoryBatchStore } from "../stores/InventoryBatchStore";
import { useDispatchOrderStore } from "../stores/DispatchOrderStore";
import { useDisasterEventStore } from "../stores/DisasterEventStore";
import StatCard from "../components/common/StatCard.vue";
import StatusBadge from "../components/common/StatusBadge.vue";
import ExpireWarningList from "../components/common/ExpireWarningList.vue";
import { formatNumber } from "../utils/formatters";

const warehouseStore = useWarehouseStore();
const batchStore = useInventoryBatchStore();
const dispatchStore = useDispatchOrderStore();
const eventStore = useDisasterEventStore();

onMounted(async () => {
  await Promise.all([
    warehouseStore.load(),
    batchStore.load(),
    dispatchStore.load(),
    eventStore.load()
  ]);
});

const pendingCount = computed(() => dispatchStore.rows.filter((o) => o.status === "SUBMITTED").length);
const reservedTotal = computed(() =>
  dispatchStore.rows.reduce((sum, o) => sum + (o.reserved_quantity ?? 0), 0));
const shortageTotal = computed(() =>
  dispatchStore.rows.reduce((sum, o) => sum + (o.shortage_quantity ?? 0), 0));
const expiring = computed(() => {
  const cutoff = Date.now() + 90 * 24 * 60 * 60 * 1000;
  return batchStore.rows
    .filter((b) => new Date(b.expire_at).getTime() <= cutoff)
    .sort((a, b) => new Date(a.expire_at).getTime() - new Date(b.expire_at).getTime());
});
</script>

<template>
  <section class="dashboard">
    <div class="metrics">
      <StatCard label="启用仓库" :value="warehouseStore.rows.length" />
      <StatCard label="待审批调拨" :value="pendingCount" />
      <StatCard label="在途预占数量" :value="formatNumber(reservedTotal)" />
      <StatCard label="未满足缺口" :value="formatNumber(shortageTotal)" />
    </div>

    <div class="grid">
      <div class="panel">
        <h3>待办调拨单</h3>
        <ul class="todo">
          <li v-for="o in dispatchStore.rows" :key="o.id">
            <span class="mono">#{{ o.id }}</span>
            <span>仓库{{ o.source_warehouse_id }} → 避难点{{ o.shelter_id }}</span>
            <span class="num">申请 {{ formatNumber(o.requested_quantity ?? 0) }} · 预占 {{ formatNumber(o.reserved_quantity ?? 0) }}</span>
            <StatusBadge :value="o.status" />
          </li>
        </ul>
        <p v-if="!dispatchStore.rows.length" class="muted">暂无调拨单</p>
      </div>
      <div class="panel">
        <h3>临期物资</h3>
        <ExpireWarningList :batches="expiring" />
      </div>
    </div>

    <div class="panel">
      <h3>进行中的灾害事件</h3>
      <ul class="events">
        <li v-for="e in eventStore.rows" :key="e.id">
          <strong>{{ e.name }}</strong>
          <span class="muted">{{ e.district }} · {{ e.occurred_at }}</span>
          <StatusBadge :value="e.status" />
        </li>
      </ul>
    </div>
  </section>
</template>

<style scoped>
.dashboard { display: grid; gap: 16px; }
.metrics { display: grid; grid-template-columns: repeat(4, minmax(0,1fr)); gap: 14px; }
.grid { display: grid; grid-template-columns: 2fr 1fr; gap: 16px; align-items: start; }
.panel { background: #fff; border: 1px solid #d8d6c8; border-radius: 10px; padding: 18px; }
h3 { margin: 0 0 12px; font-size: 16px; }
.todo, .events { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
.todo li { display: grid; grid-template-columns: auto 1fr auto auto; gap: 12px; align-items: center; font-size: 13px; border-bottom: 1px solid #f0eee4; padding-bottom: 8px; }
.events li { display: flex; justify-content: space-between; gap: 10px; align-items: center; font-size: 13px; }
.mono { font-family: ui-monospace, Menlo, monospace; }
.num { color: #596257; }
.muted { color: #8a8578; font-size: 13px; }
@media (max-width: 1000px) { .metrics, .grid { grid-template-columns: 1fr 1fr; } }
</style>
