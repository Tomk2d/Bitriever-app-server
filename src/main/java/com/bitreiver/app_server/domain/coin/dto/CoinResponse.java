package com.bitreiver.app_server.domain.coin.dto;

import com.bitreiver.app_server.domain.coin.entity.Coin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinResponse {
    private Integer id;
    private String symbol;
    private String quoteCurrency;
    private String marketCode;
    private String koreanName;
    private String englishName;
    private String imgUrl;
    private String exchange;
    private Boolean isActive;
    
    public static CoinResponse from(Coin coin) {
        return CoinResponse.builder()
            .id(coin.getId())
            .symbol(coin.getSymbol())
            .quoteCurrency(coin.getQuoteCurrency())
            .marketCode(coin.getMarketCode())
            .koreanName(coin.getKoreanName())
            .englishName(coin.getEnglishName())
            .imgUrl(coin.getImgUrl())
            .exchange(coin.getExchange())
            .isActive(coin.getIsActive())
            .build();
    }
}

