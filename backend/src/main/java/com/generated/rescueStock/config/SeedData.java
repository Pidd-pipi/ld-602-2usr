package com.generated.rescueStock.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import com.generated.rescueStock.constants.DispatchStatus;
import com.generated.rescueStock.constants.ReservationStatus;
import com.generated.rescueStock.models.DispatchEvent;
import com.generated.rescueStock.models.DispatchLine;
import com.generated.rescueStock.models.DispatchOrder;
import com.generated.rescueStock.models.DispatchReservation;
import com.generated.rescueStock.models.DispatchStatusEvent;
import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.Shelter;
import com.generated.rescueStock.models.SupplyItem;
import com.generated.rescueStock.models.Warehouse;
import com.generated.rescueStock.repositories.DispatchEventRepository;
import com.generated.rescueStock.repositories.DispatchOrderRepository;
import com.generated.rescueStock.repositories.DispatchReservationRepository;
import com.generated.rescueStock.repositories.InventoryBatchRepository;
import com.generated.rescueStock.repositories.ShelterRepository;
import com.generated.rescueStock.repositories.SupplyItemRepository;
import com.generated.rescueStock.repositories.WarehouseRepository;

// 进程内种子数据：服务启动时写入内存仓库，页面刷新后从服务端回读同一份状态。
@Configuration
public class SeedData {
  private final WarehouseRepository warehouseRepo;
  private final SupplyItemRepository supplyItemRepo;
  private final InventoryBatchRepository batchRepo;
  private final ShelterRepository shelterRepo;
  private final DispatchEventRepository eventRepo;
  private final DispatchOrderRepository orderRepo;
  private final DispatchReservationRepository reservationRepo;

  public SeedData(WarehouseRepository warehouseRepo, SupplyItemRepository supplyItemRepo,
      InventoryBatchRepository batchRepo, ShelterRepository shelterRepo, DispatchEventRepository eventRepo,
      DispatchOrderRepository orderRepo, DispatchReservationRepository reservationRepo) {
    this.warehouseRepo = warehouseRepo;
    this.supplyItemRepo = supplyItemRepo;
    this.batchRepo = batchRepo;
    this.shelterRepo = shelterRepo;
    this.eventRepo = eventRepo;
    this.orderRepo = orderRepo;
    this.reservationRepo = reservationRepo;
  }

