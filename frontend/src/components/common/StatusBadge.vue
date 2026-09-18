<script setup lang="ts">
import { computed } from "vue";
import { DispatchStatusText } from "../../constants/DispatchStatus";
import { ReservationStatusText } from "../../constants/ReservationStatus";

const props = defineProps<{ value: string; tone?: "status" | "plain" }>();

const TEXT_MAP: Record<string, string> = { ...DispatchStatusText, ...ReservationStatusText };

const label = computed(() => TEXT_MAP[props.value] ?? props.value.replace(/_/g, " "));
const cls = computed(() => ["badge", `tone-${(props.value ?? "").toLowerCase()}`]);
</script>

<template>
  <span :class="cls">{{ label }}</span>
</template>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  border-radius: 999px;
  background: #e4efe4;
  color: #244b31;
  padding: 2px 10px;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}
.tone-submitted { background: #fff3d6; color: #7d4d18; }
.tone-approved { background: #e2ecff; color: #1d3f91; }
.tone-dispatched { background: #e4efe4; color: #244b31; }
.tone-received { background: #dff0ec; color: #155e52; }
.tone-rejected, .tone-cancelled, .tone-released { background: #f8e2e0; color: #8c2f28; }
.tone-reserved { background: #e2ecff; color: #1d3f91; }
.tone-consumed { background: #e4efe4; color: #244b31; }
</style>
