package com.generated.rescueStock.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.generated.rescueStock.middlewares.ErrorHandlerMiddleware;
import com.generated.rescueStock.routes.DispatchEventRoutes;
import com.generated.rescueStock.services.DispatchEventService;
import com.generated.rescueStock.types.ServiceException;

@RestController
@RequestMapping(DispatchEventRoutes.PATH)
public class DispatchEventController {
  private final DispatchEventService service;

  public DispatchEventController(DispatchEventService service) {
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
      return ResponseEntity.status(ErrorHandlerMiddleware.statusFor(ex.getCode()))
          .body(ErrorHandlerMiddleware.body(ex.getCode(), ex.getMessage(), ex.getDetails()));
    }
  }
}
