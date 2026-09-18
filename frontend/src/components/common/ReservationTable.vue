<script setup lang="ts">
import type { DispatchReservation } from "../../types/DispatchOrder";
import { formatNumber } from "../../utils/formatters";
import StatusBadge from "./StatusBadge.vue";
import EmptyState from "./EmptyState.vue";

/** 预占明细表：审批后显示 FEFO 命中的批次及数量；出库/驳回后状态同步为 CONSUMED/RELEASED */
defineProps<{ reservations: DispatchReservation[] }>();
</script>

<template>
  <div class="reservation-table">
    <table v-if="reservations.length">
      <thead>
        <tr>
          <th>批次号</th>
          <th>到期日</th>
          <th class="num">占用数量</th>
          <th class="num">占用后批次可用</th>
          <th>状态</th>
          <th>发生时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in reservations" :key="r.id">
          <td class="mono">{{ r.batch_no ?? `批次#${r.batch_id}` }}</td>
          <td>{{ r.expire_at ?? "-" }}</td>
          <td class="num strong">{{ formatNumber(r.quantity) }}</td>
          <td class="num">{{ r.batch_available_quantity ?? "-" }}</td>
          <td><StatusBadge :value="r.status" /></td>
          <td class="time">
            <template v-if="r.status === 'CONSUMED'">{{ r.consumed_at }}</template>
            <template v-else-if="r.status === 'RELEASED'">{{ r.released_at }}</template>
            <template v-else>{{ r.created_at }}</template>
          </td>
        </tr>
      </tbody>
    </table>
    <EmptyState v-else text="尚无预占记录：审批通过后按最早到期先出（FEFO）分配批次" />
  </div>
</template>

<style scoped>
.reservation-table { width: 100%; overflow-x: auto; }
table { width: 100%; border-collapse: collapse; font-size: 13px; }
th, td { padding: 9px 10px; border-bottom: 1px solid #e7e3d6; text-align: left; }
th { color: #6a7268; font-weight: 700; background: #f5f3ea; }
.num { text-align: right; font-variant-numeric: tabular-nums; }
.mono { font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
.strong { font-weight: 700; color: #1d3f91; }
.time { color: #7c837b; white-space: nowrap; }
</style>
