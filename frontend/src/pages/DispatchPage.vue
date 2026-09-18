<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useDispatchOrderStore } from "../stores/DispatchOrderStore";
import { useWarehouseStore } from "../stores/WarehouseStore";
import { useShelterStore } from "../stores/ShelterStore";
import { useSupplyItemStore } from "../stores/SupplyItemStore";
import { useInventoryBatchStore } from "../stores/InventoryBatchStore";
import { useDispatchFlow } from "../hooks/useDispatchFlow";
import { createDispatchOrderForm } from "../constructors/DispatchOrderConstructor";
import StatusBadge from "../components/common/StatusBadge.vue";
import BatchTable from "../components/common/BatchTable.vue";
import ReservationTable from "../components/common/ReservationTable.vue";
import ApprovalTimeline from "../components/common/ApprovalTimeline.vue";
import EmptyState from "../components/common/EmptyState.vue";
import { formatNumber } from "../utils/formatters";
import type { DispatchOrder, DispatchShortage } from "../types/DispatchOrder";

const store = useDispatchOrderStore();
const warehouseStore = useWarehouseStore();
const shelterStore = useShelterStore();
const supplyStore = useSupplyItemStore();
const batchStore = useInventoryBatchStore();
const flow = useDispatchFlow();

const showCreate = ref(false);
const selectedWarehouseForBatches = ref<number | null>(null);
const detailId = ref<number | null>(null);

const form = reactive({
  event_id: null as number | null,
  source_warehouse_id: 1,
  shelter_id: 1,
  priority: "HIGH",
  requested_by: "street-admin",
  lines: [
    { supply_item_id: 1, quantity: 100 }
  ]
});

onMounted(async () => {
  await Promise.all([
    store.load(),
    warehouseStore.load(),
    shelterStore.load(),
    supplyStore.load(),
    batchStore.load()
  ]);
  selectedWarehouseForBatches.value = form.source_warehouse_id;
});

watch(() => form.source_warehouse_id, (id) => {
  selectedWarehouseForBatches.value = id;
  batchStore.load(id);
});

watch(selectedWarehouseForBatches, (id) => {
  if (id) batchStore.load(id);
});

const orders = computed(() => store.rows);
const current = computed(() => store.current);
const warehouses = computed(() => warehouseStore.rows);
const shelters = computed(() => shelterStore.rows);
const supplyItems = computed(() => supplyStore.rows);
const batches = computed(() => batchStore.rows);
const batchTotals = computed(() => batchStore.totals);

const itemName = (id: number) => supplyItems.value.find((s) => s.id === id)?.name ?? `物资#${id}`;
const warehouseName = (id: number) => warehouses.value.find((w) => w.id === id)?.name ?? `仓库#${id}`;
const shelterName = (id: number) => shelters.value.find((s) => s.id === id)?.name ?? `避难点#${id}`;

function addLine() {
  form.lines.push({ supply_item_id: supplyItems.value[0]?.id ?? 1, quantity: 50 });
}
function removeLine(index: number) {
  form.lines.splice(index, 1);
}

async function submitCreate() {
  const payload = createDispatchOrderForm({
    eventId: form.event_id,
    sourceWarehouseId: form.source_warehouse_id,
    shelterId: form.shelter_id,
    priority: form.priority,
    requestedBy: form.requested_by,
    lines: form.lines.filter((l) => Number(l.quantity) > 0)
  });
  const created = await store.create(payload);
  if (created) {
    showCreate.value = false;
  }
}

async function openDetail(id: number) {
  detailId.value = id;
  flow.shortages.value = [];
  flow.errorMessage.value = "";
  await store.loadDetail(id);
}
function closeDetail() {
  detailId.value = null;
}

async function doApprove(order: DispatchOrder) {
  const result = await flow.approve(order.id, "approver");
  if (result) detailId.value = result.id;
}
async function doOutbound(order: DispatchOrder) {
  const result = await flow.confirmOutbound(order.id, "warehouse-keeper");
  if (result) detailId.value = result.id;
}
async function doReject(order: DispatchOrder) {
  const reason = window.prompt("请输入驳回原因（将释放全部预占）", "物资信息待核实");
  if (reason === null) return;
  await flow.reject(order.id, "approver", reason || "未填写原因");
}
async function doCancel(order: DispatchOrder) {
  const reason = window.prompt("请输入取消原因（将释放全部预占）", "需求变更");
  if (reason === null) return;
  await flow.cancel(order.id, "street-admin", reason || "未填写原因");
}
async function doReceive(order: DispatchOrder) {
  await flow.receive(order.id, "shelter-staff");
}

