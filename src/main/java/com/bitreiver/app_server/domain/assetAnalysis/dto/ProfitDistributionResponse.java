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
@Schema(description = "수익률 분포 응답")
public class ProfitDistributionResponse {
    
    @Schema(description = "전체 매도 거래 수", example = "100")
    private Long totalSellCount;
    
    @Schema(description = "수익 거래 수", example = "60")
    private Long profitCount;
    
    @Schema(description = "손실 거래 수", example = "40")
    private Long lossCount;
    
    @Schema(description = "승률 (%)", example = "60.0")
    private BigDecimal winRate;
    
    @Schema(description = "평균 수익률 (%)", example = "15.5")
    private BigDecimal averageProfitRate;
    
    @Schema(description = "평균 손실률 (%)", example = "-8.3")
    private BigDecimal averageLossRate;
    
    @Schema(description = "중앙값 수익률 (%)", example = "12.0")
    private BigDecimal medianProfitRate;
    
    @Schema(description = "최고 수익률 (%)", example = "150.0")
    private BigDecimal maxProfitRate;
    
    @Schema(description = "최저 수익률 (%)", example = "-50.0")
    private BigDecimal minProfitRate;
    
    @Schema(description = "수익률 분포 구간별 통계")
    private List<ProfitRange> profitRanges;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "수익률 구간별 통계")
    public static class ProfitRange {
        @Schema(description = "구간 시작 (%)", example = "-50.0")
        private BigDecimal rangeStart;
        
        @Schema(description = "구간 종료 (%)", example = "-30.0")
        private BigDecimal rangeEnd;
        
        @Schema(description = "거래 수", example = "5")
        private Long count;
    }
}
