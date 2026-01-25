package com.bitreiver.app_server.domain.price.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "코인원 티커 응답")
public class CoinoneTickerResponse {
    
    @Schema(description = "결과 코드", example = "success")
    @JsonProperty("result")
    private String result;
    
    @Schema(description = "에러 코드", example = "0")
    @JsonProperty("error_code")
    private String errorCode;
    
    @Schema(description = "서버 시간 (ms)", example = "1714732800000")
    @JsonProperty("server_time")
    private Long serverTime;
    
    @Schema(description = "티커 정보 목록")
    @JsonProperty("tickers")
    private List<CoinoneTicker> tickers;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "코인원 개별 티커 정보")
    public static class CoinoneTicker {
        
        @Schema(description = "마켓 기준 통화", example = "KRW")
        @JsonProperty("quote_currency")
        private String quoteCurrency;
        
        @Schema(description = "티커 종목 명", example = "BTC")
        @JsonProperty("target_currency")
        private String targetCurrency;
        
        @Schema(description = "티커 생성 시점 (Unix time) (ms)", example = "1714732800000")
        @JsonProperty("timestamp")
        private Long timestamp;
        
        @Schema(description = "고가 (24시간 기준)", example = "1000000")
        @JsonProperty("high")
        private String high;
        
        @Schema(description = "저가 (24시간 기준)", example = "900000")
        @JsonProperty("low")
        private String low;
        
        @Schema(description = "시가 (24시간 기준)", example = "950000")
        @JsonProperty("first")
        private String first;
        
        @Schema(description = "종가 (24시간 기준)", example = "980000")
        @JsonProperty("last")
        private String last;
        
        @Schema(description = "24시간 기준 종목 체결 금액 (원화)", example = "1000000000")
        @JsonProperty("quote_volume")
        private String quoteVolume;
        
        @Schema(description = "24시간 기준 종목 체결량 (종목)", example = "1000")
        @JsonProperty("target_volume")
        private String targetVolume;
        
        @Schema(description = "매도 최저가의 오더북 정보")
        @JsonProperty("best_asks")
        private List<OrderBook> bestAsks;
        
        @Schema(description = "매수 최고가의 오더북 정보")
        @JsonProperty("best_bids")
        private List<OrderBook> bestBids;
        
        @Schema(description = "티커 별 ID 값", example = "1234567890")
        @JsonProperty("id")
        private String id;
        
        @Schema(description = "전일 고가 (additional_data=true일 때)", example = "920000")
        @JsonProperty("yesterday_high")
        private String yesterdayHigh;
        
        @Schema(description = "전일 저가 (additional_data=true일 때)", example = "880000")
        @JsonProperty("yesterday_low")
        private String yesterdayLow;
        
        @Schema(description = "전일 시가 (additional_data=true일 때)", example = "900000")
        @JsonProperty("yesterday_first")
        private String yesterdayFirst;
        
        @Schema(description = "전일 종가 (additional_data=true일 때)", example = "920000")
        @JsonProperty("yesterday_last")
        private String yesterdayLast;
        
        @Schema(description = "전일 체결 금액 (additional_data=true일 때)", example = "900000000")
        @JsonProperty("yesterday_quote_volume")
        private String yesterdayQuoteVolume;
        
        @Schema(description = "전일 체결량 (additional_data=true일 때)", example = "900")
        @JsonProperty("yesterday_target_volume")
        private String yesterdayTargetVolume;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "오더북 정보")
    public static class OrderBook {
        
        @Schema(description = "가격", example = "980000")
        @JsonProperty("price")
        private String price;
        
        @Schema(description = "수량", example = "1.5")
        @JsonProperty("qty")
        private String qty;
    }
}
