package com.bitreiver.app_server.domain.longshort.dto;

import lombok.Getter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.bitreiver.app_server.domain.longshort.dto.BinanceLongShortRatioResponse;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LongShortResponse {
    private String symbol;
    private String longShortRatio;
    private String longAccount;
    private String shortAccount;
    private Long timestamp;

    public static LongShortResponse from(BinanceLongShortRatioResponse binanceLongShortRatioResponse) {
        return LongShortResponse.builder()
            .symbol(binanceLongShortRatioResponse.getSymbol())
            .longShortRatio(binanceLongShortRatioResponse.getLongShortRatio())
            .longAccount(binanceLongShortRatioResponse.getLongAccount())
            .shortAccount(binanceLongShortRatioResponse.getShortAccount())
            .timestamp(binanceLongShortRatioResponse.getTimestamp())
            .build();
    }
}
