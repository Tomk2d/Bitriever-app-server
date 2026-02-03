package com.bitreiver.app_server.domain.assetAnalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "월별 투자 현황 응답")
public class MonthlyInvestmentResponse {
    
    @Schema(description = "월별 투자 현황 목록")
    private List<MonthlyInvestment> monthlyInvestments;
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "월별 투자 정보")
    public static class MonthlyInvestment {
        @Schema(description = "년", example = "2024")
        private Integer year;
        
        @Schema(description = "월", example = "1")
        private Integer month;
        
        @Schema(description = "총 매수액", example = "1000000.0")
        private BigDecimal totalBuyAmount;
        
        @Schema(description = "총 매도액", example = "1200000.0")
        private BigDecimal totalSellAmount;
        
        @Schema(description = "순 투자액 (매수액 - 매도액)", example = "-200000.0")
        private BigDecimal netInvestment;
        
        @Schema(description = "월별 수익률 (%)", example = "20.0")
        private BigDecimal monthlyProfitRate;
        
        @Schema(description = "거래 횟수", example = "50")
        private Long tradeCount;
    }
}
