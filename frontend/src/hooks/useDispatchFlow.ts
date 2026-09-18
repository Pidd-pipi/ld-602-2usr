import { computed, ref } from "vue";
import { useDispatchOrderStore } from "../stores/DispatchOrderStore";
import { ApiError } from "../api/request";
import type { DispatchOrder, DispatchShortage } from "../types/DispatchOrder";

/**
 * 调拨闭环操作 hook：审批（FEFO 预占）/ 出库（转实扣）/ 驳回 / 取消 / 签收。
 * 统一处理 acting、错误与缺口明细；动作完成后回读详情，保证刷新可回读。
 */
export function useDispatchFlow() {
  const store = useDispatchOrderStore();
  const acting = ref(false);
  const errorMessage = ref("");
  const shortages = ref<DispatchShortage[]>([]);

  const totalShortage = computed(() =>
    shortages.value.reduce((sum, s) => sum + s.shortage_quantity, 0));

  async function run(fn: () => Promise<DispatchOrder>): Promise<DispatchOrder | null> {
    acting.value = true;
    errorMessage.value = "";
    shortages.value = [];
    try {
      return await fn();
    } catch (err) {
      if (err instanceof ApiError) {
        errorMessage.value = err.message;
        if (err.code === "RESERVATION_SHORTAGE" && Array.isArray(err.shortages)) {
          shortages.value = err.shortages as DispatchShortage[];
        }
      } else {
        errorMessage.value = err instanceof Error ? err.message : String(err);
      }
      return null;
    } finally {
      acting.value = false;
    }
  }

  return {
    acting,
    errorMessage,
    shortages,
    totalShortage,
    approve: (id: number, by: string) => run(() => store.approve(id, by)),
    confirmOutbound: (id: number, by: string) => run(() => store.confirmOutbound(id, by)),
    reject: (id: number, by: string, reason: string) => run(() => store.reject(id, by, reason)),
    cancel: (id: number, by: string, reason: string) => run(() => store.cancel(id, by, reason)),
    receive: (id: number, by: string) => run(() => store.receive(id, by)),
    clearFeedback: () => {
      errorMessage.value = "";
      shortages.value = [];
    }
  };
}
