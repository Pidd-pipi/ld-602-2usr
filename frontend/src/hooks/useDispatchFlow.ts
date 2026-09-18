import { computed, ref } from "vue";
import { storeToRefs } from "pinia";
import type { DispatchAction } from "../api/DispatchOrder";
import type { DispatchOrder } from "../types/DispatchOrder";
import { useDispatchOrderStore } from "../stores/DispatchOrderStore";

// 调拨流转 hook：页面只声明动作，状态机校验与缺口反馈由后端返回。
const ACTION_TEXT: Record<DispatchAction, string> = {
  submit: "提交审批",
  approve: "审批通过",
  reject: "驳回",
  cancel: "取消",
  dispatch: "出库确认",
  receive: "签收"
};

// 每个状态允许的操作按钮。
const ACTIONS_BY_STATUS: Record<string, DispatchAction[]> = {
  DRAFT: ["submit", "cancel"],
  SUBMITTED: ["approve", "reject", "cancel"],
  APPROVED: ["dispatch", "reject", "cancel"],
  DISPATCHED: ["receive"]
};

export function useDispatchFlow() {
  const store = useDispatchOrderStore();
  const { acting, lastError, lastShortages } = storeToRefs(store);
  const feedback = ref<string | null>(null);

  async function runAction(order: DispatchOrder, action: DispatchAction, operator = "值班审批员") {
    feedback.value = null;
    try {
      await store.act(order.id, action, operator);
      feedback.value = `调拨单 #${order.id} ${ACTION_TEXT[action]}成功`;
      return true;
    } catch {
      // 缺口与错误消息已写入 store（lastError / lastShortages），页面直接展示。
      return false;
    }
  }

  function actionsOf(order: DispatchOrder) {
    return ACTIONS_BY_STATUS[order.status] ?? [];
  }

  return {
    acting: computed(() => acting.value),
    lastError: computed(() => lastError.value),
    lastShortages: computed(() => lastShortages.value),
    feedback,
    actionText: (action: DispatchAction) => ACTION_TEXT[action],
    actionsOf,
    runAction
  };
}