  @PostConstruct
  public void seed() {
    warehouseRepo.save(new Warehouse(1, "东部中心仓", "东城区", "东城物流园 1 号", 1, 92, "13800000001", "ACTIVE"));
    warehouseRepo.save(new Warehouse(2, "城西应急仓", "西城区", "西城储备基地 3 号", 2, 104, "13800000002", "ACTIVE"));
    warehouseRepo.save(new Warehouse(3, "北山前置仓", "北山区", "北山街道仓库", 3, 116, "13800000003", "STANDBY"));

    supplyItemRepo.save(new SupplyItem(1, "SKU-WATER-500", "矿泉水 500ml", "WATER", "瓶", 500, 365, "常温避光"));
    supplyItemRepo.save(new SupplyItem(2, "SKU-FOOD-BISC", "压缩饼干", "FOOD", "箱", 200, 540, "干燥通风"));
    supplyItemRepo.save(new SupplyItem(3, "SKU-MED-KIT", "急救包", "MEDICAL", "套", 100, 730, "常温防潮"));
    supplyItemRepo.save(new SupplyItem(4, "SKU-SHELTER-TENT", "应急帐篷", "SHELTER", "顶", 50, 1825, "立库码放"));
    supplyItemRepo.save(new SupplyItem(5, "SKU-TOOL-ROPE", "救生绳", "RESCUE_TOOL", "根", 80, 1095, "常温"));

    // 批次：QUALIFIED 参与预占；QUARANTINE 不参与。同到期日按批次号升序。
    batchRepo.save(new InventoryBatch(1, 1, 1, "BATCH-W001", 100, 0, "2026-10-01", "政府储备入库", "QUALIFIED"));
    batchRepo.save(new InventoryBatch(2, 1, 1, "BATCH-W002", 50, 0, "2026-09-25", "社会捐赠入库", "QUALIFIED"));
    batchRepo.save(new InventoryBatch(3, 1, 1, "BATCH-W003", 80, 0, "2026-10-01", "采购入库", "QUALIFIED"));
    batchRepo.save(new InventoryBatch(4, 1, 2, "BATCH-F001", 30, 0, "2026-11-15", "采购入库", "QUALIFIED"));
    batchRepo.save(new InventoryBatch(5, 1, 3, "BATCH-M001", 60, 0, "2027-01-01", "采购入库", "QUALIFIED"));
    batchRepo.save(new InventoryBatch(6, 1, 3, "BATCH-M002", 40, 0, "2026-08-01", "回收复检", "QUARANTINE"));
    batchRepo.save(new InventoryBatch(7, 2, 1, "BATCH-W101", 200, 0, "2026-12-01", "政府储备入库", "QUALIFIED"));
    batchRepo.save(new InventoryBatch(8, 2, 4, "BATCH-S101", 25, 0, "2028-05-01", "采购入库", "QUALIFIED"));
    batchRepo.save(new InventoryBatch(9, 3, 5, "BATCH-R201", 90, 0, "2027-06-30", "采购入库", "QUALIFIED"));

    shelterRepo.save(new Shelter(1, "滨河社区安置点", "东城区", 300, 120, "陈静", "MEDIUM", "OPEN"));
    shelterRepo.save(new Shelter(2, "体育馆安置点", "西城区", 800, 260, "刘洋", "LOW", "OPEN"));
    shelterRepo.save(new Shelter(3, "学校临时安置点", "北山区", 500, 0, "赵鹏", "HIGH", "STANDBY"));

    eventRepo.save(new DispatchEvent(1, "台风“海葵”应急响应", "台风", "LEVEL_II", "RESPONDING",
        "2026-09-16T08:00:00Z", "沿海街道转移安置，需调拨饮用水与食品。"));
    eventRepo.save(new DispatchEvent(2, "城区内涝应急", "内涝", "LEVEL_III", "RESPONDING",
        "2026-09-17T13:30:00Z", "低洼片区积水，安置点接收转移群众。"));
    eventRepo.save(new DispatchEvent(3, "北山山体滑坡", "地质灾害", "LEVEL_IV", "CLOSED",
        "2026-08-28T02:10:00Z", "滑坡点已处置完毕，转入复盘。"));

    // 单 1：待审批，矿泉水 120 -> 审批时按 FEFO 预占 W002(50)+W001(70)。
    DispatchOrder order1 = new DispatchOrder(1, 1, 1, 1, "HIGH", DispatchStatus.SUBMITTED.name(), "张磊", null,
        null, "2026-09-17T09:00:00Z");
    order1.lines.add(new DispatchLine(1, 1, 1, 120));
    order1.history.add(new DispatchStatusEvent(DispatchStatus.DRAFT.name(), "张磊", "2026-09-17T08:50:00Z", "创建调拨单"));
    order1.history.add(new DispatchStatusEvent(DispatchStatus.SUBMITTED.name(), "张磊", "2026-09-17T09:00:00Z", "提交审批"));
    orderRepo.save(order1);

    // 单 2：已审批并预占（W001 30 + M001 20），演示预占展示与出库/释放。
    DispatchOrder order2 = new DispatchOrder(2, 1, 1, 2, "MEDIUM", DispatchStatus.APPROVED.name(), "李芳", "王敏",
        null, "2026-09-16T15:00:00Z");
    order2.lines.add(new DispatchLine(2, 2, 1, 30));
    order2.lines.add(new DispatchLine(3, 2, 3, 20));
    order2.history.add(new DispatchStatusEvent(DispatchStatus.DRAFT.name(), "李芳", "2026-09-16T14:40:00Z", "创建调拨单"));
    order2.history.add(new DispatchStatusEvent(DispatchStatus.SUBMITTED.name(), "李芳", "2026-09-16T14:50:00Z", "提交审批"));
    order2.history.add(new DispatchStatusEvent(DispatchStatus.APPROVED.name(), "王敏", "2026-09-16T15:00:00Z", "审批通过，完成库存预占"));
    orderRepo.save(order2);
    batchRepo.findById(1).reservedQuantity += 30;
    batchRepo.findById(5).reservedQuantity += 20;
    reservationRepo.save(new DispatchReservation(1, 2, 2, 1, 30, ReservationStatus.RESERVED.name(), "2026-09-16T15:00:00Z"));
    reservationRepo.save(new DispatchReservation(2, 2, 3, 5, 20, ReservationStatus.RESERVED.name(), "2026-09-16T15:00:00Z"));

    // 单 3：草稿，帐篷 10。
    DispatchOrder order3 = new DispatchOrder(3, 2, 2, 3, "LOW", DispatchStatus.DRAFT.name(), "赵鹏", null, null,
        "2026-09-17T10:20:00Z");
    order3.lines.add(new DispatchLine(4, 3, 4, 10));
    order3.history.add(new DispatchStatusEvent(DispatchStatus.DRAFT.name(), "赵鹏", "2026-09-17T10:20:00Z", "创建调拨单"));
    orderRepo.save(order3);

    // 单 4：待审批，压缩饼干 50 > 可用 30，演示整单失败并返回缺口。
    DispatchOrder order4 = new DispatchOrder(4, 1, 1, 1, "HIGH", DispatchStatus.SUBMITTED.name(), "张磊", null,
        null, "2026-09-17T11:00:00Z");
    order4.lines.add(new DispatchLine(5, 4, 2, 50));
    order4.history.add(new DispatchStatusEvent(DispatchStatus.DRAFT.name(), "张磊", "2026-09-17T10:50:00Z", "创建调拨单"));
    order4.history.add(new DispatchStatusEvent(DispatchStatus.SUBMITTED.name(), "张磊", "2026-09-17T11:00:00Z", "提交审批"));
    orderRepo.save(order4);

    orderRepo.ensureSeqAbove(4, 5);
    reservationRepo.ensureSeqAbove(2);
  }
}
