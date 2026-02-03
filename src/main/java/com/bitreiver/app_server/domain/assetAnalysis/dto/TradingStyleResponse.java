package com.bitreiver.app_server.domain.assetAnalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "거래 스타일 분석 응답")
public class TradingStyleResponse {
    
    @Schema(description = "평균 거래 금액", example = "50000.0")
    private BigDecimal averageTradeAmount;
    
    @Schema(description = "중앙값 거래 금액", example = "30000.0")
    private BigDecimal medianTradeAmount;
    
    @Schema(description = "최대 거래 금액", example = "500000.0")
    private BigDecimal maxTradeAmount;
    
    @Schema(description = "최소 거래 금액", example = "1000.0")
    private BigDecimal minTradeAmount;
    
    @Schema(description = "평균 보유 기간 (일)", example = "15.5")
    private BigDecimal averageHoldingPeriod;
    
    @Schema(description = "중앙값 보유 기간 (일)", example = "7.0")
    private BigDecimal medianHoldingPeriod;
    
    @Schema(description = "거래 스타일 분류 (SHORT_TERM, MEDIUM_TERM, LONG_TERM)", example = "MEDIUM_TERM")
    private String tradingStyle;
    
    @Schema(description = "보유 기간별 통계")
    private List<HoldingPeriodRange> holdingPeriodRanges;
    
    @Schema(description = "거래 금액 구간별 통계")
    private List<TradeAmountRange> tradeAmountRanges;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "보유 기간 구간별 통계")
    public static class HoldingPeriodRange {
        @Schema(description = "구간 이름", example = "1일 이하")
        private String rangeName;
        
        @Schema(description = "거래 수", example = "20")
        private Long count;
        
        @Schema(description = "비율 (%)", example = "20.0")
        private BigDecimal percentage;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "거래 금액 구간별 통계")
    public static class TradeAmountRange {
        @Schema(description = "구간 이름", example = "10만원 미만")
        private String rangeName;
        
        @Schema(description = "거래 수", example = "30")
        private Long count;
        
        @Schema(description = "비율 (%)", example = "30.0")
        private BigDecimal percentage;
    }
}
