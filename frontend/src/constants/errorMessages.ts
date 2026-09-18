export const ERROR_MESSAGES = {
  AUTH_REQUIRED: "请先登录后再继续操作",
  RBAC_DENIED: "当前角色没有执行该动作的权限",
  VALIDATION_FAILED: "表单字段缺失或格式错误",
  RATE_LIMITED: "请求过于频繁，请稍后再试",
  DISPATCH_NOT_FOUND: "调拨单不存在或已被删除",
  DISPATCH_STATUS_CONFLICT: "调拨单当前状态不允许执行该操作，请刷新后重试",
  RESERVATION_SHORTAGE: "来源仓库可用库存不足，整单预占失败，未占用任何批次",
  RESERVATION_NOT_READY: "调拨单尚未完成预占，无法出库确认",
  CONCURRENT_APPROVAL: "同一批次存在并发审批，本单未抢占到库存，请刷新后重试",
  INTERNAL_ERROR: "服务内部错误，请联系管理员"
};
