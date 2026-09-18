package com.generated.rescueStock.middlewares;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;
import com.generated.rescueStock.utils.Formatters;

// 操作日志记录：调拨预占闭环的每个写动作都会落一条审计记录。
@Component
public class AuditLogMiddleware {
  private final List<Map<String, Object>> entries = new CopyOnWriteArrayList<>();
  private final AtomicLong seq = new AtomicLong(0);

  public void record(String actor, String action, String targetType, long targetId, String detail) {
    Map<String, Object> entry = new LinkedHashMap<>();
    entry.put("id", seq.incrementAndGet());
    entry.put("actor", actor == null || actor.isBlank() ? "system" : actor);
    entry.put("action", action);
    entry.put("target_type", targetType);
    entry.put("target_id", targetId);
    entry.put("target_label", Formatters.audit(targetType, targetId));
    entry.put("detail", detail);
    entry.put("created_at", Instant.now().toString());
    entries.add(entry);
  }

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> rows = new ArrayList<>(entries);
    rows.sort((a, b) -> Long.compare((long) b.get("id"), (long) a.get("id")));
    return rows;
  }
}
