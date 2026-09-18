package com.generated.rescueStock.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.rescueStock.middlewares.ErrorHandlerMiddleware;
import com.generated.rescueStock.routes.DispatchOrderRoutes;
import com.generated.rescueStock.services.DispatchOrderService;
import com.generated.rescueStock.types.DispatchActionPayload;
import com.generated.rescueStock.types.DispatchOrderPayload;
import com.generated.rescueStock.types.ServiceException;

// 调拨单接口：业务异常在 controller 层包装为 HTTP 响应，全局兜底见 ErrorHandlerMiddleware。
@RestController
@RequestMapping(DispatchOrderRoutes.PATH)
public class DispatchOrderController {
  private final DispatchOrderService service;

  public DispatchOrderController(DispatchOrderService service) {
    this.service = service;
  }

  @GetMapping
  public List<Map<String, Object>> list() {
    return service.list();
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(@PathVariable long id) {
    try {
      return ResponseEntity.ok(service.detail(id));
    } catch (ServiceException ex) {
      return wrap(ex);
    }
  }

  @PostMapping
  public ResponseEntity<?> create(@RequestBody DispatchOrderPayload payload) {
    try {
      return ResponseEntity.ok(service.create(payload));
    } catch (ServiceException ex) {
      return wrap(ex);
    }
  }

  @PostMapping("/{id}/submit")
  public ResponseEntity<?> submit(@PathVariable long id, @RequestBody(required = false) DispatchActionPayload body) {
    return act(() -> service.submit(id, operator(body)));
  }

  @PostMapping("/{id}/approve")
  public ResponseEntity<?> approve(@PathVariable long id, @RequestBody(required = false) DispatchActionPayload body) {
    return act(() -> service.approve(id, operator(body)));
  }

  @PostMapping("/{id}/reject")
  public ResponseEntity<?> reject(@PathVariable long id, @RequestBody(required = false) DispatchActionPayload body) {
    return act(() -> service.reject(id, operator(body)));
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<?> cancel(@PathVariable long id, @RequestBody(required = false) DispatchActionPayload body) {
    return act(() -> service.cancel(id, operator(body)));
  }

  @PostMapping("/{id}/dispatch")
  public ResponseEntity<?> dispatch(@PathVariable long id, @RequestBody(required = false) DispatchActionPayload body) {
    return act(() -> service.confirmOutbound(id, operator(body)));
  }

  @PostMapping("/{id}/receive")
  public ResponseEntity<?> receive(@PathVariable long id, @RequestBody(required = false) DispatchActionPayload body) {
    return act(() -> service.receive(id, operator(body)));
  }

  private ResponseEntity<?> act(java.util.function.Supplier<Map<String, Object>> action) {
    try {
      return ResponseEntity.ok(action.get());
    } catch (ServiceException ex) {
      return wrap(ex);
    }
  }

  private ResponseEntity<?> wrap(ServiceException ex) {
    return ResponseEntity.status(ErrorHandlerMiddleware.statusFor(ex.getCode()))
        .body(ErrorHandlerMiddleware.body(ex.getCode(), ex.getMessage(), ex.getDetails()));
  }

  private String operator(DispatchActionPayload body) {
    return body == null || body.operator() == null || body.operator().isBlank() ? "system" : body.operator();
  }
}
