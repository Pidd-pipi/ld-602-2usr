package com.generated.rescueStock.middlewares;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.generated.rescueStock.constants.ErrorCodes;
import com.generated.rescueStock.constants.ErrorMessages;
import com.generated.rescueStock.types.ServiceException;

// 全局兜底异常处理：controller 已分别包装业务异常，这里兜底未预期错误。
@RestControllerAdvice
public class ErrorHandlerMiddleware {

  @ExceptionHandler(ServiceException.class)
  public ResponseEntity<Map<String, Object>> serviceError(ServiceException ex) {
    return ResponseEntity.status(statusFor(ex.getCode())).body(body(ex.getCode(), ex.getMessage(), ex.getDetails()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> unknownError(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(body(ErrorCodes.INTERNAL_ERROR, ErrorMessages.INTERNAL_ERROR + ": " + ex.getMessage(), null));
  }

  public static HttpStatus statusFor(String code) {
    return switch (code) {
      case ErrorCodes.ORDER_NOT_FOUND, ErrorCodes.EVENT_NOT_FOUND -> HttpStatus.NOT_FOUND;
      case ErrorCodes.INSUFFICIENT_STOCK, ErrorCodes.INVALID_STATUS_TRANSITION -> HttpStatus.CONFLICT;
      case ErrorCodes.VALIDATION_FAILED -> HttpStatus.BAD_REQUEST;
      default -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }

  public static Map<String, Object> body(String code, String message, Map<String, Object> details) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("code", code);
    body.put("message", message);
    body.put("details", details);
    return body;
  }
}
