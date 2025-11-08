package com.bitreiver.app_server.domain.user.dto;

import com.bitreiver.app_server.domain.exchange.enums.ExchangeType;
import com.bitreiver.app_server.domain.user.entity.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String nickname;
    private Short signupType;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private Boolean isConnectExchange;
    private List<ExchangeTypeInfo> connectedExchanges;
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static UserResponse from(User user) {
        List<ExchangeTypeInfo> connectedExchanges = parseConnectedExchanges(user.getConnectedExchanges());
        
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .signupType(user.getSignupType())
            .createdAt(user.getCreatedAt())
            .isActive(user.getIsActive())
            .isConnectExchange(user.getIsConnectExchange())
            .connectedExchanges(connectedExchanges)
            .build();
    }
    
    private static List<ExchangeTypeInfo> parseConnectedExchanges(String connectedExchangesJson) {
        if (connectedExchangesJson == null || connectedExchangesJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            List<String> exchangeNames = objectMapper.readValue(
                connectedExchangesJson, 
                new TypeReference<List<String>>() {}
            );
            
            List<ExchangeTypeInfo> exchangeTypeInfos = new ArrayList<>();
            for (String exchangeName : exchangeNames) {
                try {
                    ExchangeType exchangeType = ExchangeType.fromName(exchangeName);
                    exchangeTypeInfos.add(ExchangeTypeInfo.builder()
                        .code(exchangeType.getCode())
                        .name(exchangeType.getName())
                        .koreanName(exchangeType.getKoreanName())
                        .build());
                } catch (IllegalArgumentException e) {
                    log.warn("알 수 없는 거래소 이름: {}", exchangeName);
                }
            }
            
            return exchangeTypeInfos;
        } catch (Exception e) {
            log.error("connected_exchanges JSON 파싱 실패: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}

