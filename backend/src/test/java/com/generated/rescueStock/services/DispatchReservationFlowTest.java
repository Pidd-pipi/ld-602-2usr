package com.generated.rescueStock.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import com.generated.rescueStock.constants.ErrorCodes;
import com.generated.rescueStock.exceptions.ApiException;
import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.repositories.InventoryBatchRepository;
import com.generated.rescueStock.types.ApprovePayload;
import com.generated.rescueStock.types.CancelPayload;
import com.generated.rescueStock.types.DispatchLinePayload;
import com.generated.rescueStock.types.DispatchOrderPayload;
import com.generated.rescueStock.types.OutboundPayload;
import com.generated.rescueStock.types.RejectPayload;

@SpringBootTest
class DispatchReservationFlowTest {

  @Autowired
  private DispatchOrderService dispatchService;

  @Autowired
  private InventoryBatchRepository batchRepo;

  @Autowired
  private JdbcTemplate jdbc;

  /** 每个用例前清空流程数据并重置批次库存为种子值，保证并发等用例互不干扰 */
  @BeforeEach
  void resetData() {
    jdbc.update("DELETE FROM inventory_transaction");
    jdbc.update("DELETE FROM dispatch_reservation");
    jdbc.update("DELETE FROM dispatch_line");
    jdbc.update("DELETE FROM dispatch_order");
    jdbc.update("DELETE FROM audit_log");
    jdbc.update("DELETE FROM inventory_batch");
    jdbc.batchUpdate(
        "INSERT INTO inventory_batch (id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, expire_at, inbound_source, quality_status, version) "
            + "VALUES (1,1,1,'W20260601',100,0,'2026-12-01 00:00:00','市级调拨入库','QUALIFIED',0)",
        "INSERT INTO inventory_batch (id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, expire_at, inbound_source, quality_status, version) "
            + "VALUES (2,1,1,'W20260901',120,0,'2027-03-01 00:00:00','社会捐赠','QUALIFIED',0)",
        "INSERT INTO inventory_batch (id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, expire_at, inbound_source, quality_status, version) "
            + "VALUES (3,1,1,'W20260902',60,0,'2027-03-01 00:00:00','市级调拨入库','QUALIFIED',0)",
        "INSERT INTO inventory_batch (id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, expire_at, inbound_source, quality_status, version) "
            + "VALUES (4,1,1,'W20270101',80,0,'2027-06-01 00:00:00','采购入库','QUALIFIED',0)",
        "INSERT INTO inventory_batch (id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, expire_at, inbound_source, quality_status, version) "
            + "VALUES (5,1,2,'F20260801',70,0,'2026-11-01 00:00:00','采购入库','QUALIFIED',0)",
        "INSERT INTO inventory_batch (id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, expire_at, inbound_source, quality_status, version) "
            + "VALUES (6,1,2,'F20260901',60,0,'2027-02-01 00:00:00','市级调拨入库','QUALIFIED',0)",
        "INSERT INTO inventory_batch (id, warehouse_id, supply_item_id, batch_no, quantity, reserved_quantity, expire_at, inbound_source, quality_status, version) "
            + "VALUES (7,1,3,'M20260901',40,0,'2028-01-01 00:00:00','采购入库','QUALIFIED',0)");
  }

  private DispatchOrderPayload waterOrder(int quantity) {
    return new DispatchOrderPayload(1L, 1L, 1L, "HIGH", "tester",
        List.of(new DispatchLinePayload(1L, quantity)));
  }

  private int batchReserved(long batchId) {
    InventoryBatch b = batchRepo.findById(batchId);
    return b.reservedQuantity;
  }

