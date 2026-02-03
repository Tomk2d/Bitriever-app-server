package com.bitreiver.app_server.domain.assetAnalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "자산 분석 종합 응답")
public class AssetAnalysisResponse {
    
    @Schema(description = "주요 지표")
    private SummaryMetrics summaryMetrics;
    
    @Schema(description = "자산 가치 추이")
    private AssetValueTrendResponse assetValueTrend;
    
    @Schema(description = "수익률 분포")
    private ProfitDistributionResponse profitDistribution;
    
    @Schema(description = "코인별 보유 현황")
    private CoinHoldingResponse coinHoldings;
    
    @Schema(description = "거래 빈도 분석")
    private TradingFrequencyResponse tradingFrequency;
    
    @Schema(description = "거래 스타일 분석")
    private TradingStyleResponse tradingStyle;
    
    @Schema(description = "월별 투자 현황")
    private MonthlyInvestmentResponse monthlyInvestment;
    
    @Schema(description = "최고/최저 수익 종목")
    private TopCoinResponse topCoins;
    
    @Schema(description = "심리 분석")
    private PsychologyAnalysisResponse psychologyAnalysis;
    
    @Schema(description = "리스크 분석")
    private RiskAnalysisResponse riskAnalysis;
    
    @Schema(description = "종합 성향 요약")
    private TradingTendencySummaryResponse tradingTendencySummary;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "주요 지표")
    public static class SummaryMetrics {
        @Schema(description = "총 자산 가치", example = "1000000.0")
        private BigDecimal totalAssetValue;
        
        @Schema(description = "총 투자 원금", example = "800000.0")
        private BigDecimal totalPrincipal;
        
        @Schema(description = "총 수익 금액", example = "200000.0")
        private BigDecimal totalProfit;
        
        @Schema(description = "승률 (%)", example = "60.0")
        private BigDecimal winRate;
        
        @Schema(description = "총 거래 횟수", example = "500")
        private Long totalTradeCount;
        
        @Schema(description = "보유 코인 수", example = "10")
        private Integer holdingCoinCount;
    }
}
