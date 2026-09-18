package com.generated.rescueStock.models;

import java.time.LocalDateTime;

/** 灾害事件（事件响应页聚合调拨进度） */
public class DisasterEvent {
  public Long id;
  public String name;
  public String eventType;
  public String district;
  public String level;
  public LocalDateTime occurredAt;
  public String status;
  public String description;
}