  private int batchQuantity(long batchId) {
    return batchRepo.findById(batchId).quantity;
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> reservationsOf(Map<String, Object> detail) {
    return (List<Map<String, Object>>) detail.get("reservations");
  }

  // 1. FEFO：申请 150 瓶水，应先吃满最早到期批次 1（100），再取批次 2（50），批次 3/4 不动
  @Test
  void approve_allocatesByEarliestExpiryFirst() {
    Long id = dispatchService.create(waterOrder(150)).get("id") instanceof Number n ? n.longValue() : null;
    Map<String, Object> detail = dispatchService.approve(id, new ApprovePayload("approver-a"));

    assertEquals("APPROVED", detail.get("status"));
    assertEquals(0, detail.get("shortage_quantity"));
    assertEquals(150, detail.get("reserved_quantity"));
    assertEquals(100, batchReserved(1), "最早到期批次应被整批占用");
    assertEquals(50, batchReserved(2), "第二早到期批次占用剩余 50");
    assertEquals(0, batchReserved(3));
    assertEquals(0, batchReserved(4));

    List<Map<String, Object>> rs = reservationsOf(detail);
    assertEquals(2, rs.size());
    assertEquals(1L, ((Number) rs.get(0).get("batch_id")).longValue());
    assertEquals(2L, ((Number) rs.get(1).get("batch_id")).longValue());
  }

  // 2. 保质期（到期日）相同按批次号升序：批次 2、3 同为 2027-03-01
  //    在批次 1 已被上一测试类场景隔离的前提下，这里新申请只落在 2、3 时先 2 后 3。
  //    用申请 230（100+120+10）验证顺序 1 -> 2 -> 3
  @Test
  void approve_sameExpiryOrdersByBatchNoAsc() {
    Long id = createdId(waterOrder(230));
    Map<String, Object> detail = dispatchService.approve(id, new ApprovePayload("approver-b"));

    List<Map<String, Object>> rs = reservationsOf(detail);
    assertEquals(List.of(1L, 2L, 3L),
        rs.stream().map(r -> ((Number) r.get("batch_id")).longValue()).toList());
    assertEquals(10, batchReserved(3), "同到期日时 W20260902 只被占用余数 10");
    assertEquals(0, batchReserved(4));
  }

  // 3. 不足时整单失败并返回缺口，且不能占用任何批次
  @Test
  void approve_shortageFailsWholeOrderAndOccupiesNothing() {
    Long id = createdId(new DispatchOrderPayload(1L, 1L, 1L, "HIGH", "tester",
        List.of(new DispatchLinePayload(2L, 150)))); // 口粮总量 130，缺口 20

    ApiException ex = assertThrows(ApiException.class,
        () -> dispatchService.approve(id, new ApprovePayload("approver-c")));
    assertEquals(ErrorCodes.RESERVATION_SHORTAGE, ex.getCode());
    assertNotNull(ex.getDetails());
    assertEquals(20, ex.getDetails().get(0).get("shortage_quantity"));

    // 单据仍为待审批；批次 5、6 未被占用；无预占明细
    Map<String, Object> detail = dispatchService.detail(id);
    assertEquals("SUBMITTED", detail.get("status"));
    assertEquals(0, batchReserved(5));
    assertEquals(0, batchReserved(6));
    assertTrue(reservationsOf(detail).isEmpty(), "整单失败不得写入任何预占");
  }

  // 4. 驳回释放全部预占
  @Test
  void reject_releasesAllReservations() {
    Long id = createdId(waterOrder(150));
    dispatchService.approve(id, new ApprovePayload("approver-d"));
    assertEquals(100, batchReserved(1));

    Map<String, Object> detail = dispatchService.reject(id, new RejectPayload("approver-d", "信息不全"));
    assertEquals("REJECTED", detail.get("status"));
    assertEquals(0, batchReserved(1));
    assertEquals(0, batchReserved(2));
    assertTrue(reservationsOf(detail).stream().allMatch(r -> "RELEASED".equals(r.get("status"))));
  }

  // 5. 取消释放全部预占
  @Test
  void cancel_releasesAllReservations() {
    Long id = createdId(waterOrder(80));
    dispatchService.approve(id, new ApprovePayload("approver-e"));
    assertEquals(80, batchReserved(1));

    Map<String, Object> detail = dispatchService.cancel(id, new CancelPayload("street-admin", "需求取消"));
    assertEquals("CANCELLED", detail.get("status"));
    assertEquals(0, batchReserved(1));
    assertEquals(0, detail.get("reserved_quantity"));
  }

  // 6. 出库确认：预占转实扣（总量与预占量同步下降，明细 CONSUMED）
  @Test
  void outbound_convertsReservationToActualDeduction() {
    Long id = createdId(waterOrder(150));
    dispatchService.approve(id, new ApprovePayload("approver-f"));

    Map<String, Object> detail = dispatchService.confirmOutbound(id, new OutboundPayload("keeper-f"));
    assertEquals("DISPATCHED", detail.get("status"));
    assertEquals(0, batchQuantity(1), "批次1实扣100后剩余0");
    assertEquals(0, batchReserved(1));
    assertEquals(70, batchQuantity(2), "批次2实扣50后剩余70");
    assertEquals(0, batchReserved(2));
    assertTrue(reservationsOf(detail).stream().allMatch(r -> "CONSUMED".equals(r.get("status"))));
  }

  // 7. 刷新后仍能回读：先审批、释放，再按 id 重新查询，状态与历史明细仍在
  @Test
  void detail_isReadableAfterRefresh() {
    Long id = createdId(waterOrder(60));
    dispatchService.approve(id, new ApprovePayload("approver-g"));
    dispatchService.confirmOutbound(id, new OutboundPayload("keeper-g"));

    Map<String, Object> reloaded = dispatchService.detail(id); // 模拟刷新后重新拉取
    assertEquals("DISPATCHED", reloaded.get("status"));
    assertEquals(60, reloaded.get("reserved_quantity") == null ? 0 : reloaded.get("reserved_quantity"));
    List<Map<String, Object>> rs = reservationsOf(reloaded);
    assertEquals(1, rs.size());
    assertEquals("CONSUMED", rs.get(0).get("status"));
    assertEquals("W20260601", rs.get(0).get("batch_no"));
    assertNotNull(rs.get(0).get("consumed_at"));
  }

  // 8. 同一批次的并发审批只能有一单成功：
  //    仅保留批次 1（100 瓶），两单各申请 100，行锁串行化后第二单必然缺口失败
  @Test
  void concurrentApproveOnSameBatch_onlyOneSucceeds() throws Exception {
    // 把对同一物资的其他批次全部清零，制造单一批次竞争
    jdbc.update("UPDATE inventory_batch SET quantity = 0 WHERE id IN (2,3,4)");

    Long orderA = createdId(waterOrder(100));
    Long orderB = createdId(waterOrder(100));

    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      List<Future<String>> futures = new ArrayList<>();
      for (Long orderId : List.of(orderA, orderB)) {
        futures.add(pool.submit(() -> {
          ready.countDown();
          start.await();
          try {
            dispatchService.approve(orderId, new ApprovePayload("concurrent"));
            return "OK";
          } catch (ApiException ex) {
            return ex.getCode();
          }
        }));
      }
      assertTrue(ready.await(5, TimeUnit.SECONDS));
      start.countDown();

      List<String> results = new ArrayList<>();
      for (Future<String> f : futures) {
        results.add(f.get(20, TimeUnit.SECONDS));
      }
      long okCount = results.stream().filter("OK"::equals).count();
      assertEquals(1, okCount, "并发审批同一批次时必须恰好一单成功，实际：" + results);
      assertTrue(results.contains(ErrorCodes.RESERVATION_SHORTAGE)
          || results.contains(ErrorCodes.CONCURRENT_APPROVAL),
          "失败方应为库存缺口或并发冲突，实际：" + results);
    } finally {
      pool.shutdownNow();
    }

    // 只有一单成功预占：批次1预占100，无超卖；失败单仍是 SUBMITTED
    assertEquals(100, batchReserved(1));
    List<String> statuses = List.of(
        (String) dispatchService.detail(orderA).get("status"),
        (String) dispatchService.detail(orderB).get("status"));
    assertTrue(statuses.contains("APPROVED") && statuses.contains("SUBMITTED"),
        "一单 APPROVED 一单仍 SUBMITTED，实际：" + statuses);
  }

