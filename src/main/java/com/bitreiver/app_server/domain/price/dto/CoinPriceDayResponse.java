package com.bitreiver.app_server.domain.price.dto;

import com.bitreiver.app_server.domain.price.entity.CoinPriceDay;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinPriceDayResponse {
    private Integer id;
    private Integer coinId;
    private String marketCode;
    private LocalDateTime candleDateTimeUtc;
    private LocalDateTime candleDateTimeKst;
    private BigDecimal openingPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal tradePrice;
    private Long timestamp;
    private BigDecimal candleAccTradePrice;
    private BigDecimal candleAccTradeVolume;
    private BigDecimal prevClosingPrice;
    private BigDecimal changePrice;
    private BigDecimal changeRate;
    private BigDecimal convertedTradePrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CoinPriceDayResponse from(CoinPriceDay coinPriceDay) {
        return CoinPriceDayResponse.builder()
            .id(coinPriceDay.getId())
            .coinId(coinPriceDay.getCoinId())
            .marketCode(coinPriceDay.getMarketCode())
            .candleDateTimeUtc(coinPriceDay.getCandleDateTimeUtc())
            .candleDateTimeKst(coinPriceDay.getCandleDateTimeKst())
            .openingPrice(coinPriceDay.getOpeningPrice())
            .highPrice(coinPriceDay.getHighPrice())
            .lowPrice(coinPriceDay.getLowPrice())
            .tradePrice(coinPriceDay.getTradePrice())
            .timestamp(coinPriceDay.getTimestamp())
            .candleAccTradePrice(coinPriceDay.getCandleAccTradePrice())
            .candleAccTradeVolume(coinPriceDay.getCandleAccTradeVolume())
            .prevClosingPrice(coinPriceDay.getPrevClosingPrice())
            .changePrice(coinPriceDay.getChangePrice())
            .changeRate(coinPriceDay.getChangeRate())
            .convertedTradePrice(coinPriceDay.getConvertedTradePrice())
            .createdAt(coinPriceDay.getCreatedAt())
            .updatedAt(coinPriceDay.getUpdatedAt())
            .build();
    }
}
