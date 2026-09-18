package com.generated.rescueStock.models;

// 灾害事件：事件详情页聚合其下调拨单的预占/可用/缺口。
public class DispatchEvent {
  public long id;
  public String name;
  public String disasterType;
  public String level;
  public String status;
  public String occurredAt;
  public String description;

  public DispatchEvent() {}

  public DispatchEvent(long id, String name, String disasterType, String level, String status, String occurredAt,
      String description) {
    this.id = id;
    this.name = name;
    this.disasterType = disasterType;
    this.level = level;
    this.status = status;
    this.occurredAt = occurredAt;
    this.description = description;
  }
}
