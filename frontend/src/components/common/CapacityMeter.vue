<script setup lang="ts">
import { computed } from "vue";

/** 避难点容量占用条 */
const props = defineProps<{ capacity: number; current: number }>();

const percent = computed(() => {
  if (!props.capacity) return 0;
  return Math.min(100, Math.round((props.current / props.capacity) * 100));
});
const level = computed(() => (percent.value >= 90 ? "full" : percent.value >= 70 ? "high" : "ok"));
</script>

<template>
  <div class="meter">
    <div class="track"><div :class="['fill', level]" :style="{ width: percent + '%' }" /></div>
    <small>{{ current }} / {{ capacity }} 人（{{ percent }}%）</small>
  </div>
</template>

<style scoped>
.meter { display: grid; gap: 4px; }
.track { height: 8px; border-radius: 999px; background: #ece8db; overflow: hidden; }
.fill { height: 100%; border-radius: 999px; background: #2f6b4a; }
.fill.high { background: #d39b46; }
.fill.full { background: #b0443c; }
small { color: #7c837b; font-size: 12px; }
</style>
