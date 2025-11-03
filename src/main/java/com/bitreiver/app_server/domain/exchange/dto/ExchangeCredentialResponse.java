package com.bitreiver.app_server.domain.exchange.dto;

import com.bitreiver.app_server.domain.exchange.entity.ExchangeCredential;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "거래소 자격증명 응답")
public class ExchangeCredentialResponse {
    @Schema(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
    
    @Schema(description = "거래소 타입 코드 (1:UPBIT, 2:BITHUMB, 3:COINONE, 11:BINANCE, 12:BYBIT, 13:COINBASE, 14:OKX)", example = "1")
    private Short exchangeProvider;
    
    @Schema(description = "생성 일시", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "마지막 수정 일시", example = "2024-01-01T00:00:00")
    private LocalDateTime lastUpdatedAt;
    
    public static ExchangeCredentialResponse from(ExchangeCredential credential) {
        return ExchangeCredentialResponse.builder()
            .userId(credential.getUserId())
            .exchangeProvider(credential.getExchangeProvider())
            .createdAt(credential.getCreatedAt())
            .lastUpdatedAt(credential.getLastUpdatedAt())
            .build();
    }
}

