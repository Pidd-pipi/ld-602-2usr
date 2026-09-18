package com.generated.rescueStock.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generated.rescueStock.services.DisasterEventService;

/** 灾害事件：事件详情同步展示关联调拨单的预占/缺口 */
@RestController
@RequestMapping("/api/event")
public class DisasterEventController {

  private final DisasterEventService service;

  public DisasterEventController(DisasterEventService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }

  @GetMapping("/{id}")
  public Map<String, Object> detail(@PathVariable Long id) {
    return service.detail(id);
  }
}
