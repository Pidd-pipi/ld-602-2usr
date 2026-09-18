<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { storeToRefs } from "pinia";
import StatusBadge from "../components/common/StatusBadge.vue";
import StatCard from "../components/common/StatCard.vue";
import BatchTable from "../components/common/BatchTable.vue";
import ApprovalTimeline from "../components/common/ApprovalTimeline.vue";
import EmptyState from "../components/common/EmptyState.vue";
import { useDispatchOrderStore } from "../stores/DispatchOrderStore";
import { useInventoryBatchStore } from "../stores/InventoryBatchStore";
import { useDispatchEventStore } from "../stores/DispatchEventStore";
import { useWarehouseStore } from "../stores/WarehouseStore";
import { useShelterStore } from "../stores/ShelterStore";
import { useSupplyItemStore } from "../stores/SupplyItemStore";
import { useDispatchFlow } from "../hooks/useDispatchFlow";
import { createDispatchOrderForm } from "../constructors/DispatchOrderConstructor";
import { DispatchPriority, DispatchPriorityText } from "../constants/DispatchPriority";
import { formatNumber } from "../utils/formatters";
import type { DispatchOrder } from "../types/DispatchOrder";

const orderStore = useDispatchOrderStore();
const batchStore = useInventoryBatchStore();
const eventStore = useDispatchEventStore();
const warehouseStore = useWarehouseStore();
const shelterStore = useShelterStore();
const supplyItemStore = useSupplyItemStore();
const flow = useDispatchFlow();

const { rows, current } = storeToRefs(orderStore);

const showCreate = ref(false);
const form = ref(createDispatchOrderForm());
const operator = ref("值班审批员");

onMounted(async () => {
  await Promise.all([
    orderStore.load(),
    batchStore.load(),
    eventStore.load(),
    warehouseStore.load(),
    shelterStore.load(),
    supplyItemStore.load()
  ]);
});

// 顶部汇总：预占（已审批未出库）、可用（全部合格批次）、缺口（待审批单据）。
const reservedTotal = computed(() =>
  rows.value.filter((order) => order.status === "APPROVED").reduce((sum, order) => sum + order.total_reserved, 0)
);
const availableTotal = computed(() => batchStore.totalAvailable);
const shortageTotal = computed(() =>
  rows.value
    .filter((order) => order.status === "DRAFT" || order.status === "SUBMITTED")
    .reduce((sum, order) => sum + order.total_shortage, 0)
);

async function select(order: DispatchOrder) {
  await orderStore.loadDetail(order.id);
}

async function refresh() {
  await Promise.all([orderStore.load(), batchStore.load()]);
  if (current.value) {
    await orderStore.loadDetail(current.value.id);
  }
}

async function run(order: DispatchOrder, action: Parameters<typeof flow.runAction>[1]) {
  await flow.runAction(order, action, operator.value);
  await batchStore.load();
}

async function submitCreate() {
  const payload = form.value;
  if (payload.lines.length === 0 || payload.lines.some((line) => line.quantity <= 0)) {
    return;
  }
  const created = await orderStore.create({
    ...payload,
    requested_by: payload.requested_by || "值班调度员"
  });
  showCreate.value = false;
  form.value = createDispatchOrderForm();
  if (created?.id) {
    await orderStore.loadDetail(created.id);
  }
}

function addLine() {
  form.value.lines.push({ supply_item_id: supplyItemStore.rows[0]?.id ?? 1, quantity: 1 });
}

function removeLine(index: number) {
  form.value.lines.splice(index, 1);
}

const reservationRows = computed(() =>
  (current.value?.reservations ?? []).map((reservation) => ({
    batch_no: reservation.batch_no ?? `#${reservation.batch_id}`,
    expire_at: reservation.expire_at,
    quantity: reservation.quantity,
    status: reservation.status
  }))
);
</script>

