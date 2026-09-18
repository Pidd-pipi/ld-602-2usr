package com.generated.rescueStock.types;

import java.util.List;
import java.util.Map;
import com.generated.rescueStock.constants.ErrorCodes;
import com.generated.rescueStock.constants.ErrorMessages;

// 业务异常：service 层抛出，controller 层包装为 HTTP 响应。
public class ServiceException extends RuntimeException {
  private final String code;
  private final transient Map<String, Object> details;

  public ServiceException(String code, String message, Map<String, Object> details) {
    super(message);
    this.code = code;
    this.details = details;
  }

  public String getCode() {
    return code;
  }

  public Map<String, Object> getDetails() {
    return details;
  }

  public static ServiceException validation(String message) {
    return new ServiceException(ErrorCodes.VALIDATION_FAILED, message, null);
  }

  public static ServiceException orderNotFound(long id) {
    return new ServiceException(ErrorCodes.ORDER_NOT_FOUND, ErrorMessages.ORDER_NOT_FOUND + ": " + id, null);
  }

  public static ServiceException eventNotFound(long id) {
    return new ServiceException(ErrorCodes.EVENT_NOT_FOUND, ErrorMessages.EVENT_NOT_FOUND + ": " + id, null);
  }

  public static ServiceException invalidStatus(String status, String action) {
    return new ServiceException(ErrorCodes.INVALID_STATUS_TRANSITION,
        ErrorMessages.INVALID_STATUS_TRANSITION + "（当前状态 " + status + "，操作 " + action + "）", null);
  }

  public static ServiceException insufficientStock(List<Map<String, Object>> shortages) {
    return new ServiceException(ErrorCodes.INSUFFICIENT_STOCK, ErrorMessages.INSUFFICIENT_STOCK,
        Map.of("shortages", shortages));
  }
}
