<script setup lang="ts">
import { computed } from "vue";
import { DispatchStatusText, type DispatchStatus } from "../../constants/DispatchStatus";

const props = defineProps<{ value: string }>();

const isDispatchStatus = computed(() => props.value in DispatchStatusText);
const text = computed(() =>
  isDispatchStatus.value ? DispatchStatusText[props.value as DispatchStatus] : props.value.replace(/_/g, " ")
);
const tone = computed(() => {
  switch (props.value) {
    case "SUBMITTED":
      return "warn";
    case "APPROVED":
      return "info";
    case "DISPATCHED":
      return "primary";
    case "RECEIVED":
      return "ok";
    case "REJECTED":
      return "danger";
    case "CANCELLED":
    case "DRAFT":
      return "muted";
    default:
      return "";
  }
});
</script>

<template>
  <span class="badge" :class="tone ? `badge-${tone}` : ''">{{ text }}</span>
</template>
