package com.bitreiver.app_server.domain.assetAnalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "자산 가치 추이 응답")
public class AssetValueTrendResponse {
    
    @Schema(description = "시계열 데이터")
    private List<AssetValuePoint> dataPoints;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "자산 가치 시점 데이터")
    public static class AssetValuePoint {
        @Schema(description = "날짜", example = "2024-01-01T00:00:00")
        private LocalDateTime date;
        
        @Schema(description = "총 자산 가치", example = "1000000.0")
        private BigDecimal totalValue;
        
        @Schema(description = "투자 원금", example = "800000.0")
        private BigDecimal principal;
        
        @Schema(description = "수익 금액", example = "200000.0")
        private BigDecimal profit;
        
        @Schema(description = "수익률 (%)", example = "25.0")
        private BigDecimal profitRate;
    }
}