function shortageOfOrder(orderId: number): DispatchShortage[] {
  return detailId.value === orderId ? flow.shortages.value : [];
}
const detailShortages = computed<DispatchShortage[]>(() =>
  current.value ? shortageOfOrder(current.value.id) : []);
</script>

<template>
  <section class="dispatch-page">
    <div class="page-toolbar">
      <div>
        <p class="eyebrow">DISPATCH · 预占闭环</p>
        <h2>调拨审批与库存预占</h2>
        <p class="hint">审批通过按「最早到期先出（FEFO），同到期日按批次号升序」预占；不足时整单失败不占任何批次。</p>
      </div>
      <div class="toolbar-actions">
        <button class="btn primary" @click="showCreate = !showCreate">
          {{ showCreate ? "收起新建" : "新建调拨单" }}
        </button>
        <button class="btn" @click="store.load()">刷新</button>
      </div>
    </div>

    <!-- 批次概览：总量 / 预占 / 可用 -->
    <div class="metrics three">
      <div class="stat"><span>批次总量</span><strong>{{ formatNumber(batchTotals.quantity) }}</strong></div>
      <div class="stat"><span>已预占</span><strong class="reserved">{{ formatNumber(batchTotals.reserved) }}</strong></div>
      <div class="stat"><span>当前可用</span><strong class="available">{{ formatNumber(batchTotals.available) }}</strong></div>
    </div>

    <!-- 来源仓批次 -->
    <div class="panel">
      <div class="panel-head">
        <h3>来源仓库批次（可用 / 预占）</h3>
        <select v-model="selectedWarehouseForBatches">
          <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
        </select>
      </div>
      <BatchTable :batches="batches" :loading="batchStore.loading" />
    </div>

    <!-- 新建调拨单 -->
    <div v-if="showCreate" class="panel create-panel">
      <h3>新建调拨单（提交后待审批）</h3>
      <div class="form-grid">
        <label>来源仓库
          <select v-model="form.source_warehouse_id">
            <option v-for="w in warehouses" :key="w.id" :value="w.id">{{ w.name }}</option>
          </select>
        </label>
        <label>目标避难点
          <select v-model="form.shelter_id">
            <option v-for="s in shelters" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </label>
        <label>优先级
          <select v-model="form.priority">
            <option value="HIGH">高</option>
            <option value="NORMAL">普通</option>
            <option value="LOW">低</option>
          </select>
        </label>
        <label>申请人
          <input v-model="form.requested_by" type="text" />
        </label>
      </div>
      <table class="line-form">
        <thead>
          <tr><th>物资</th><th class="num">申请数量</th><th></th></tr>
        </thead>
        <tbody>
          <tr v-for="(line, i) in form.lines" :key="i">
            <td>
              <select v-model="line.supply_item_id">
                <option v-for="item in supplyItems" :key="item.id" :value="item.id">{{ item.name }}（{{ item.unit }}）</option>
              </select>
            </td>
            <td class="num"><input v-model.number="line.quantity" type="number" min="1" /></td>
            <td><button class="btn link danger" @click="removeLine(i)" :disabled="form.lines.length <= 1">删除</button></td>
          </tr>
        </tbody>
      </table>
      <div class="form-foot">
        <button class="btn link" @click="addLine">+ 增加一行物资</button>
        <button class="btn primary" :disabled="store.acting" @click="submitCreate">提交审批</button>
      </div>
      <p v-if="store.error" class="error">{{ store.error }}</p>
    </div>

    <!-- 调拨单列表 -->
    <div class="panel">
      <h3>调拨单（{{ orders.length }}）</h3>
      <p v-if="store.loading" class="muted">加载中…</p>
      <EmptyState v-else-if="!orders.length" text="暂无调拨单，点击右上角新建" />
      <table v-else class="order-table">
        <thead>
          <tr>
            <th>#</th><th>来源仓库 → 避难点</th><th class="num">申请</th>
            <th class="num">预占</th><th class="num">缺口</th><th>状态</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="o in orders" :key="o.id">
            <td class="mono">#{{ o.id }}</td>
            <td>{{ warehouseName(o.source_warehouse_id) }} → {{ shelterName(o.shelter_id) }}</td>
            <td class="num">{{ formatNumber(o.requested_quantity ?? 0) }}</td>
            <td class="num reserved">{{ formatNumber(o.reserved_quantity ?? 0) }}</td>
            <td class="num">
              <span v-if="(o.shortage_quantity ?? 0) > 0" class="shortage">缺 {{ formatNumber(o.shortage_quantity) }}</span>
              <span v-else class="muted">0</span>
            </td>
            <td><StatusBadge :value="o.status" /></td>
            <td class="actions">
              <button class="btn link" @click="openDetail(o.id)">详情</button>
              <button v-if="o.status === 'SUBMITTED'" class="btn link ok" :disabled="store.acting" @click="doApprove(o)">审批通过</button>
              <button v-if="o.status === 'APPROVED'" class="btn link ok" :disabled="store.acting" @click="doOutbound(o)">出库确认</button>
              <button v-if="o.status === 'DISPATCHED'" class="btn link" @click="doReceive(o)">签收</button>
              <button v-if="o.status === 'SUBMITTED' || o.status === 'APPROVED'" class="btn link danger" @click="doReject(o)">驳回</button>
              <button v-if="o.status === 'SUBMITTED' || o.status === 'APPROVED'" class="btn link danger" @click="doCancel(o)">取消</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 操作反馈：缺口整单失败 -->
    <div v-if="flow.errorMessage.value" class="panel feedback">
      <h3>操作未完成</h3>
      <p class="error">{{ flow.errorMessage.value }}</p>
      <table v-if="flow.shortages.value.length" class="shortage-table">
        <thead>
          <tr><th>物资</th><th class="num">申请</th><th class="num">可用</th><th class="num">缺口</th></tr>
        </thead>
        <tbody>
          <tr v-for="s in flow.shortages.value" :key="s.line_id">
            <td>{{ s.supply_item_name }}</td>
            <td class="num">{{ formatNumber(s.requested_quantity) }}</td>
            <td class="num">{{ formatNumber(s.available_quantity) }}</td>
            <td class="num shortage">{{ formatNumber(s.shortage_quantity) }}</td>
          </tr>
        </tbody>
      </table>
      <p class="hint">整单未占用任何批次，请释放/调整其他调拨后重试。</p>
      <button class="btn" @click="flow.clearFeedback()">知道了</button>
    </div>

    <!-- 详情抽屉：行缺口 + 预占明细 + 时间线 -->
    <div v-if="detailId !== null" class="drawer-mask" @click.self="closeDetail">
      <div class="drawer">
        <div class="drawer-head">
          <div>
            <p class="eyebrow">ORDER #{{ detailId }}</p>
            <h3>调拨单详情与预占明细</h3>
          </div>
          <button class="btn" @click="closeDetail">关闭</button>
        </div>

        <div v-if="store.loading" class="muted">详情加载中…</div>
        <div v-else-if="current" class="drawer-body">
          <div class="detail-status">
            <StatusBadge :value="current.status" />
            <span class="muted">{{ current.warehouse?.name }} → {{ current.shelter?.name }}</span>
          </div>

          <div class="metrics three tight">
            <div class="stat"><span>申请总量</span><strong>{{ formatNumber(current.requested_quantity) }}</strong></div>
            <div class="stat"><span>已预占</span><strong class="reserved">{{ formatNumber(current.reserved_quantity) }}</strong></div>
            <div class="stat"><span>缺口</span>
              <strong :class="(current.shortage_quantity ?? 0) > 0 ? 'shortage-text' : ''">
                {{ formatNumber(current.shortage_quantity) }}
              </strong>
            </div>
          </div>

          <h4>调拨明细行</h4>
          <table class="line-table">
            <thead><tr><th>物资</th><th class="num">申请</th><th class="num">已预占</th><th class="num">缺口</th></tr></thead>
            <tbody>
              <tr v-for="l in current.lines ?? []" :key="l.id">
                <td>{{ l.supply_item_name ?? itemName(l.supply_item_id) }}</td>
                <td class="num">{{ formatNumber(l.quantity) }}</td>
                <td class="num reserved">{{ formatNumber(l.reserved_quantity) }}</td>
                <td class="num">
                  <span v-if="l.shortage_quantity > 0" class="shortage">{{ formatNumber(l.shortage_quantity) }}</span>
                  <span v-else>0</span>
                </td>
              </tr>
            </tbody>
          </table>

          <h4>批次预占明细（FEFO）</h4>
          <ReservationTable :reservations="current.reservations ?? []" />

          <h4>审批与出库时间线</h4>
          <ApprovalTimeline :order="current" />

          <div v-if="detailShortages.length" class="feedback inline">
            <p class="error">最近一次审批因缺口整单失败，未占用任何批次：</p>
            <ul>
              <li v-for="s in detailShortages" :key="s.line_id">
                {{ s.supply_item_name }}：申请 {{ s.requested_quantity }}，可用 {{ s.available_quantity }}，
                <span class="shortage">缺口 {{ s.shortage_quantity }}</span>
              </li>
            </ul>
          </div>
        </div>
        <EmptyState v-else text="调拨单详情加载失败或已被删除" />
      </div>
    </div>
  </section>
