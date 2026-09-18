package com.generated.rescueStock.models;

// 调拨单状态流转记录，用于审批时间线展示。
public class DispatchStatusEvent {
  public String status;
  public String actor;
  public String at;
  public String note;

  public DispatchStatusEvent() {}

  public DispatchStatusEvent(String status, String actor, String at, String note) {
    this.status = status;
    this.actor = actor;
    this.at = at;
    this.note = note;
  }
}