<template>
  <div class="dispatch-page">
    <section class="metrics">
      <StatCard label="预占总量（已审批）" :value="formatNumber(reservedTotal)" />
      <StatCard label="可用总量（合格批次）" :value="formatNumber(availableTotal)" />
      <StatCard label="缺口总量（待审批）" :value="formatNumber(shortageTotal)" />
    </section>

    <section class="toolbar">
      <button class="primary" @click="showCreate = true">新建调拨单</button>
      <button @click="refresh">刷新</button>
      <label class="operator">操作人 <input v-model="operator" placeholder="操作人" /></label>
      <span v-if="flow.feedback.value" class="feedback ok-text">{{ flow.feedback.value }}</span>
    </section>

    <section v-if="flow.lastError.value" class="alert">
      <strong>{{ flow.lastError.value }}</strong>
      <table v-if="flow.lastShortages.value.length > 0">
        <thead>
          <tr><th>物资</th><th>请求</th><th>可用</th><th>缺口</th></tr>
        </thead>
        <tbody>
          <tr v-for="shortage in flow.lastShortages.value" :key="shortage.supply_item_id">
            <td>{{ shortage.supply_item_name ?? `#${shortage.supply_item_id}` }}</td>
            <td>{{ shortage.requested_quantity }}</td>
            <td>{{ shortage.available_quantity }}</td>
            <td class="danger-text">{{ shortage.shortage_quantity }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="workbench">
      <div class="panel wide">
        <h2>调拨单</h2>
        <table>
          <thead>
            <tr>
              <th>单号</th><th>事件</th><th>来源仓 → 安置点</th><th>优先级</th><th>状态</th>
              <th>请求</th><th>预占</th><th>可用</th><th>缺口</th><th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="order in rows"
              :key="order.id"
              :class="{ selected: current?.id === order.id }"
              @click="select(order)"
            >
              <td>#{{ order.id }}</td>
              <td>{{ order.event_name ?? order.event_id }}</td>
              <td>{{ order.source_warehouse_name ?? order.source_warehouse_id }} → {{ order.shelter_name ?? order.shelter_id }}</td>
              <td>{{ DispatchPriorityText[order.priority as DispatchPriority] ?? order.priority }}</td>
              <td><StatusBadge :value="order.status" /></td>
              <td>{{ formatNumber(order.total_requested) }}</td>
              <td class="info-text">{{ formatNumber(order.total_reserved) }}</td>
              <td>{{ formatNumber(order.total_available) }}</td>
              <td :class="{ 'danger-text': order.total_shortage > 0 }">{{ formatNumber(order.total_shortage) }}</td>
              <td @click.stop>
                <button
                  v-for="action in flow.actionsOf(order)"
                  :key="action"
                  class="mini"
                  :disabled="flow.acting.value"
                  @click="run(order, action)"
                >{{ flow.actionText(action) }}</button>
              </td>
            </tr>
            <tr v-if="rows.length === 0"><td colspan="10" class="empty-cell">暂无调拨单</td></tr>
          </tbody>
        </table>
      </div>

      <div class="panel" v-if="current">
        <h2>调拨单 #{{ current.id }} 明细</h2>
        <p class="meta">
          {{ current.source_warehouse_name }} → {{ current.shelter_name }} ｜ 申请人 {{ current.requested_by }}
          <template v-if="current.approved_by">｜ 审批人 {{ current.approved_by }}</template>
        </p>
        <h3>物资行（请求 / 预占 / 实扣 / 可用 / 缺口）</h3>
        <table>
          <thead>
            <tr><th>物资</th><th>请求</th><th>预占</th><th>实扣</th><th>可用</th><th>缺口</th></tr>
          </thead>
          <tbody>
            <tr v-for="line in current.lines" :key="line.id">
              <td>{{ line.supply_item_name ?? line.supply_item_id }}（{{ line.unit ?? "-" }}）</td>
              <td>{{ line.requested_quantity }}</td>
              <td class="info-text">{{ line.reserved_quantity }}</td>
              <td>{{ line.consumed_quantity }}</td>
              <td>{{ line.available_quantity }}</td>
              <td :class="{ 'danger-text': line.shortage_quantity > 0 }">{{ line.shortage_quantity }}</td>
            </tr>
          </tbody>
        </table>
        <BatchTable title="批次预占明细" :rows="reservationRows" />
        <ApprovalTimeline title="流转时间线" :events="current.history ?? []" />
      </div>
      <div class="panel" v-else>
        <h2>调拨单明细</h2>
        <EmptyState />
      </div>
    </section>

    <div v-if="showCreate" class="dialog-mask" @click.self="showCreate = false">
      <div class="dialog">
        <h2>新建调拨单</h2>
        <label>关联事件
          <select v-model.number="form.event_id">
            <option v-for="event in eventStore.rows" :key="event.id" :value="event.id">{{ event.name }}</option>
          </select>
        </label>
        <label>来源仓库
          <select v-model.number="form.source_warehouse_id">
            <option v-for="warehouse in warehouseStore.rows" :key="warehouse.id" :value="warehouse.id">{{ warehouse.name }}</option>
          </select>
        </label>
        <label>安置点
          <select v-model.number="form.shelter_id">
            <option v-for="shelter in shelterStore.rows" :key="shelter.id" :value="shelter.id">{{ shelter.name }}</option>
          </select>
        </label>
        <label>优先级
          <select v-model="form.priority">
            <option v-for="priority in DispatchPriority" :key="priority" :value="priority">{{ DispatchPriorityText[priority] }}</option>
          </select>
        </label>
        <label>申请人 <input v-model="form.requested_by" placeholder="值班调度员" /></label>
        <h3>物资行</h3>
        <div v-for="(line, index) in form.lines" :key="index" class="line-editor">
          <select v-model.number="line.supply_item_id">
            <option v-for="item in supplyItemStore.rows" :key="item.id" :value="item.id">{{ item.name }}（{{ item.unit }}）</option>
          </select>
          <input v-model.number="line.quantity" type="number" min="1" />
          <button class="mini" @click="removeLine(index)">删除</button>
        </div>
        <button class="mini" @click="addLine">添加物资行</button>
        <div class="dialog-actions">
          <button class="primary" :disabled="orderStore.acting" @click="submitCreate">创建</button>
          <button @click="showCreate = false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>
