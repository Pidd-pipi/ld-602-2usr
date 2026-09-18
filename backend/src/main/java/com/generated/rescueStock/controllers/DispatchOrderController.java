package com.generated.rescueStock.controllers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.generated.rescueStock.constants.ErrorCodes;
import com.generated.rescueStock.constants.ErrorMessages;
import com.generated.rescueStock.exceptions.ApiException;
import com.generated.rescueStock.services.DispatchOrderService;
import com.generated.rescueStock.types.ApprovePayload;
import com.generated.rescueStock.types.CancelPayload;
import com.generated.rescueStock.types.DispatchOrderPayload;
import com.generated.rescueStock.types.OutboundPayload;
import com.generated.rescueStock.types.RejectPayload;

/**
 * 调拨审批 / 出库 / 驳回 / 取消 / 签收。
 * controller 层单独包装 IllegalArgumentException 等入参异常；
 * service 抛出的 ApiException 透传给全局 ErrorHandlerMiddleware。
 */
@RestController
@RequestMapping("/api/dispatch-order")
public class DispatchOrderController {

  private static final Logger log = LoggerFactory.getLogger(DispatchOrderController.class);

  private final DispatchOrderService service;

  public DispatchOrderController(DispatchOrderService service) {
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

  @PostMapping
  public ResponseEntity<Map<String, Object>> create(@RequestBody DispatchOrderPayload payload) {
    return ResponseEntity.status(HttpStatus.CREATED).body(wrap(() -> service.create(payload)));
  }

  /** 审批通过：FEFO 预占；库存不足返回 409 与缺口明细 */
  @PostMapping("/{id}/approve")
  public Map<String, Object> approve(@PathVariable Long id, @RequestBody(required = false) ApprovePayload payload) {
    return wrap(() -> service.approve(id, payload));
  }

  /** 出库确认：预占转实扣 */
  @PostMapping("/{id}/outbound")
  public Map<String, Object> outbound(@PathVariable Long id, @RequestBody(required = false) OutboundPayload payload) {
    return wrap(() -> service.confirmOutbound(id, payload));
  }

  /** 驳回：释放全部预占 */
  @PostMapping("/{id}/reject")
  public Map<String, Object> reject(@PathVariable Long id, @RequestBody(required = false) RejectPayload payload) {
    return wrap(() -> service.reject(id, payload));
  }

  /** 取消：释放全部预占 */
  @PostMapping("/{id}/cancel")
  public Map<String, Object> cancel(@PathVariable Long id, @RequestBody(required = false) CancelPayload payload) {
    return wrap(() -> service.cancel(id, payload));
  }

  /** 签收 */
  @PostMapping("/{id}/receive")
  public Map<String, Object> receive(@PathVariable Long id, @RequestBody(required = false) OutboundPayload payload) {
    return wrap(() -> service.receive(id, payload));
  }

  /** controller 自己的异常包装层：非法入参转 VALIDATION_FAILED，业务异常原样抛出 */
  private Map<String, Object> wrap(ServiceCall call) {
    try {
      return call.invoke();
    } catch (ApiException ex) {
      throw ex;
    } catch (IllegalArgumentException ex) {
      log.warn("dispatch controller validation failed: {}", ex.getMessage());
      throw new ApiException(ErrorCodes.VALIDATION_FAILED,
          java.text.MessageFormat.format(ErrorMessages.VALIDATION_FAILED, ex.getMessage()),
          HttpStatus.BAD_REQUEST.value());
    }
  }

  @FunctionalInterface
  private interface ServiceCall {
    Map<String, Object> invoke();
  }
}
