package com.generated.rescueStock.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.generated.rescueStock.constructors.DispatchEventDtoFactory;
import com.generated.rescueStock.models.DispatchEvent;
import com.generated.rescueStock.repositories.DispatchEventRepository;
import com.generated.rescueStock.types.ServiceException;

// 事件响应：事件详情聚合其下调拨单，与调拨页共用同一份预占/可用/缺口数据。
@Service
public class DispatchEventService {
  private final DispatchEventRepository eventRepo;
  private final DispatchOrderService dispatchOrderService;

  public DispatchEventService(DispatchEventRepository eventRepo, DispatchOrderService dispatchOrderService) {
    this.eventRepo = eventRepo;
    this.dispatchOrderService = dispatchOrderService;
  }

  public List<Map<String, Object>> list() {
    List<Map<String, Object>> rows = new ArrayList<>();
    for (DispatchEvent event : eventRepo.findAll()) {
      rows.add(DispatchEventDtoFactory.toDto(event));
    }
    return rows;
  }

  public Map<String, Object> detail(long id) {
    DispatchEvent event = eventRepo.findById(id);
    if (event == null) {
      throw ServiceException.eventNotFound(id);
    }
    return DispatchEventDtoFactory.detailDto(event, dispatchOrderService.listByEvent(id));
  }
}
