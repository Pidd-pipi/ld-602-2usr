package com.generated.rescueStock.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.generated.rescueStock.models.InventoryBatch;

/**
 * 批次分配引擎：最早到期先出（FEFO）。
 * 调用方必须已在事务内通过 SELECT ... FOR UPDATE 锁定批次行，
 * 本类只根据锁定后的快照计算分配计划，不做任何写库操作。
 *
 * 排序（由 SQL ORDER BY expire_at ASC, batch_no ASC 保证）：
 *   1) 保质期/到期日越早越优先；
 *   2) 到期日相同，批次号字典序升序。
 */
@Service
public class BatchAllocationService {

  /** 单条行的分配结果：逐批次占用量 + 缺口（不足时 need > 0） */
  public AllocationPlan plan(List<InventoryBatch> lockedBatches, int need) {
    AllocationPlan plan = new AllocationPlan();
    int remaining = need;
    for (InventoryBatch batch : lockedBatches) {
      int available = batch.quantity - batch.reservedQuantity;
      if (available <= 0 || remaining <= 0) {
        continue;
      }
      int take = Math.min(available, remaining);
      plan.allocations.add(new BatchAllocation(batch, take));
      remaining -= take;
    }
    plan.shortage = remaining;
    return plan;
  }

  /** 一笔批次占用 */
  public record BatchAllocation(InventoryBatch batch, int quantity) {}

  /** 分配计划：allocation 总和 = need - shortage；shortage > 0 表示整单必须失败 */
  public static class AllocationPlan {
    private final List<BatchAllocation> allocations = new ArrayList<>();
    private int shortage;

    public List<BatchAllocation> getAllocations() {
      return allocations;
    }

    public int getShortage() {
      return shortage;
    }

    public boolean isFulfilled() {
      return shortage == 0;
    }

    public int getAllocatedTotal() {
      return allocations.stream().mapToInt(BatchAllocation::quantity).sum();
    }
  }
}
