package com.bitreiver.app_server.domain.asset.dto;

import com.bitreiver.app_server.domain.asset.entity.Asset;
import com.bitreiver.app_server.domain.coin.dto.CoinResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "자산 응답")
public class AssetResponse {
    @Schema(description = "자산 ID", example = "1")
    private Integer id;
    
    @Schema(description = "거래소 코드 (1:UPBIT, 2:BITHUMB, 3:COINONE, 11:BINANCE, 12:BYBIT, 13:COINBASE, 14:OKX)", example = "1")
    private Short exchangeCode;
    
    @Schema(description = "코인 ID", example = "1")
    private Integer coinId;
    
    @Schema(description = "자산 심볼", example = "KRW")
    private String symbol;
    
    @Schema(description = "거래 기준 심볼", example = "KRW")
    private String tradeBySymbol;
    
    @Schema(description = "보유량", example = "254.43007247")
    private BigDecimal quantity;
    
    @Schema(description = "잠금된 양", example = "0")
    private BigDecimal lockedQuantity;
    
    @Schema(description = "평균 매수가", example = "50000.0")
    private BigDecimal avgBuyPrice;
    
    @Schema(description = "평균 매수가 수정 여부", example = "true")
    private Boolean avgBuyPriceModified;
    
    @Schema(description = "생성 일시", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정 일시", example = "2024-01-01T00:00:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "코인 정보")
    private CoinResponse coin;
    
    public static AssetResponse from(Asset asset) {
        return AssetResponse.builder()
            .id(asset.getId())
            .exchangeCode(asset.getExchangeCode())
            .coinId(asset.getCoinId())
            .symbol(asset.getSymbol())
            .tradeBySymbol(asset.getTradeBySymbol())
            .quantity(asset.getQuantity())
            .lockedQuantity(asset.getLockedQuantity())
            .avgBuyPrice(asset.getAvgBuyPrice())
            .avgBuyPriceModified(asset.getAvgBuyPriceModified())
            .createdAt(asset.getCreatedAt())
            .updatedAt(asset.getUpdatedAt())
            .build();
    }
    
    public static AssetResponse from(Asset asset, CoinResponse coin) {
        return AssetResponse.builder()
            .id(asset.getId())
            .exchangeCode(asset.getExchangeCode())
            .coinId(asset.getCoinId())
            .symbol(asset.getSymbol())
            .tradeBySymbol(asset.getTradeBySymbol())
            .quantity(asset.getQuantity())
            .lockedQuantity(asset.getLockedQuantity())
            .avgBuyPrice(asset.getAvgBuyPrice())
            .avgBuyPriceModified(asset.getAvgBuyPriceModified())
            .createdAt(asset.getCreatedAt())
            .updatedAt(asset.getUpdatedAt())
            .coin(coin)
            .build();
    }
}

