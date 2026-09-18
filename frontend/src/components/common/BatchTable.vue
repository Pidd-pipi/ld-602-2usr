<script setup lang="ts">
import { formatDate } from "../../utils/formatters";

// 批次/预占通用表格：库存批次与调拨预占明细共用。
interface BatchTableRow {
  batch_no: string;
  supply_item_name?: string;
  expire_at?: string;
  quantity?: number;
  reserved_quantity?: number;
  available_quantity?: number;
  status?: string;
}

defineProps<{ rows: BatchTableRow[]; title?: string }>();
</script>

<template>
  <div class="batch-table">
    <h3 v-if="title">{{ title }}</h3>
    <table>
      <thead>
        <tr>
          <th>批次号</th>
          <th v-if="rows.some((row) => row.supply_item_name)">物资</th>
          <th>到期日</th>
          <th v-if="rows.some((row) => row.quantity != null)">数量</th>
          <th v-if="rows.some((row) => row.reserved_quantity != null)">预占</th>
          <th v-if="rows.some((row) => row.available_quantity != null)">可用</th>
          <th v-if="rows.some((row) => row.status)">状态</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row.batch_no + (row.status ?? '')">
          <td>{{ row.batch_no }}</td>
          <td v-if="rows.some((r) => r.supply_item_name)">{{ row.supply_item_name ?? "-" }}</td>
          <td>{{ row.expire_at ? formatDate(row.expire_at) : "-" }}</td>
          <td v-if="rows.some((r) => r.quantity != null)">{{ row.quantity ?? "-" }}</td>
          <td v-if="rows.some((r) => r.reserved_quantity != null)">{{ row.reserved_quantity ?? "-" }}</td>
          <td v-if="rows.some((r) => r.available_quantity != null)">{{ row.available_quantity ?? "-" }}</td>
          <td v-if="rows.some((r) => r.status)">{{ row.status ?? "-" }}</td>
        </tr>
        <tr v-if="rows.length === 0">
          <td colspan="7" class="empty-cell">暂无批次数据</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
