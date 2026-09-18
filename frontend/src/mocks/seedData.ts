// 本地种子数据：与后端 config/SeedData 同口径，仅在后端不可达时兜底展示。
export const mockData = {
  "warehouse": [
    { "id": 1, "name": "东部中心仓", "district": "东城区", "address": "东城物流园 1 号", "manager_id": 1, "capacity_level": 92, "contact_phone": "13800000001", "status": "ACTIVE" },
    { "id": 2, "name": "城西应急仓", "district": "西城区", "address": "西城储备基地 3 号", "manager_id": 2, "capacity_level": 104, "contact_phone": "13800000002", "status": "ACTIVE" },
    { "id": 3, "name": "北山前置仓", "district": "北山区", "address": "北山街道仓库", "manager_id": 3, "capacity_level": 116, "contact_phone": "13800000003", "status": "STANDBY" }
  ],
  "supplyItem": [
    { "id": 1, "sku_code": "SKU-WATER-500", "name": "矿泉水 500ml", "category": "WATER", "unit": "瓶", "safety_stock": 500, "expire_days": 365, "storage_requirement": "常温避光" },
    { "id": 2, "sku_code": "SKU-FOOD-BISC", "name": "压缩饼干", "category": "FOOD", "unit": "箱", "safety_stock": 200, "expire_days": 540, "storage_requirement": "干燥通风" },
    { "id": 3, "sku_code": "SKU-MED-KIT", "name": "急救包", "category": "MEDICAL", "unit": "套", "safety_stock": 100, "expire_days": 730, "storage_requirement": "常温防潮" },
    { "id": 4, "sku_code": "SKU-SHELTER-TENT", "name": "应急帐篷", "category": "SHELTER", "unit": "顶", "safety_stock": 50, "expire_days": 1825, "storage_requirement": "立库码放" },
    { "id": 5, "sku_code": "SKU-TOOL-ROPE", "name": "救生绳", "category": "RESCUE_TOOL", "unit": "根", "safety_stock": 80, "expire_days": 1095, "storage_requirement": "常温" }
  ],
  "inventoryBatch": [
    { "id": 1, "warehouse_id": 1, "supply_item_id": 1, "batch_no": "BATCH-W001", "quantity": 100, "reserved_quantity": 30, "available_quantity": 70, "expire_at": "2026-10-01", "inbound_source": "政府储备入库", "quality_status": "QUALIFIED" },
    { "id": 2, "warehouse_id": 1, "supply_item_id": 1, "batch_no": "BATCH-W002", "quantity": 50, "reserved_quantity": 0, "available_quantity": 50, "expire_at": "2026-09-25", "inbound_source": "社会捐赠入库", "quality_status": "QUALIFIED" },
    { "id": 3, "warehouse_id": 1, "supply_item_id": 1, "batch_no": "BATCH-W003", "quantity": 80, "reserved_quantity": 0, "available_quantity": 80, "expire_at": "2026-10-01", "inbound_source": "采购入库", "quality_status": "QUALIFIED" },
    { "id": 4, "warehouse_id": 1, "supply_item_id": 2, "batch_no": "BATCH-F001", "quantity": 30, "reserved_quantity": 0, "available_quantity": 30, "expire_at": "2026-11-15", "inbound_source": "采购入库", "quality_status": "QUALIFIED" },
    { "id": 5, "warehouse_id": 1, "supply_item_id": 3, "batch_no": "BATCH-M001", "quantity": 60, "reserved_quantity": 20, "available_quantity": 40, "expire_at": "2027-01-01", "inbound_source": "采购入库", "quality_status": "QUALIFIED" },
    { "id": 6, "warehouse_id": 1, "supply_item_id": 3, "batch_no": "BATCH-M002", "quantity": 40, "reserved_quantity": 0, "available_quantity": 40, "expire_at": "2026-08-01", "inbound_source": "回收复检", "quality_status": "QUARANTINE" },
    { "id": 7, "warehouse_id": 2, "supply_item_id": 1, "batch_no": "BATCH-W101", "quantity": 200, "reserved_quantity": 0, "available_quantity": 200, "expire_at": "2026-12-01", "inbound_source": "政府储备入库", "quality_status": "QUALIFIED" },
    { "id": 8, "warehouse_id": 2, "supply_item_id": 4, "batch_no": "BATCH-S101", "quantity": 25, "reserved_quantity": 0, "available_quantity": 25, "expire_at": "2028-05-01", "inbound_source": "采购入库", "quality_status": "QUALIFIED" },
    { "id": 9, "warehouse_id": 3, "supply_item_id": 5, "batch_no": "BATCH-R201", "quantity": 90, "reserved_quantity": 0, "available_quantity": 90, "expire_at": "2027-06-30", "inbound_source": "采购入库", "quality_status": "QUALIFIED" }
  ],
  "shelter": [
    { "id": 1, "name": "滨河社区安置点", "district": "东城区", "capacity": 300, "current_population": 120, "contact_person": "陈静", "risk_level": "MEDIUM", "open_status": "OPEN" },
    { "id": 2, "name": "体育馆安置点", "district": "西城区", "capacity": 800, "current_population": 260, "contact_person": "刘洋", "risk_level": "LOW", "open_status": "OPEN" },
    { "id": 3, "name": "学校临时安置点", "district": "北山区", "capacity": 500, "current_population": 0, "contact_person": "赵鹏", "risk_level": "HIGH", "open_status": "STANDBY" }
  ],
  "dispatchEvent": [
    { "id": 1, "name": "台风“海葵”应急响应", "disaster_type": "台风", "level": "LEVEL_II", "status": "RESPONDING", "occurred_at": "2026-09-16T08:00:00Z", "description": "沿海街道转移安置，需调拨饮用水与食品。" },
    { "id": 2, "name": "城区内涝应急", "disaster_type": "内涝", "level": "LEVEL_III", "status": "RESPONDING", "occurred_at": "2026-09-17T13:30:00Z", "description": "低洼片区积水，安置点接收转移群众。" },
    { "id": 3, "name": "北山山体滑坡", "disaster_type": "地质灾害", "level": "LEVEL_IV", "status": "CLOSED", "occurred_at": "2026-08-28T02:10:00Z", "description": "滑坡点已处置完毕，转入复盘。" }
  ],
  "dispatchOrder": [
    {
      "id": 1, "event_id": 1, "event_name": "台风“海葵”应急响应", "source_warehouse_id": 1, "source_warehouse_name": "东部中心仓",
      "shelter_id": 1, "shelter_name": "滨河社区安置点", "priority": "HIGH", "status": "SUBMITTED",
      "requested_by": "张磊", "approved_by": null, "dispatched_at": null, "created_at": "2026-09-17T09:00:00Z",
      "lines": [
        { "id": 1, "dispatch_order_id": 1, "supply_item_id": 1, "supply_item_name": "矿泉水 500ml", "unit": "瓶", "requested_quantity": 120, "reserved_quantity": 0, "consumed_quantity": 0, "available_quantity": 200, "shortage_quantity": 0 }
      ],
      "total_requested": 120, "total_reserved": 0, "total_available": 200, "total_shortage": 0
    },
    {
      "id": 2, "event_id": 1, "event_name": "台风“海葵”应急响应", "source_warehouse_id": 1, "source_warehouse_name": "东部中心仓",
      "shelter_id": 2, "shelter_name": "体育馆安置点", "priority": "MEDIUM", "status": "APPROVED",
      "requested_by": "李芳", "approved_by": "王敏", "dispatched_at": null, "created_at": "2026-09-16T15:00:00Z",
      "lines": [
        { "id": 2, "dispatch_order_id": 2, "supply_item_id": 1, "supply_item_name": "矿泉水 500ml", "unit": "瓶", "requested_quantity": 30, "reserved_quantity": 30, "consumed_quantity": 0, "available_quantity": 200, "shortage_quantity": 0 },
        { "id": 3, "dispatch_order_id": 2, "supply_item_id": 3, "supply_item_name": "急救包", "unit": "套", "requested_quantity": 20, "reserved_quantity": 20, "consumed_quantity": 0, "available_quantity": 40, "shortage_quantity": 0 }
      ],
      "total_requested": 50, "total_reserved": 50, "total_available": 240, "total_shortage": 0
    },
    {
      "id": 4, "event_id": 1, "event_name": "台风“海葵”应急响应", "source_warehouse_id": 1, "source_warehouse_name": "东部中心仓",
      "shelter_id": 1, "shelter_name": "滨河社区安置点", "priority": "HIGH", "status": "SUBMITTED",
      "requested_by": "张磊", "approved_by": null, "dispatched_at": null, "created_at": "2026-09-17T11:00:00Z",
      "lines": [
        { "id": 5, "dispatch_order_id": 4, "supply_item_id": 2, "supply_item_name": "压缩饼干", "unit": "箱", "requested_quantity": 50, "reserved_quantity": 0, "consumed_quantity": 0, "available_quantity": 30, "shortage_quantity": 20 }
      ],
      "total_requested": 50, "total_reserved": 0, "total_available": 30, "total_shortage": 20
    }
  ]
} as const;
