package com.generated.rescueStock.routes;

/** 调拨单路由常量，controller 的 @RequestMapping 需与此保持一致 */
public final class DispatchOrderRoutes {
  public static final String PATH = "/api/dispatch-order";
  public static final String APPROVE = PATH + "/{id}/approve";
  public static final String OUTBOUND = PATH + "/{id}/outbound";
  public static final String REJECT = PATH + "/{id}/reject";
  public static final String CANCEL = PATH + "/{id}/cancel";
  public static final String RECEIVE = PATH + "/{id}/receive";

  private DispatchOrderRoutes() {}
}