  // 9. 同一调拨单重复审批（并发）也只能成功一次
  @Test
  void concurrentApproveSameOrder_onlyOneSucceeds() throws Exception {
    Long id = createdId(waterOrder(10));
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    ExecutorService pool = Executors.newFixedThreadPool(2);
    try {
      List<Future<String>> futures = new ArrayList<>();
      for (int i = 0; i < 2; i++) {
        futures.add(pool.submit(() -> {
          ready.countDown();
          start.await();
          try {
            dispatchService.approve(id, new ApprovePayload("dup"));
            return "OK";
          } catch (ApiException ex) {
            return ex.getCode();
          }
        }));
      }
      assertTrue(ready.await(5, TimeUnit.SECONDS));
      start.countDown();
      List<String> results = new ArrayList<>();
      for (Future<String> f : futures) {
        results.add(f.get(20, TimeUnit.SECONDS));
      }
      assertEquals(1, results.stream().filter("OK"::equals).count(), "重复审批只允许一次：" + results);
    } finally {
      pool.shutdownNow();
    }
    assertEquals(10, batchReserved(1), "不得重复预占");
  }

  // 10. 未审批通过的单据不能出库确认
  @Test
  void outbound_beforeApproval_isRejected() {
    Long id = createdId(waterOrder(10));
    ApiException ex = assertThrows(ApiException.class,
        () -> dispatchService.confirmOutbound(id, new OutboundPayload("keeper")));
    assertEquals(ErrorCodes.RESERVATION_NOT_READY, ex.getCode());
  }

  private Long createdId(DispatchOrderPayload payload) {
    Object raw = dispatchService.create(payload).get("id");
    return ((Number) raw).longValue();
  }
}
