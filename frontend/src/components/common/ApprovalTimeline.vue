<script setup lang="ts">
import { computed } from "vue";
import type { DispatchOrder } from "../../types/DispatchOrder";

/**
 * 审批时间线（调拨页详情 + 事件响应页共用）：
 * 创建 -> 提交 -> 审批通过(预占) -> 出库确认(实扣) -> 签收；驳回/取消为终止节点
 */
const props = defineProps<{ order: DispatchOrder }>();

interface Step {
  key: string;
  label: string;
  time?: string;
  actor?: string;
  state: "done" | "active" | "pending" | "stop";
}

const steps = computed<Step[]>(() => {
  const o = props.order;
  const status = o.status;
  const isRejected = status === "REJECTED";
  const isCancelled = status === "CANCELLED";

  const stage = (key: string): "done" | "active" | "pending" | "stop" => {
    const order2 = ["SUBMITTED", "APPROVED", "DISPATCHED", "RECEIVED"];
    if (isRejected || isCancelled) {
      const stopAt = status === "REJECTED"
        ? (o.approved_at ? "APPROVED" : "SUBMITTED")
        : (o.cancelled_at && o.approved_at ? "APPROVED" : "SUBMITTED");
      const idx = order2.indexOf(stopAt);
      const mine = order2.indexOf(key);
      if (mine < idx) return "done";
      if (mine === idx) return "stop";
      return "pending";
    }
    const cur = order2.indexOf(status);
    const mine = order2.indexOf(key);
    if (mine < cur) return "done";
    if (mine === cur) return "active";
    return "pending";
  };

  return [
    { key: "SUBMITTED", label: "提交审批", time: o.submitted_at, actor: o.requested_by, state: stage("SUBMITTED") },
    { key: "APPROVED", label: "审批通过 · 批次预占", time: o.approved_at, actor: o.approved_by, state: stage("APPROVED") },
    { key: "DISPATCHED", label: "出库确认 · 预占转实扣", time: o.dispatched_at, state: stage("DISPATCHED") },
    { key: "RECEIVED", label: "签收确认", time: o.received_at, state: stage("RECEIVED") }
  ];
});

const terminal = computed(() => {
  const o = props.order;
  if (o.status === "REJECTED") return { label: "已驳回", time: o.rejected_at, reason: o.reject_reason };
  if (o.status === "CANCELLED") return { label: "已取消 · 预占释放", time: o.cancelled_at, reason: o.cancel_reason };
  return null;
});
</script>

<template>
  <ol class="timeline">
    <li v-for="s in steps" :key="s.key" :class="s.state">
      <span class="dot" />
      <div class="content">
        <strong>{{ s.label }}</strong>
        <small v-if="s.time">{{ s.time }}<span v-if="s.actor"> · {{ s.actor }}</span></small>
        <small v-else-if="s.state === 'pending'" class="muted">待处理</small>
      </div>
    </li>
    <li v-if="terminal" class="stop">
      <span class="dot" />
      <div class="content">
        <strong>{{ terminal.label }}</strong>
        <small>{{ terminal.time }}<span v-if="terminal.reason"> · {{ terminal.reason }}</span></small>
      </div>
    </li>
  </ol>
</template>

<style scoped>
.timeline { list-style: none; margin: 0; padding: 0; }
.timeline li { display: flex; gap: 12px; padding: 8px 0; position: relative; }
.timeline li::before {
  content: ""; position: absolute; left: 5px; top: 20px; bottom: -8px;
  width: 2px; background: #ddd8c9;
}
.timeline li:last-child::before { display: none; }
.dot { width: 12px; height: 12px; border-radius: 50%; background: #cfc9b8; margin-top: 4px; z-index: 1; flex: none; }
li.done .dot { background: #2f6b4a; }
li.active .dot { background: #1d3f91; box-shadow: 0 0 0 4px rgba(29, 63, 145, 0.15); }
li.stop .dot { background: #b0443c; }
.content { display: grid; gap: 2px; }
.content small { color: #7c837b; font-size: 12px; }
.muted { color: #a8a294; }
li.pending .content strong { color: #9a958a; font-weight: 600; }
</style>
