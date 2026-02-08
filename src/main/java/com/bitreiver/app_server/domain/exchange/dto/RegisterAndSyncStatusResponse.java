package com.bitreiver.app_server.domain.exchange.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterAndSyncStatusResponse {
    private String status;
    @JsonProperty("user_id")
    private String userId;
    @JsonProperty("exchange_provider")
    private Short exchangeProvider;
    @JsonProperty("exchange_name")
    private String exchangeName;
    private Map<String, Object> result;
    private String error;
    @JsonProperty("error_code")
    private String errorCode;
    private String message;
}
