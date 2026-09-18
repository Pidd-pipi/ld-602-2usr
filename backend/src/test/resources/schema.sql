-- H2 (MySQL 兼容模式) 测试结构，与 database/init.sql 对齐
CREATE TABLE warehouse (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  district VARCHAR(64),
  address VARCHAR(255),
  manager_id BIGINT,
  capacity_level INT,
  contact_phone VARCHAR(32),
  status VARCHAR(32)
);
CREATE TABLE supply_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sku_code VARCHAR(64),
  name VARCHAR(128),
  category VARCHAR(32),
  unit VARCHAR(16),
  safety_stock INT,
  expire_days INT,
  storage_requirement VARCHAR(255)
);
CREATE TABLE inventory_batch (
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
  UNIQUE (warehouse_id, supply_item_id, batch_no)
);
CREATE TABLE shelter (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128),
  district VARCHAR(64),
  capacity INT,
  current_population INT,
  contact_person VARCHAR(64),
  risk_level VARCHAR(16),
  open_status VARCHAR(32)
);
CREATE TABLE disaster_event (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(128),
  event_type VARCHAR(32),
  district VARCHAR(64),
  level VARCHAR(16),
  occurred_at DATETIME,
  status VARCHAR(32),
  description VARCHAR(512)
);
CREATE TABLE dispatch_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  event_id BIGINT,
  source_warehouse_id BIGINT NOT NULL,
  shelter_id BIGINT NOT NULL,
  priority VARCHAR(16),
  status VARCHAR(32) NOT NULL,
  requested_by VARCHAR(64),
  approved_by VARCHAR(64),
  created_at DATETIME,
  updated_at DATETIME,
  submitted_at DATETIME,
  approved_at DATETIME,
  dispatched_at DATETIME,
  received_at DATETIME,
  rejected_at DATETIME,
  cancelled_at DATETIME,
  reject_reason VARCHAR(255),
  cancel_reason VARCHAR(255),
  version INT NOT NULL DEFAULT 0
);
CREATE TABLE dispatch_line (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  supply_item_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  reserved_quantity INT NOT NULL DEFAULT 0,
  shortage_quantity INT NOT NULL DEFAULT 0
);
CREATE TABLE dispatch_reservation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  line_id BIGINT NOT NULL,
  batch_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'RESERVED',
  created_at DATETIME,
  consumed_at DATETIME,
  released_at DATETIME
);
CREATE TABLE inventory_transaction (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  batch_id BIGINT NOT NULL,
  order_id BIGINT,
  supply_item_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  change_type VARCHAR(16),
  change_quantity INT,
  balance_after INT,
  reserved_after INT,
  created_at DATETIME,
  actor VARCHAR(64)
);
CREATE TABLE audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  actor VARCHAR(64),
  action VARCHAR(64),
  target_type VARCHAR(32),
  target_id VARCHAR(64),
  detail VARCHAR(512),
  created_at DATETIME
);
