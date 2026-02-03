package com.bitreiver.app_server.domain.assetAnalysis.dto;

import com.bitreiver.app_server.domain.coin.dto.CoinResponse;
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
@Schema(description = "최고/최저 수익 종목 응답")
public class TopCoinResponse {
    
    @Schema(description = "최고 수익 종목 목록")
    private List<TopCoin> topProfitCoins;
    
    @Schema(description = "최저 수익 종목 목록 (손실)")
    private List<TopCoin> topLossCoins;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "수익 종목 정보")
    public static class TopCoin {
        @Schema(description = "코인 정보")
        private CoinResponse coin;
        
        @Schema(description = "누적 수익 금액", example = "500000.0")
        private BigDecimal totalProfit;
        
        @Schema(description = "평균 수익률 (%)", example = "25.5")
        private BigDecimal averageProfitRate;
        
        @Schema(description = "매도 거래 수", example = "10")
        private Long sellCount;
        
        @Schema(description = "거래소 코드", example = "1")
        private Short exchangeCode;
    }
}
