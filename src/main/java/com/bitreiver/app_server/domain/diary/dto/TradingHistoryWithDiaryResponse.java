package com.bitreiver.app_server.domain.diary.dto;

import com.bitreiver.app_server.domain.diary.entity.Diary;
import com.bitreiver.app_server.domain.trading.entity.TradingHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "거래 내역과 매매 일지를 함께 반환하는 응답")
public class TradingHistoryWithDiaryResponse {
    
    @Schema(description = "매매 내역 ID", example = "1")
    private Integer tradingHistoryId;
    
    @Schema(description = "사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
    
    @Schema(description = "코인 ID", example = "1")
    private Integer coinId;
    
    @Schema(description = "거래소 코드 (1:UPBIT, 2:BITHUMB, 3:COINONE, 11:BINANCE, 12:BYBIT, 13:COINBASE, 14:OKX)", example = "1")
    private Short exchangeCode;
    
    @Schema(description = "거래 고유 UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String tradeUuid;
    
    @Schema(description = "거래 타입 (0:매수, 1:매도)", example = "0")
    private Short tradeType;
    
    @Schema(description = "거래 가격", example = "50000.0")
    private BigDecimal price;
    
    @Schema(description = "거래 수량", example = "0.001")
    private BigDecimal quantity;
    
    @Schema(description = "총 거래 금액", example = "50.0")
    private BigDecimal totalPrice;
    
    @Schema(description = "수수료", example = "0.05")
    private BigDecimal fee;
    
    @Schema(description = "거래 일시", example = "2024-01-01T00:00:00")
    private LocalDateTime tradeTime;
    
    @Schema(description = "생성 일시", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "매매 일지 정보 (일지가 없는 경우 null)", nullable = true)
    private DiaryInfo diary;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "매매 일지 정보")
    public static class DiaryInfo {
        @Schema(description = "일지 ID", example = "1")
        private Integer id;
        
        @Schema(description = "일지 내용 (JSONB 형식)", example = "{\"blocks\":[{\"type\":\"text\",\"content\":\"오늘 비트코인을 매수했습니다...\"}]}")
        private String content;
        
        @Schema(description = "태그 배열", example = "[\"스캘핑\", \"분석필요\"]")
        private List<String> tags;
    }
    
    public static TradingHistoryWithDiaryResponse from(TradingHistory tradingHistory, Diary diary) {
        TradingHistoryWithDiaryResponseBuilder builder = TradingHistoryWithDiaryResponse.builder()
            .tradingHistoryId(tradingHistory.getId())
            .userId(tradingHistory.getUserId())
            .coinId(tradingHistory.getCoinId())
            .exchangeCode(tradingHistory.getExchangeCode())
            .tradeUuid(tradingHistory.getTradeUuid())
            .tradeType(tradingHistory.getTradeType())
            .price(tradingHistory.getPrice())
            .quantity(tradingHistory.getQuantity())
            .totalPrice(tradingHistory.getTotalPrice())
            .fee(tradingHistory.getFee())
            .tradeTime(tradingHistory.getTradeTime())
            .createdAt(tradingHistory.getCreatedAt());
        
        if (diary != null) {
            builder.diary(DiaryInfo.builder()
                .id(diary.getId())
                .content(diary.getContent())
                .tags(diary.getTags())
                .build());
        } else {
            builder.diary(null);
        }
        
        return builder.build();
    }
}

