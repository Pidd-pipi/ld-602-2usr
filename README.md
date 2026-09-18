# 城市防灾应急物资调度系统

面向街道、社区和应急仓库的防灾物资储备与调拨平台，覆盖物资库存、避难点、事件响应和调拨审批。

## 快速启动

```bash
cp .env.example .env && docker compose up -d
```

## 访问地址或 CLI 示例

前端：<http://localhost:20102>

后端健康检查：<http://localhost:21102/health>

调拨预占闭环 API 示例：

```bash
# 创建调拨单（DRAFT）
curl -X POST http://localhost:21102/api/dispatch-order \
  -H 'Content-Type: application/json' \
  -d '{"event_id":1,"source_warehouse_id":1,"shelter_id":1,"priority":"HIGH","requested_by":"张磊","lines":[{"supply_item_id":1,"quantity":120}]}'
# 提交 -> 审批（按最早到期先出预占，不足则 409 返回缺口且不占用任何批次）
curl -X POST http://localhost:21102/api/dispatch-order/{id}/submit -H 'Content-Type: application/json' -d '{}'
curl -X POST http://localhost:21102/api/dispatch-order/{id}/approve -H 'Content-Type: application/json' -d '{"operator":"王敏"}'
# 出库确认（预占转实扣）/ 驳回或取消（释放全部预占）
curl -X POST http://localhost:21102/api/dispatch-order/{id}/dispatch -H 'Content-Type: application/json' -d '{}'
curl -X POST http://localhost:21102/api/dispatch-order/{id}/cancel -H 'Content-Type: application/json' -d '{}'
```

## 调拨库存预占闭环

调拨单状态机：`DRAFT → SUBMITTED → APPROVED(预占中) → DISPATCHED(已实扣) → RECEIVED`，`REJECTED` / `CANCELLED` 为终态。

- **审批通过（approve）**：从来源仓库 `QUALIFIED` 且可用量（`quantity - reserved_quantity`）大于 0 的批次中，按**最早到期先出**分配，到期日相同按**批次号升序**；任一物资行不足则**整单失败**，返回 `409 INSUFFICIENT_STOCK` 及 `details.shortages` 缺口明细，不占用任何批次。
- **出库确认（dispatch）**：该单全部 `RESERVED` 预占记录转为 `CONSUMED`，批次账面库存与预占量同步扣减。
- **驳回（reject）/ 取消（cancel）**：该单全部预占记录转为 `RELEASED`，批次预占量全部释放。
- **并发控制**：审批/出库/释放共用同一把公平锁，`检查可用 + 扣减可用` 原子化——同一批次的并发审批只有一单能成功，其余单按库存不足失败。
- **展示与回读**：调拨页 `/dispatch` 展示每单/每行的预占、可用、缺口及批次预占明细；事件页 `/events` 详情聚合同一份数据；状态保存在服务端，刷新页面后重新拉取、数据一致。

主要接口：`GET/POST /api/dispatch-order`、`GET /api/dispatch-order/{id}`、`POST /api/dispatch-order/{id}/{submit|approve|reject|cancel|dispatch|receive}`、`GET /api/inventory-batch?warehouse_id=`、`GET /api/dispatch-event`、`GET /api/dispatch-event/{id}`、`GET /api/audit-log`。

> 当前后端使用进程内内存仓库（`config/SeedData` 启动时播种），`database/init.sql` 维护了与之对应的目标表结构（含 `dispatch_line`、`dispatch_reservation`、`dispatch_event` 与 `inventory_batch.reserved_quantity`）。

## 本地开发方式

- 前端：`cd frontend && npm install && npm run dev`（`/api` 经 vite 代理到 `BACKEND_PORT`，默认 21102）
- 后端：进入 `backend` 后按技术栈运行开发命令，接口统一挂在 `/api`。

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3 + TypeScript + Vite + Element Plus + Pinia + ECharts |
| 后端 | Spring Boot 3 + Java 17 + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 部署 | Docker Compose |

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

- SupplyCategory: constants/SupplyCategory、types/SupplyCategory、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。
- DispatchStatus（DRAFT / SUBMITTED / APPROVED / DISPATCHED / RECEIVED / REJECTED / CANCELLED）: constants/DispatchStatus、types/DispatchStatus、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用；`CANCELLED` 为预占闭环新增终态，前后端常量、状态文案、StatusBadge 色调同步维护。
- ReservationStatus（RESERVED / CONSUMED / RELEASED）: 后端 constants/ReservationStatus、services/DispatchOrderService、repositories/DispatchReservationRepository，前端 types/DispatchReservation、BatchTable 预占明细列。
- DispatchPriority（HIGH / MEDIUM / LOW）: frontend/src/constants/DispatchPriority、constructors/DispatchOrderConstructor、DispatchPage 创建表单与列表展示。
- ShelterStatus: constants/ShelterStatus、types/ShelterStatus、constructors、logTemplates、errorMessages、筛选器、展示组件/控制器均有引用。

## 为什么会牵一发动全身

实体字段、枚举、日志模板、错误消息、构造器、筛选器和展示组件被刻意拆散到多个目录；修改一个状态值通常需要同步类型、构造器、服务、控制器、store、页面、README 与数据库种子。本次预占闭环即为例证：`CANCELLED` 终态与 `reserved_quantity` 字段贯穿了后端常量/模型/仓库/服务/DTO 构造器、前端类型/常量/组件/页面/种子数据与 `database/init.sql`。

## License

MIT
