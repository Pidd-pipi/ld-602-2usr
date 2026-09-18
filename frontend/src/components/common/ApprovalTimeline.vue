<script setup lang="ts">
import type { DispatchStatusEvent } from "../../types/DispatchOrder";
import { formatDate } from "../../utils/formatters";
import StatusBadge from "./StatusBadge.vue";

// 审批时间线：展示调拨单状态流转记录（创建/提交/审批/出库/签收/驳回/取消）。
defineProps<{ events: DispatchStatusEvent[]; title?: string }>();
</script>

<template>
  <div class="timeline">
    <h3 v-if="title">{{ title }}</h3>
    <ol>
      <li v-for="(event, index) in events" :key="index">
        <StatusBadge :value="event.status" />
        <div class="timeline-body">
          <strong>{{ event.actor }}</strong>
          <span>{{ event.note }}</span>
          <time>{{ formatDate(event.at) }}</time>
        </div>
      </li>
      <li v-if="events.length === 0" class="empty-cell">暂无流转记录</li>
    </ol>
  </div>
</template>
