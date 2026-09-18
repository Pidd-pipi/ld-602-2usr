package com.generated.rescueStock.types;

import com.fasterxml.jackson.annotation.JsonProperty;

// 调拨单流转动作请求体：operator 为操作人，缺省时由服务端兜底。
public record DispatchActionPayload(@JsonProperty("operator") String operator) {}
