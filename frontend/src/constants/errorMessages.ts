export const ERROR_MESSAGES = {
  AUTH_REQUIRED: "请先登录后再继续操作",
  RBAC_DENIED: "当前角色没有执行该动作的权限",
  VALIDATION_FAILED: "表单字段缺失或格式错误",
  RATE_LIMITED: "请求过于频繁，请稍后再试",
  ORDER_NOT_FOUND: "调拨单不存在或已被清理",
  EVENT_NOT_FOUND: "事件不存在",
  INVALID_STATUS_TRANSITION: "当前状态不允许执行该操作",
  INSUFFICIENT_STOCK: "来源仓库可用库存不足，整单审批失败，未占用任何批次",
  INTERNAL_ERROR: "服务内部错误，请稍后再试"
};
