<script setup lang="ts">
import { computed } from "vue";
import type { InventoryBatch } from "../../types/InventoryBatch";
import { formatNumber } from "../../utils/formatters";
import EmptyState from "./EmptyState.vue";

/**
 * 批次表（调拨页 + 仓库库存页共用）：
 * 展示 批次号 / 物资 / 到期日 / 总量 / 已预占 / 可用，按 FEFO 顺序（后端已排序）
 */
const props = defineProps<{
  batches: InventoryBatch[];
  loading?: boolean;
  compact?: boolean;
}>();

const rows = computed(() => props.batches ?? []);
</script>

<template>
  <div class="batch-table">
    <table v-if="rows.length">
      <thead>
        <tr>
          <th>批次号</th>
          <th>物资</th>
          <th>到期日</th>
          <th class="num">总量</th>
          <th class="num">已预占</th>
          <th class="num">可用</th>
          <th v-if="!compact" class="num">在途来源</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="b in rows" :key="b.id" :class="{ tight: b.available_quantity === 0 }">
          <td class="mono">{{ b.batch_no }}</td>
          <td>{{ b.supply_item_name ?? `物资#${b.supply_item_id}` }}</td>
          <td>{{ b.expire_at }}</td>
          <td class="num">{{ formatNumber(b.quantity) }}</td>
          <td class="num reserved">{{ formatNumber(b.reserved_quantity) }}</td>
          <td class="num available">{{ formatNumber(b.available_quantity) }}</td>
          <td v-if="!compact">{{ b.inbound_source }}</td>
        </tr>
      </tbody>
    </table>
    <EmptyState v-else :text="loading ? '批次加载中…' : '该仓库暂无可展示批次'" />
  </div>
</template>

<style scoped>
.batch-table { width: 100%; overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
th, td { padding: 9px 10px; border-bottom: 1px solid #e7e3d6; text-align: left; }
th { color: #6a7268; font-weight: 700; background: #f5f3ea; }
.num { text-align: right; font-variant-numeric: tabular-nums; }
.mono { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
.reserved { color: #1d3f91; font-weight: 700; }
.available { color: #244b31; font-weight: 700; }
tr.tight td { color: #9a6b66; }
</style>
