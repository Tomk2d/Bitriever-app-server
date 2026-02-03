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
@Schema(description = "리스크 분석 응답")
public class RiskAnalysisResponse {
    
    @Schema(description = "상위 5개 코인 자산 집중도 (%)", example = "75.5")
    private BigDecimal top5CoinConcentration;
    
    @Schema(description = "상위 코인별 비율")
    private List<CoinConcentration> topCoinConcentrations;
    
    @Schema(description = "포트폴리오 다양성 지수 (0-1, 1이 가장 다양)", example = "0.65")
    private BigDecimal diversityIndex;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "코인 집중도")
    public static class CoinConcentration {
        @Schema(description = "코인 심볼", example = "BTC")
        private String symbol;
        
        @Schema(description = "비율 (%)", example = "30.0")
        private BigDecimal percentage;
    }
    
}
