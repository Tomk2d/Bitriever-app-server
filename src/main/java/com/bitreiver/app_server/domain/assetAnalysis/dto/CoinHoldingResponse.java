package com.bitreiver.app_server.domain.assetAnalysis.dto;

import com.bitreiver.app_server.domain.coin.dto.CoinResponse;
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
@Schema(description = "코인별 보유 현황 응답")
public class CoinHoldingResponse {
    
    @Schema(description = "코인별 보유 현황 목록")
    private List<CoinHolding> holdings;
    
    @Schema(description = "총 자산 가치", example = "1000000.0")
    private BigDecimal totalValue;
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "코인 보유 정보")
    public static class CoinHolding {
        @Schema(description = "코인 정보")
        private CoinResponse coin;
        
        @Schema(description = "보유 수량", example = "1.5")
        private BigDecimal quantity;
        
        @Schema(description = "평균 매수가", example = "50000.0")
        private BigDecimal avgBuyPrice;
        
        @Schema(description = "현재 가격", example = "55000.0")
        private BigDecimal currentPrice;
        
        @Schema(description = "보유 금액", example = "82500.0")
        private BigDecimal holdingValue;
        
        @Schema(description = "비율 (%)", example = "8.25")
        private BigDecimal percentage;
        
        @Schema(description = "수익 금액", example = "7500.0")
        private BigDecimal profit;
        
        @Schema(description = "수익률 (%)", example = "10.0")
        private BigDecimal profitRate;
        
        @Schema(description = "거래소 코드", example = "1")
        private Short exchangeCode;
    }
}
