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
@Schema(description = "거래소별 사용 패턴 응답")
public class ExchangeUsageResponse {
    
    @Schema(description = "거래소별 통계 목록")
    private List<ExchangeStat> exchangeStats;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "거래소별 통계")
    public static class ExchangeStat {
        @Schema(description = "거래소 코드 (1:UPBIT, 2:BITHUMB, 3:COINONE, 11:BINANCE, 12:BYBIT, 13:COINBASE, 14:OKX)", example = "1")
        private Short exchangeCode;
        
        @Schema(description = "거래소 이름", example = "UPBIT")
        private String exchangeName;
        
        @Schema(description = "거래 횟수", example = "100")
        private Long tradeCount;
        
        @Schema(description = "총 거래 금액", example = "5000000.0")
        private BigDecimal totalTradeAmount;
        
        @Schema(description = "평균 거래 금액", example = "50000.0")
        private BigDecimal averageTradeAmount;
        
        @Schema(description = "매도 거래 수", example = "50")
        private Long sellCount;
        
        @Schema(description = "평균 수익률 (%)", example = "12.5")
        private BigDecimal averageProfitRate;
        
        @Schema(description = "사용 비율 (%)", example = "60.0")
        private BigDecimal usagePercentage;
    }
}
