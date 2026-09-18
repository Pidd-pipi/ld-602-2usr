<template><div class="empty">{{ text ?? "暂无数据" }}</div></template>

<script setup lang="ts">
defineProps<{ text?: string }>();
</script>

<style scoped>
.empty {
  border: 1px dashed #b8b09f;
  padding: 18px;
  border-radius: 8px;
  color: #8a8578;
  text-align: center;
  font-size: 13px;
}
</style>
