-- 测试种子：与 database/init.sql 保持同一业务场景
INSERT INTO warehouse (id, name, district, address, manager_id, capacity_level, contact_phone, status)
VALUES (1, '城北应急一号仓', '城北区', '救灾路8号', 101, 3, '0571-88000001', 'ACTIVE');
INSERT INTO supply_item (id, sku_code, name, category, unit, safety_stock, expire_days, storage_requirement)
VALUES
  (1, 'WATER-550', '瓶装饮用水550ml', 'WATER', '瓶', 200, 720, '常温避晒'),
  (2, 'FOOD-MRE', '自热应急口粮', 'FOOD', '份', 150, 540, '阴凉干燥');
INSERT INTO shelter (id, name, district, capacity, current_population, contact_person, risk_level, open_status)
VALUES (1, '城北实验学校避难点', '城北区', 800, 320, '王主任', 'HIGH', 'OPEN');
INSERT INTO disaster_event (id, name, event_type, district, level, occurred_at, status, description)
VALUES (1, '城北区暴雨内涝', 'FLOOD', '城北区', 'HIGH', '2026-09-15 08:20:00', 'ACTIVE', '测试事件');
-- 水：同到期日批次按 batch_no 升序
INSERT INTO inventory_batch (id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, expire_at, inbound_source, quality_status, version)
VALUES
  (1, 1, 1, 'W20260601', 100, 0, '2026-12-01 00:00:00', '市级调拨入库', 'QUALIFIED', 0),
  (2, 1, 1, 'W20260901', 120, 0, '2027-03-01 00:00:00', '社会捐赠', 'QUALIFIED', 0),
  (3, 1, 1, 'W20260902', 60, 0, '2027-03-01 00:00:00', '市级调拨入库', 'QUALIFIED', 0),
  (4, 1, 1, 'W20270101', 80, 0, '2027-06-01 00:00:00', '采购入库', 'QUALIFIED', 0);
-- 口粮：总量 130，申请 150 时缺口 20
INSERT INTO inventory_batch (id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, expire_at, inbound_source, quality_status, version)
VALUES
  (5, 1, 2, 'F20260801', 70, 0, '2026-11-01 00:00:00', '采购入库', 'QUALIFIED', 0),
  (6, 1, 2, 'F20260901', 60, 0, '2027-02-01 00:00:00', '市级调拨入库', 'QUALIFIED', 0),
  (7, 1, 3, 'M20260901', 40, 0, '2028-01-01 00:00:00', '采购入库', 'QUALIFIED', 0);
