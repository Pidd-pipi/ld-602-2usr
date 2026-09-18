-- rescue-stock 初始化脚本（MySQL 8.0）
-- 包含：仓库 / 物资 / 批次 / 避难点 / 灾害事件 / 调拨单 / 调拨行 / 预占明细 / 库存流水 / 审计日志

CREATE TABLE IF NOT EXISTS warehouse (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  district VARCHAR(64) NOT NULL,
  address VARCHAR(255),
  manager_id BIGINT,
  capacity_level INT NOT NULL DEFAULT 0,
  contact_phone VARCHAR(32),
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS supply_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sku_code VARCHAR(64) NOT NULL,
  name VARCHAR(128) NOT NULL,
  category VARCHAR(32) NOT NULL,
  unit VARCHAR(16) NOT NULL,
  safety_stock INT NOT NULL DEFAULT 0,
  expire_days INT NOT NULL DEFAULT 0,
  storage_requirement VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS inventory_batch (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  warehouse_id BIGINT NOT NULL,
  supply_item_id BIGINT NOT NULL,
  batch_no VARCHAR(64) NOT NULL,
  quantity INT NOT NULL DEFAULT 0,
  reserved_quantity INT NOT NULL DEFAULT 0,
  expire_at DATETIME NOT NULL,
  inbound_source VARCHAR(128),
  quality_status VARCHAR(32) NOT NULL DEFAULT 'QUALIFIED',
  version INT NOT NULL DEFAULT 0,
  CONSTRAINT ck_batch_qty CHECK (quantity >= 0),
  CONSTRAINT ck_batch_reserved CHECK (reserved_quantity >= 0 AND reserved_quantity <= quantity),
  UNIQUE KEY uk_batch_no (warehouse_id, supply_item_id, batch_no),
  KEY idx_batch_allocation (warehouse_id, supply_item_id, quality_status, expire_at, batch_no)
);

CREATE TABLE IF NOT EXISTS shelter (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  district VARCHAR(64) NOT NULL,
  capacity INT NOT NULL DEFAULT 0,
  current_population INT NOT NULL DEFAULT 0,
  contact_person VARCHAR(64),
  risk_level VARCHAR(16) NOT NULL DEFAULT 'LOW',
  open_status VARCHAR(32) NOT NULL DEFAULT 'STANDBY'
);

CREATE TABLE IF NOT EXISTS disaster_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  event_type VARCHAR(32) NOT NULL DEFAULT 'FLOOD',
  district VARCHAR(64) NOT NULL,
  level VARCHAR(16) NOT NULL DEFAULT 'MEDIUM',
  occurred_at DATETIME NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
  description VARCHAR(512)
);

CREATE TABLE IF NOT EXISTS dispatch_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_id BIGINT,
  source_warehouse_id BIGINT NOT NULL,
  shelter_id BIGINT NOT NULL,
  priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
  status VARCHAR(32) NOT NULL DEFAULT 'SUBMITTED',
  requested_by VARCHAR(64) NOT NULL,
  approved_by VARCHAR(64),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  submitted_at DATETIME,
  approved_at DATETIME,
  dispatched_at DATETIME,
  received_at DATETIME,
  rejected_at DATETIME,
  cancelled_at DATETIME,
  reject_reason VARCHAR(255),
  cancel_reason VARCHAR(255),
  version INT NOT NULL DEFAULT 0,
  KEY idx_dispatch_event (event_id),
  KEY idx_dispatch_status (status)
);

CREATE TABLE IF NOT EXISTS dispatch_line (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  supply_item_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  reserved_quantity INT NOT NULL DEFAULT 0,
  shortage_quantity INT NOT NULL DEFAULT 0,
  KEY idx_line_order (order_id)
);

-- 批次预占明细：审批通过时按 FEFO 写入，出库时转实扣，驳回/取消时释放
CREATE TABLE IF NOT EXISTS dispatch_reservation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  line_id BIGINT NOT NULL,
  batch_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'RESERVED', -- RESERVED / CONSUMED / RELEASED
  created_at DATETIME NOT NULL,
  consumed_at DATETIME,
  released_at DATETIME,
  KEY idx_reservation_order (order_id),
  KEY idx_reservation_batch (batch_id, status)
);

