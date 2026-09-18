<script setup lang="ts">
import { computed } from "vue";
import type { InventoryBatch } from "../../types/InventoryBatch";
import EmptyState from "./EmptyState.vue";

const props = defineProps<{ batches: InventoryBatch[] }>();

const daysLeft = (date: string) =>
  Math.ceil((new Date(date).getTime() - Date.now()) / (24 * 60 * 60 * 1000));

const rows = computed(() => props.batches ?? []);
</script>

<template>
  <div>
    <ul v-if="rows.length" class="warning-list">
      <li v-for="b in rows" :key="b.id">
        <div class="line">
          <strong class="mono">{{ b.batch_no }}</strong>
          <span :class="['days', daysLeft(b.expire_at) <= 30 ? 'soon' : 'near']">
            剩 {{ daysLeft(b.expire_at) }} 天
          </span>
        </div>
        <small>{{ b.supply_item_name ?? `物资#${b.supply_item_id}` }} · 到期 {{ b.expire_at }} · 可用 {{ b.available_quantity }}</small>
      </li>
    </ul>
    <EmptyState v-else text="90 天内暂无临期批次" />
  </div>
</template>

<style scoped>
.warning-list { list-style: none; margin: 0; padding: 0; display: grid; gap: 10px; }
.warning-list li { border-left: 3px solid #d39b46; padding: 2px 0 2px 10px; }
.line { display: flex; justify-content: space-between; align-items: center; }
.mono { font-family: ui-monospace, Menlo, monospace; font-size: 13px; }
.days { font-size: 12px; font-weight: 800; border-radius: 999px; padding: 1px 8px; }
.days.soon { background: #f8e2e0; color: #8c2f28; }
.days.near { background: #fdeccb; color: #7d4d18; }
small { color: #7c837b; font-size: 12px; }
</style>
