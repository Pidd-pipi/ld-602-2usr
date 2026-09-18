<script setup lang="ts">
import { onMounted, ref } from "vue";
import { storeToRefs } from "pinia";
import StatusBadge from "../components/common/StatusBadge.vue";
import StatCard from "../components/common/StatCard.vue";
import EmptyState from "../components/common/EmptyState.vue";
import { useDispatchEventStore } from "../stores/DispatchEventStore";
import { formatDate, formatNumber } from "../utils/formatters";

// 事件响应：事件详情聚合其下调拨单，预占/可用/缺口与调拨页同源同步。
const eventStore = useDispatchEventStore();
const { rows, current } = storeToRefs(eventStore);
const selectedId = ref<number | null>(null);

onMounted(async () => {
  await eventStore.load();
  if (rows.value.length > 0) {
    await select(rows.value[0].id);
  }
});

async function select(id: number) {
  selectedId.value = id;
  await eventStore.loadDetail(id);
}

function orderTotals(key: "total_requested" | "total_reserved" | "total_available" | "total_shortage") {
  return (current.value?.orders ?? []).reduce((sum, order) => sum + (order[key] ?? 0), 0);
}
</script>

<template>
  <div class="events-page">
    <section v-if="current" class="metrics">
      <StatCard label="事件调拨单" :value="current.orders?.length ?? 0" />
      <StatCard label="预占总量" :value="formatNumber(orderTotals('total_reserved'))" />
      <StatCard label="缺口总量" :value="formatNumber(orderTotals('total_shortage'))" />
    </section>

    <section class="workbench">
      <div class="panel">
        <h2>灾害事件</h2>
        <article
          v-for="event in rows"
          :key="event.id"
          class="row clickable"
          :class="{ selected: selectedId === event.id }"
          @click="select(event.id)"
        >
          <strong>{{ event.name }}</strong>
          <span>{{ event.disaster_type }} ｜ {{ event.level }}</span>
          <StatusBadge :value="event.status" />
        </article>
        <EmptyState v-if="rows.length === 0" />
      </div>

      <div class="panel wide" v-if="current">
        <h2>{{ current.name }}</h2>
        <p class="meta">
          {{ current.disaster_type }} ｜ 等级 {{ current.level }} ｜ {{ formatDate(current.occurred_at) }} ｜
          <StatusBadge :value="current.status" />
        </p>
        <p>{{ current.description }}</p>

        <h3>关联调拨单（预占 / 可用 / 缺口实时同步）</h3>
        <table>
          <thead>
            <tr>
              <th>单号</th><th>来源仓 → 安置点</th><th>状态</th>
              <th>请求</th><th>预占</th><th>可用</th><th>缺口</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in current.orders ?? []" :key="order.id">
              <td>#{{ order.id }}</td>
              <td>{{ order.source_warehouse_name ?? order.source_warehouse_id }} → {{ order.shelter_name ?? order.shelter_id }}</td>
              <td><StatusBadge :value="order.status" /></td>
              <td>{{ formatNumber(order.total_requested) }}</td>
              <td class="info-text">{{ formatNumber(order.total_reserved) }}</td>
              <td>{{ formatNumber(order.total_available) }}</td>
              <td :class="{ 'danger-text': order.total_shortage > 0 }">{{ formatNumber(order.total_shortage) }}</td>
            </tr>
            <tr v-if="(current.orders ?? []).length === 0"><td colspan="7" class="empty-cell">该事件暂无调拨单</td></tr>
          </tbody>
        </table>

        <div v-for="order in (current.orders ?? []).filter((o) => o.lines.length > 0)" :key="`lines-${order.id}`" class="order-lines">
          <h4>调拨单 #{{ order.id }} 物资行</h4>
          <table>
            <thead>
              <tr><th>物资</th><th>请求</th><th>预占</th><th>实扣</th><th>可用</th><th>缺口</th></tr>
            </thead>
            <tbody>
              <tr v-for="line in order.lines" :key="line.id">
                <td>{{ line.supply_item_name ?? line.supply_item_id }}（{{ line.unit ?? "-" }}）</td>
                <td>{{ line.requested_quantity }}</td>
                <td class="info-text">{{ line.reserved_quantity }}</td>
                <td>{{ line.consumed_quantity }}</td>
                <td>{{ line.available_quantity }}</td>
                <td :class="{ 'danger-text': line.shortage_quantity > 0 }">{{ line.shortage_quantity }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="panel wide" v-else>
        <h2>事件详情</h2>
        <EmptyState />
      </div>
    </section>
  </div>
</template>
