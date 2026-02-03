package com.bitreiver.app_server.domain.assetAnalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "종합 성향 요약 응답")
public class TradingTendencySummaryResponse {
    
    @Schema(description = "거래 스타일 (SHORT_TERM, MEDIUM_TERM, LONG_TERM)", example = "MEDIUM_TERM")
    private String tradingStyle;
    
    @Schema(description = "리스크 성향 (CONSERVATIVE, MODERATE, AGGRESSIVE)", example = "MODERATE")
    private String riskTendency;
    
    @Schema(description = "선호 거래 시간대", example = "14")
    private Integer preferredTradingHour;
    
    @Schema(description = "선호 거래 요일", example = "1")
    private Integer preferredTradingDay;
    
    @Schema(description = "선호 코인 카테고리 (상위 3개)")
    private List<String> preferredCoinCategories;
    
    @Schema(description = "주요 거래소", example = "UPBIT")
    private String primaryExchange;
}
