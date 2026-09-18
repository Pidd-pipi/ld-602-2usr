import { computed, ref, toValue, type MaybeRefOrGetter } from "vue";

/** 通用分页：pageRows 随响应式数据源变化自动更新 */
export function usePagination<T>(rows: MaybeRefOrGetter<T[]>, pageSizeValue = 8) {
  const page = ref(1);
  const pageSize = ref(pageSizeValue);
  const all = computed(() => toValue(rows) ?? []);
  const total = computed(() => all.value.length);
  const pageRows = computed(() =>
    all.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value));
  return { page, pageSize, pageRows, total };
}
