package com.generated.rescueStock.middlewares;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.generated.rescueStock.constants.ErrorCodes;
import com.generated.rescueStock.constants.ErrorMessages;
import com.generated.rescueStock.exceptions.ApiException;

/**
 * 全局错误处理（errorHandlerMiddleware）。
 * 仅负责最终响应包装；controller 层仍需单独包装，禁止业务代码只在这里吞掉异常。
 */
@RestControllerAdvice
public class ErrorHandlerMiddleware {

  private static final Logger log = LoggerFactory.getLogger(ErrorHandlerMiddleware.class);

  /** 缺口/并发等业务异常：把 service 抛出的错误码与明细原样返回 */
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
    Map<String, Object> body = new java.util.HashMap<>();
    body.put("code", ex.getCode());
    body.put("message", ex.getMessage());
    if (ex.getDetails() != null) {
      body.put("shortages", ex.getDetails());
    }
    return ResponseEntity.status(ex.getHttpStatus()).body(body);
  }

  /** 兜底异常：controller 未包装到的系统错误在此收敛 */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
    log.error("unhandled exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of(
            "code", ErrorCodes.INTERNAL_ERROR,
            "message", ErrorMessages.INTERNAL_ERROR));
  }
}
