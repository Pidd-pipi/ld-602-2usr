// 本地种子数据：结构与后端 /api 响应一致，后端不可达时作为离线回退
export const mockData = {
  "warehouse": [
    {
      "id": 1,
      "name": "城北应急一号仓",
      "district": "城北区",
      "address": "城北区救灾路 8 号",
      "manager_id": 101,
      "capacity_level": 3,
      "contact_phone": "0571-88000001",
      "status": "ACTIVE"
    },
    {
      "id": 2,
      "name": "城南应急二号仓",
      "district": "城南区",
      "address": "城南区应急大道 12 号",
      "manager_id": 102,
      "capacity_level": 2,
      "contact_phone": "0571-88000002",
      "status": "ACTIVE"
    }
  ],
  "supplyItem": [
    { "id": 1, "sku_code": "WATER-550", "name": "瓶装饮用水 550ml", "category": "WATER", "unit": "瓶", "safety_stock": 200, "expire_days": 720, "storage_requirement": "常温避晒" },
    { "id": 2, "sku_code": "FOOD-MRE", "name": "自热应急口粮", "category": "FOOD", "unit": "份", "safety_stock": 150, "expire_days": 540, "storage_requirement": "阴凉干燥" },
    { "id": 3, "sku_code": "MED-BAND", "name": "医用急救包", "category": "MEDICAL", "unit": "个", "safety_stock": 50, "expire_days": 1080, "storage_requirement": "防潮" },
    { "id": 4, "sku_code": "SHELTER-TENT", "name": "折叠救灾帐篷", "category": "SHELTER", "unit": "顶", "safety_stock": 10, "expire_days": 1800, "storage_requirement": "常温" }
  ],
  "inventoryBatch": [
    { "id": 1, "warehouse_id": 1, "supply_item_id": 1, "batch_no": "W20260601", "quantity": 100, "reserved_quantity": 0, "available_quantity": 100, "expire_at": "2026-12-01", "inbound_source": "市级调拨入库", "quality_status": "QUALIFIED" },
    { "id": 2, "warehouse_id": 1, "supply_item_id": 1, "batch_no": "W20260901", "quantity": 120, "reserved_quantity": 0, "available_quantity": 120, "expire_at": "2027-03-01", "inbound_source": "社会捐赠", "quality_status": "QUALIFIED" },
    { "id": 3, "warehouse_id": 1, "supply_item_id": 1, "batch_no": "W20260902", "quantity": 60, "reserved_quantity": 0, "available_quantity": 60, "expire_at": "2027-03-01", "inbound_source": "市级调拨入库", "quality_status": "QUALIFIED" },
    { "id": 4, "warehouse_id": 1, "supply_item_id": 1, "batch_no": "W20270101", "quantity": 80, "reserved_quantity": 0, "available_quantity": 80, "expire_at": "2027-06-01", "inbound_source": "采购入库", "quality_status": "QUALIFIED" },
    { "id": 5, "warehouse_id": 1, "supply_item_id": 2, "batch_no": "F20260801", "quantity": 70, "reserved_quantity": 0, "available_quantity": 70, "expire_at": "2026-11-01", "inbound_source": "采购入库", "quality_status": "QUALIFIED" },
    { "id": 6, "warehouse_id": 1, "supply_item_id": 2, "batch_no": "F20260901", "quantity": 60, "reserved_quantity": 0, "available_quantity": 60, "expire_at": "2027-02-01", "inbound_source": "市级调拨入库", "quality_status": "QUALIFIED" },
    { "id": 7, "warehouse_id": 1, "supply_item_id": 3, "batch_no": "M20260901", "quantity": 40, "reserved_quantity": 0, "available_quantity": 40, "expire_at": "2028-01-01", "inbound_source": "采购入库", "quality_status": "QUALIFIED" },
    { "id": 8, "warehouse_id": 2, "supply_item_id": 1, "batch_no": "W20260815", "quantity": 50, "reserved_quantity": 0, "available_quantity": 50, "expire_at": "2027-01-15", "inbound_source": "采购入库", "quality_status": "QUALIFIED" }
  ],
  "shelter": [
    { "id": 1, "name": "城北实验学校避难点", "district": "城北区", "capacity": 800, "current_population": 320, "contact_person": "王主任", "risk_level": "HIGH", "open_status": "OPEN" },
    { "id": 2, "name": "城南体育馆避难点", "district": "城南区", "capacity": 1200, "current_population": 540, "contact_person": "李馆长", "risk_level": "MEDIUM", "open_status": "OPEN" }
  ],
  "disasterEvent": [
    { "id": 1, "name": "城北区暴雨内涝", "event_type": "FLOOD", "district": "城北区", "level": "HIGH", "occurred_at": "2026-09-15 08:20:00", "status": "ACTIVE", "description": "连续强降雨导致城北片区内涝，需紧急调拨生活与救灾物资。" },
    { "id": 2, "name": "城南区地质隐患排查", "event_type": "LANDSLIDE", "district": "城南区", "level": "MEDIUM", "occurred_at": "2026-09-16 14:00:00", "status": "ACTIVE", "description": "山体滑坡风险点周边群众转移安置。" }
  ],
  "dispatchOrder": [
    {
      "id": 1,
      "event_id": 1,
      "source_warehouse_id": 1,
      "shelter_id": 1,
      "priority": "HIGH",
      "status": "SUBMITTED",
      "requested_by": "street-admin",
      "approved_by": null,
      "requested_quantity": 150,
      "reserved_quantity": 0,
      "shortage_quantity": 0,
      "lines": [
        { "id": 1, "supply_item_id": 1, "supply_item_name": "瓶装饮用水 550ml", "unit": "瓶", "quantity": 150, "reserved_quantity": 0, "shortage_quantity": 0 }
      ],
      "reservations": []
    }
  ]
} as const;