-- 库存流水：预占 / 释放 / 实扣 双写留痕
CREATE TABLE IF NOT EXISTS inventory_transaction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  batch_id BIGINT NOT NULL,
  order_id BIGINT,
  supply_item_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  change_type VARCHAR(16) NOT NULL, -- RESERVE / RELEASE / CONSUME
  change_quantity INT NOT NULL,
  balance_after INT NOT NULL,
  reserved_after INT NOT NULL,
  created_at DATETIME NOT NULL,
  actor VARCHAR(64),
  KEY idx_txn_batch (batch_id)
);

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  actor VARCHAR(64),
  action VARCHAR(64),
  target_type VARCHAR(32),
  target_id VARCHAR(64),
  detail VARCHAR(512),
  created_at DATETIME
);

-- ---------- 种子数据 ----------
INSERT INTO warehouse (id, name, district, address, manager_id, capacity_level, contact_phone, status)
VALUES
  (1, '城北应急一号仓', '城北区', '城北区救灾路 8 号', 101, 3, '0571-88000001', 'ACTIVE'),
  (2, '城南应急二号仓', '城南区', '城南区应急大道 12 号', 102, 2, '0571-88000002', 'ACTIVE')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO supply_item (id, sku_code, name, category, unit, safety_stock, expire_days, storage_requirement)
VALUES
  (1, 'WATER-550', '瓶装饮用水 550ml', 'WATER', '瓶', 200, 720, '常温避晒'),
  (2, 'FOOD-MRE', '自热应急口粮', 'FOOD', '份', 150, 540, '阴凉干燥'),
  (3, 'MED-BAND', '医用急救包', 'MEDICAL', '个', 50, 1080, '防潮'),
  (4, 'SHELTER-TENT', '折叠救灾帐篷', 'SHELTER', '顶', 10, 1800, '常温')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO shelter (id, name, district, capacity, current_population, contact_person, risk_level, open_status)
VALUES
  (1, '城北实验学校避难点', '城北区', 800, 320, '王主任', 'HIGH', 'OPEN'),
  (2, '城南体育馆避难点', '城南区', 1200, 540, '李馆长', 'MEDIUM', 'OPEN')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO disaster_event (id, name, event_type, district, level, occurred_at, status, description)
VALUES
  (1, '城北区暴雨内涝', 'FLOOD', '城北区', 'HIGH', '2026-09-15 08:20:00', 'ACTIVE', '连续强降雨导致城北片区内涝，需紧急调拨生活与救灾物资。'),
  (2, '城南区地质隐患排查', 'LANDSLIDE', '城南区', 'MEDIUM', '2026-09-16 14:00:00', 'ACTIVE', '山体滑坡风险点周边群众转移安置。')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 一号仓 饮用水：4 个批次（同到期日按批次号升序验证；早到期优先验证）
INSERT INTO inventory_batch (id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, expire_at, inbound_source, quality_status, version)
VALUES
  (1, 1, 1, 'W20260601', 100, 0, '2026-12-01 00:00:00', '市级调拨入库', 'QUALIFIED', 0),
  (2, 1, 1, 'W20260901', 120, 0, '2027-03-01 00:00:00', '社会捐赠', 'QUALIFIED', 0),
  (3, 1, 1, 'W20260902', 60, 0, '2027-03-01 00:00:00', '市级调拨入库', 'QUALIFIED', 0),
  (4, 1, 1, 'W20270101', 80, 0, '2027-06-01 00:00:00', '采购入库', 'QUALIFIED', 0),
  -- 一号仓 口粮：2 个批次，总量 130（用于验证缺口场景：申请 150 时缺口 20，整单失败）
  (5, 1, 2, 'F20260801', 70, 0, '2026-11-01 00:00:00', '采购入库', 'QUALIFIED', 0),
  (6, 1, 2, 'F20260901', 60, 0, '2027-02-01 00:00:00', '市级调拨入库', 'QUALIFIED', 0),
  -- 一号仓 急救包：1 个批次
  (7, 1, 3, 'M20260901', 40, 0, '2028-01-01 00:00:00', '采购入库', 'QUALIFIED', 0),
  -- 二号仓 饮用水（用于跨仓并发互不干扰）
  (8, 2, 1, 'W20260815', 50, 0, '2027-01-15 00:00:00', '采购入库', 'QUALIFIED', 0)
ON DUPLICATE KEY UPDATE batch_no = VALUES(batch_no);
