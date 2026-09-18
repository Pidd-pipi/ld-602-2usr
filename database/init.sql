CREATE TABLE IF NOT EXISTS warehouse (
  id INTEGER PRIMARY KEY,
  name TEXT,
  district TEXT,
  address TEXT,
  manager_id TEXT,
  capacity_level TEXT,
  contact_phone TEXT,
  status TEXT
);

CREATE TABLE IF NOT EXISTS supply_item (
  id INTEGER PRIMARY KEY,
  sku_code TEXT,
  name TEXT,
  category TEXT,
  unit TEXT,
  safety_stock TEXT,
  expire_days TEXT,
  storage_requirement TEXT
);

-- 库存批次：reserved_quantity 为调拨预占量，可用量 = quantity - reserved_quantity
CREATE TABLE IF NOT EXISTS inventory_batch (
  id INTEGER PRIMARY KEY,
  warehouse_id TEXT,
  supply_item_id TEXT,
  batch_no TEXT,
  quantity TEXT,
  reserved_quantity TEXT,
  expire_at TEXT,
  inbound_source TEXT,
  quality_status TEXT
);

CREATE TABLE IF NOT EXISTS shelter (
  id INTEGER PRIMARY KEY,
  name TEXT,
  district TEXT,
  capacity TEXT,
  current_population TEXT,
  contact_person TEXT,
  risk_level TEXT,
  open_status TEXT
);

-- 灾害事件：事件详情聚合其下调拨单
CREATE TABLE IF NOT EXISTS dispatch_event (
  id INTEGER PRIMARY KEY,
  name TEXT,
  disaster_type TEXT,
  level TEXT,
  status TEXT,
  occurred_at TEXT,
  description TEXT
);

CREATE TABLE IF NOT EXISTS dispatch_order (
  id INTEGER PRIMARY KEY,
  event_id TEXT,
  source_warehouse_id TEXT,
  shelter_id TEXT,
  priority TEXT,
  status TEXT,
  requested_by TEXT,
  approved_by TEXT,
  dispatched_at TEXT,
  created_at TEXT
);

-- 调拨单物资行
CREATE TABLE IF NOT EXISTS dispatch_line (
  id INTEGER PRIMARY KEY,
  dispatch_order_id TEXT,
  supply_item_id TEXT,
  quantity TEXT
);

-- 库存预占记录：RESERVED 预占中 / CONSUMED 已实扣 / RELEASED 已释放
CREATE TABLE IF NOT EXISTS dispatch_reservation (
  id INTEGER PRIMARY KEY,
  dispatch_order_id TEXT,
  dispatch_line_id TEXT,
  batch_id TEXT,
  quantity TEXT,
  status TEXT,
  created_at TEXT
);

CREATE TABLE IF NOT EXISTS audit_log (
  id INTEGER PRIMARY KEY,
  actor TEXT,
  action TEXT,
  target_type TEXT,
  target_id TEXT,
  created_at TEXT
);
