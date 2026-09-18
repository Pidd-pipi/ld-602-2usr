<script setup lang="ts">
import { mockData } from "../mocks/seedData";
import StatusBadge from "../components/common/StatusBadge.vue";
import StatCard from "../components/common/StatCard.vue";

const entries = Object.entries(mockData);
const total = entries.reduce((sum, [, rows]) => sum + rows.length, 0);
</script>

<template>
  <section class="metrics">
    <StatCard label="核心模型" :value="entries.length" />
    <StatCard label="共享枚举" :value="3" />
    <StatCard label="本地记录" :value="total" />
  </section>
  <section class="workbench">
    <div class="panel wide">
      <h2>业务数据</h2>
      <article class="row" v-for="[key, rows] in entries" :key="key">
        <strong>{{ key }}</strong>
        <span>{{ rows.length }} 条</span>
        <StatusBadge value="READY" />
      </article>
    </div>
    <div class="panel">
      <h2>调拨预占闭环</h2>
      <p>审批通过按最早到期先出预占批次，出库确认转实扣，驳回或取消全部释放；调拨页与事件详情同步展示预占、可用与缺口。</p>
    </div>
  </section>
</template>
