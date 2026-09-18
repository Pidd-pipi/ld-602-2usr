# 城市防灾应急物资调度系统

面向街道、社区和应急仓库的防灾物资储备与调拨平台，覆盖物资库存、避难点、事件响应和调拨审批。

## 快速启动

```bash
cp .env.example .env && docker compose up -d
```

## 访问地址或 CLI 示例

前端：<http://localhost:20102>

后端健康检查：<http://localhost:21102/health>


## 本地开发方式

- 前端：`cd frontend && npm install && npm run dev`
- 后端：进入 `backend` 后按技术栈运行开发命令，接口统一挂在 `/api`。


## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia + ECharts |
| 后端 | Spring Boot 3 + Java 17 + MyBatis-Plus + Spring JDBC |
| 数据库 | MySQL 8.0 |
| 部署 | Docker Compose |

## 调拨库存预占闭环（ld-602 核心流程）

审批 / 出库 / 驳回 / 取消围绕「批次预占」形成闭环，状态机为
`SUBMITTED → APPROVED → DISPATCHED → RECEIVED`，任意阶段可 `REJECTED`（审批阶段）或 `CANCELLED`（提交/已预占阶段）。

1. **审批通过（预占）**：在一个数据库事务内对来源仓库的合格批次加行锁（`SELECT … FOR UPDATE`），
   按 **最早到期先出（FEFO）** 分配；保质期（到期日）相同按 **批次号升序**。
2. **不足整单失败**：任意一行可用量不足，整张单预占失败并返回逐行缺口（`RESERVATION_SHORTAGE` + `shortages`），
   不写入任何预占、不占用任何批次。
3. **出库确认（实扣）**：`APPROVED → DISPATCHED`，预占明细转为实扣，批次总量与预占量同时下降。
4. **驳回 / 取消（释放）**：释放该单全部预占，批次预占量回补，明细置 `RELEASED`。
5. **并发安全**：同一批次的并发审批由行锁 + 乐观条件更新保证仅一单成功，另一单得到缺口/并发冲突。
6. **库存流水双写**：预占 / 释放 / 实扣同时写 `inventory_transaction` 与 `audit_log`。

相关后端入口：`services/DispatchOrderService.java`（事务编排）、`services/BatchAllocationService.java`（FEFO 计算）、
`repositories/InventoryBatchRepository.java`（`FOR UPDATE` 与版本更新）、`controllers/DispatchOrderController.java`。
前端入口：`pages/DispatchPage.vue`、`pages/EventsPage.vue`、`hooks/useDispatchFlow.ts`、`components/common/ReservationTable.vue`。

### 接口清单（统一前缀 `/api`）

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/dispatch-order` | 调拨单列表（聚合申请/预占/缺口） |
| GET | `/dispatch-order/{id}` | 详情：明细行 + FEFO 预占明细（刷新可回读） |
| POST | `/dispatch-order` | 创建并提交调拨单 |
| POST | `/dispatch-order/{id}/approve` | 审批通过并预占（不足返回 409 与缺口） |
| POST | `/dispatch-order/{id}/outbound` | 出库确认：预占转实扣 |
| POST | `/dispatch-order/{id}/reject` | 驳回并释放全部预占 |
| POST | `/dispatch-order/{id}/cancel` | 取消并释放全部预占 |
| POST | `/dispatch-order/{id}/receive` | 签收 |
| GET | `/event`、`/event/{id}` | 事件列表 / 事件详情（同步关联调拨单预占缺口） |
| GET | `/inventory-batch?warehouse_id=` | 批次总量/预占/可用 |
| GET | `/inventory-batch/availability` | 按仓库+物资汇总可用量 |

## 项目目录结构

```text
frontend/src/api, stores, types, constants, constructors, components/common, hooks, pages, router, utils, mocks
backend/src/routes, controllers, services, models, repositories, middlewares, constants, constructors, utils, types, config
```

## 环境变量说明

- `COMPOSE_PROJECT_NAME`: Compose 项目名，默认 `rescue-stock`
- `FRONTEND_PORT`: 前端端口，默认 `20102`
- `BACKEND_PORT`: 后端端口，默认 `21102`
- `DB_PORT`: 数据库宿主机端口
- `DB_USER/DB_PASSWORD/DB_NAME`: 本地数据库凭据

## Docker 部署说明

- 根 Compose 文件不写 `version`，顶层 `name: rescue-stock`。
- 容器名均使用 `${COMPOSE_PROJECT_NAME:-rescue-stock}` 前缀。
- 数据库使用命名卷，避免绑定中文路径。
- 常见问题：端口占用时修改 `.env` 中端口后重启；需要重置数据时执行 `docker compose down -v`。

## 枚举/常量出现位置清单

- SupplyCategory: 后端 `constants/SupplyCategory.java`；前端 `constants/SupplyCategory.ts`、`types/SupplyCategory.ts`；constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- DispatchStatus: 后端 `constants/DispatchStatus.java`（含新增 `CANCELLED`）；前端 `constants/DispatchStatus.ts`、`types/DispatchStatus.ts`；`utils/formatters.ts`、`components/common/StatusBadge.vue`、`ApprovalTimeline.vue`、`stores/DispatchOrderStore.ts`、`pages/DispatchPage.vue`、`pages/EventsPage.vue`、constructors、logTemplates、errorMessages 均有引用。
- ShelterStatus: 后端 `constants/ShelterStatus.java`；前端 `constants/ShelterStatus.ts`、`types/ShelterStatus.ts`；constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- QualityStatus（批次质量 `QUALIFIED/QUARANTINED/DAMAGED`）: 后端 `constants/QualityStatus.java`，`InventoryBatchRepository.lockAvailableBatchesForUpdate` 只分配 `QUALIFIED` 批次。
- ReservationStatus（预占明细 `RESERVED/CONSUMED/RELEASED`）: 后端 `constants/ReservationStatus.java`；前端 `constants/ReservationStatus.ts`、`types/DispatchOrder.ts`、`components/common/ReservationTable.vue`、`utils/formatters.ts`。
- TransactionType（库存流水 `RESERVE/RELEASE/CONSUME`）: 后端 `constants/TransactionType.java`、`services/DispatchOrderService.java`、`repositories/InventoryTransactionRepository.java`；前端 `constants/ReservationStatus.ts`。

## 为什么会牵一发动全身

实体字段、枚举、日志模板、错误消息、构造器、筛选器和展示组件被刻意拆散到多个目录；修改一个状态值通常需要同步类型、构造器、服务、控制器、store、页面、README 与数据库种子。

## License

MIT
