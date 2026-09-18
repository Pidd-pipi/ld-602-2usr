<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useDisasterEventStore } from "../stores/DisasterEventStore";
import { useDispatchOrderStore } from "../stores/DispatchOrderStore";
import { useWarehouseStore } from "../stores/WarehouseStore";
import { useShelterStore } from "../stores/ShelterStore";
import StatusBadge from "../components/common/StatusBadge.vue";
import ApprovalTimeline from "../components/common/ApprovalTimeline.vue";
import EmptyState from "../components/common/EmptyState.vue";
import { formatNumber, formatDate, formatRisk } from "../utils/formatters";
import type { DispatchOrder } from "../types/DispatchOrder";

const eventStore = useDisasterEventStore();
const dispatchStore = useDispatchOrderStore();
const warehouseStore = useWarehouseStore();
const shelterStore = useShelterStore();

const selectedEventId = ref<number | null>(null);

onMounted(async () => {
  await Promise.all([
    eventStore.load(),
    dispatchStore.load(),
    warehouseStore.load(),
    shelterStore.load()
  ]);
  selectedEventId.value = eventStore.rows[0]?.id ?? null;
});

const events = computed(() => eventStore.rows);
const detail = computed(() => eventStore.current);

const warehouseName = (id: number) =>
  warehouseStore.rows.find((w) => w.id === id)?.name ?? `仓库#${id}`;
const shelterName = (id: number) =>
  shelterStore.rows.find((s) => s.id === id)?.name ?? `避难点#${id}`;

async function selectEvent(id: number) {
  selectedEventId.value = id;
  await eventStore.loadDetail(id);
}

/** 事件详情里的调拨单：用 dispatch 详情补全时间线（含预占/缺口/状态） */
function asOrder(o: DispatchOrder): DispatchOrder {
  return o;
}
</script>

<template>
  <section class="events-page">
    <div class="page-head">
      <div>
        <p class="eyebrow">EVENTS · 响应进度</p>
        <h2>灾害事件响应</h2>
        <p class="hint">事件详情同步关联调拨单的预占、实扣与缺口进度。</p>
      </div>
      <button class="btn" @click="eventStore.load()">刷新</button>
    </div>

    <div class="layout">
      <div class="panel event-list">
        <h3>事件列表</h3>
        <button
          v-for="e in events"
          :key="e.id"
          class="event-item"
          :class="{ active: selectedEventId === e.id }"
          @click="selectEvent(e.id)">
          <div class="event-title">
            <strong>{{ e.name }}</strong>
            <StatusBadge :value="e.status" />
          </div>
          <small>{{ e.district }} · {{ formatRisk(e.level) }}风险 · {{ formatDate(e.occurred_at) }}</small>
        </button>
        <EmptyState v-if="!events.length && !eventStore.loading" text="暂无灾害事件" />
      </div>

      <div class="panel event-detail">
        <template v-if="detail">
          <div class="detail-head">
            <div>
              <h3>{{ detail.name }}</h3>
              <p class="muted">{{ detail.description }}</p>
            </div>
            <div class="tags">
              <StatusBadge :value="detail.status" />
              <span class="tag">{{ detail.event_type }}</span>
              <span class="tag risk">{{ formatRisk(detail.level) }}风险</span>
            </div>
          </div>

          <div class="metrics three">
            <div class="stat"><span>申请调拨总量</span><strong>{{ formatNumber(detail.total_requested_quantity) }}</strong></div>
            <div class="stat"><span>已预占/实扣</span><strong class="reserved">{{ formatNumber(detail.total_reserved_quantity) }}</strong></div>
            <div class="stat"><span>当前缺口</span>
              <strong :class="detail.total_shortage_quantity > 0 ? 'shortage-text' : ''">
                {{ formatNumber(detail.total_shortage_quantity) }}
              </strong>
            </div>
          </div>

          <h4>关联调拨单（{{ detail.dispatch_orders.length }}）</h4>
          <EmptyState v-if="!detail.dispatch_orders.length" text="该事件暂未关联调拨单" />
          <div v-for="o in detail.dispatch_orders" :key="o.id" class="order-card">
            <div class="order-head">
              <div>
                <strong class="mono">调拨单 #{{ o.id }}</strong>
                <span class="muted">　{{ warehouseName(o.source_warehouse_id) }} → {{ shelterName(o.shelter_id) }}</span>
              </div>
              <StatusBadge :value="o.status" />
            </div>
            <div class="order-metrics">
              <span>申请 <b>{{ formatNumber(o.requested_quantity ?? 0) }}</b></span>
              <span class="reserved">预占 <b>{{ formatNumber(o.reserved_quantity ?? 0) }}</b></span>
              <span :class="(o.shortage_quantity ?? 0) > 0 ? 'shortage' : 'muted'">
                缺口 <b>{{ formatNumber(o.shortage_quantity ?? 0) }}</b>
              </span>
            </div>
            <!-- 事件详情同步：复用调拨时间线组件 -->
            <ApprovalTimeline :order="asOrder(o)" />
          </div>
        </template>
        <EmptyState v-else text="请选择左侧事件查看响应进度" />
      </div>
    </div>
  </section>