</template>

<style scoped>
.dispatch-page { display: grid; gap: 16px; }
.page-toolbar { display: flex; justify-content: space-between; align-items: flex-end; gap: 16px; flex-wrap: wrap; }
.toolbar-actions { display: flex; gap: 8px; }
h2 { margin: 4px 0; font-size: 24px; }
h3 { margin: 0 0 12px; font-size: 16px; }
h4 { margin: 18px 0 8px; font-size: 14px; }
.eyebrow { margin: 0; color: #7d4d18; font-size: 11px; font-weight: 800; letter-spacing: .08em; }
.hint { color: #7c837b; font-size: 12px; margin: 6px 0 0; }
.muted { color: #8a8578; font-size: 13px; }
.metrics { display: grid; gap: 12px; }
.metrics.three { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.metrics.tight { gap: 10px; }
.stat { background: #fbfaf4; border: 1px solid #d8d6c8; border-radius: 8px; padding: 14px 16px; display: grid; gap: 4px; }
.stat span { color: #596257; font-size: 12px; }
.stat strong { font-size: 24px; color: #274335; }
.reserved { color: #1d3f91 !important; }
.available { color: #244b31 !important; }
.shortage, .shortage-text { color: #b0443c !important; font-weight: 800; }
.panel { background: #fff; border: 1px solid #d8d6c8; border-radius: 10px; padding: 18px; }
.panel-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
select, input[type="text"], input[type="number"] {
  border: 1px solid #c9c4b4; border-radius: 6px; padding: 7px 9px; font: inherit; background: #fff;
}
.form-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; margin-bottom: 12px; }
.form-grid label, .line-form select, .line-form input { width: 100%; font-size: 13px; }
.form-grid label { display: grid; gap: 5px; color: #596257; font-size: 12px; }
.line-form, .order-table, .line-table, .shortage-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.line-form th, .line-form td, .order-table th, .order-table td, .line-table th, .line-table td,
.shortage-table th, .shortage-table td { padding: 8px 9px; border-bottom: 1px solid #eeeadd; text-align: left; }
.line-form th, .order-table th, .line-table th, .shortage-table th { color: #6a7268; background: #f8f6ee; }
.num { text-align: right; font-variant-numeric: tabular-nums; }
.mono { font-family: ui-monospace, Menlo, monospace; }
.form-foot { display: flex; justify-content: space-between; margin-top: 12px; }
.btn { border: 1px solid #c9c4b4; background: #fff; border-radius: 6px; padding: 7px 14px; cursor: pointer; font: inherit; }
.btn:hover { background: #f5f1e6; }
.btn.primary { background: #274335; color: #fff; border-color: #274335; }
.btn.link { border: 0; background: transparent; padding: 4px 8px; color: #1d3f91; }
.btn.link.ok { color: #244b31; font-weight: 700; }
.btn.link.danger { color: #b0443c; }
.btn:disabled { opacity: .5; cursor: not-allowed; }
.actions { display: flex; flex-wrap: wrap; gap: 2px; }
.order-table .actions .btn.link { padding: 3px 6px; font-size: 12px; }
.error { color: #b0443c; font-weight: 600; }
.feedback { border-color: #e3c3bf; background: #fdf5f4; }
.feedback.inline { padding: 12px; margin-top: 14px; }
.feedback ul { margin: 6px 0; padding-left: 18px; font-size: 13px; }
.drawer-mask { position: fixed; inset: 0; background: rgba(30, 35, 30, .42); display: flex; justify-content: flex-end; z-index: 40; }
.drawer { width: min(680px, 94vw); background: #f4f2ea; height: 100%; overflow-y: auto; padding: 22px; box-shadow: -8px 0 30px rgba(0,0,0,.18); }
.drawer-head { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 14px; }
.detail-status { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
.create-panel { background: #fbfaf4; }
@media (max-width: 900px) { .metrics.three, .form-grid { grid-template-columns: 1fr 1fr; } }
</style>
