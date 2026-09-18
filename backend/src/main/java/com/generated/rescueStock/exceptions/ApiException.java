package com.generated.rescueStock.exceptions;

import java.util.List;
import java.util.Map;

/** 业务异常：携带错误码与可选的缺口明细，service 层抛出，controller/全局处理器分别包装 */
public class ApiException extends RuntimeException {
  private final String code;
  private final int httpStatus;
  private List<Map<String, Object>> details;

  public ApiException(String code, String message, int httpStatus) {
    super(message);
    this.code = code;
    this.httpStatus = httpStatus;
  }

  public ApiException(String code, String message, int httpStatus, List<Map<String, Object>> details) {
    this(code, message, httpStatus);
    this.details = details;
  }

  public String getCode() {
    return code;
  }

  public int getHttpStatus() {
    return httpStatus;
  }

  public List<Map<String, Object>> getDetails() {
    return details;
  }
}