</template>

<style scoped>
.events-page { display: grid; gap: 16px; }
.page-head { display: flex; justify-content: space-between; align-items: flex-end; }
.eyebrow { margin: 0; color: #7d4d18; font-size: 11px; font-weight: 800; letter-spacing: .08em; }
h2 { margin: 4px 0; font-size: 24px; }
h3 { margin: 0 0 12px; font-size: 16px; }
h4 { margin: 18px 0 10px; font-size: 14px; }
.hint { color: #7c837b; font-size: 12px; margin: 6px 0 0; }
.muted { color: #8a8578; }
.layout { display: grid; grid-template-columns: 300px 1fr; gap: 16px; align-items: start; }
.panel { background: #fff; border: 1px solid #d8d6c8; border-radius: 10px; padding: 18px; }
.event-list { display: grid; gap: 8px; }
.event-item { display: grid; gap: 4px; text-align: left; border: 1px solid #e3dfd2; border-radius: 8px; padding: 12px; background: #fbfaf4; cursor: pointer; }
.event-item.active { border-color: #274335; box-shadow: 0 0 0 1px #274335 inset; }
.event-item small { color: #7c837b; }
.event-title { display: flex; justify-content: space-between; align-items: center; gap: 8px; }
.detail-head { display: flex; justify-content: space-between; gap: 12px; align-items: flex-start; }
.tags { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
.tag { border: 1px solid #d2cdbd; border-radius: 999px; padding: 2px 10px; font-size: 12px; font-weight: 700; color: #596257; }
.tag.risk { background: #fdeccb; color: #7d4d18; border-color: #e8c98f; }
.metrics { display: grid; grid-template-columns: repeat(3, minmax(0,1fr)); gap: 12px; margin: 14px 0; }
.stat { background: #fbfaf4; border: 1px solid #d8d6c8; border-radius: 8px; padding: 14px 16px; display: grid; gap: 4px; }
.stat span { color: #596257; font-size: 12px; }
.stat strong { font-size: 24px; color: #274335; }
.reserved { color: #1d3f91; }
.shortage, .shortage-text { color: #b0443c !important; font-weight: 800; }
.order-card { border: 1px solid #e7e3d6; border-radius: 8px; padding: 14px; margin-bottom: 12px; background: #fbfaf4; }
.order-head { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.order-metrics { display: flex; gap: 18px; margin: 10px 0; font-size: 13px; color: #596257; }
.mono { font-family: ui-monospace, Menlo, monospace; }
.btn { border: 1px solid #c9c4b4; background: #fff; border-radius: 6px; padding: 7px 14px; cursor: pointer; }
@media (max-width: 900px) { .layout { grid-template-columns: 1fr; } .metrics { grid-template-columns: 1fr; } }
</style>
