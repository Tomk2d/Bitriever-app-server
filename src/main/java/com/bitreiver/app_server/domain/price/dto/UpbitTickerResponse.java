package com.bitreiver.app_server.domain.price.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Upbit 티커 응답")
public class UpbitTickerResponse {
    @Schema(description ="페어(거래쌍)의 코드", example = "KRW-BTC")
    @JsonProperty("market")
    private String market;  // "KRW-BTC"
    
    @Schema(description = "최근 체결 일자 (UTC 기준)", example = "2024-01-01")
    @JsonProperty("trade_date")
    private String tradeDate;
    
    @Schema(description = "최근 체결 시각 (UTC 기준)", example = "10:00:00")
    @JsonProperty("trade_time")
    private String tradeTime;
    
    @Schema(description = "최근 체결 일자 (KST 기준)", example = "2024-01-01")
    @JsonProperty("trade_date_kst")
    private String tradeDateKst;
    
    @Schema(description = "최근 체결 시각 (KST 기준)", example = "10:00:00")
    @JsonProperty("trade_time_kst")
    private String tradeTimeKst;
    
    @Schema(description = "현재가 정보가 반영된 시각의 타임스탬프(ms)", example = "1714732800000")
    @JsonProperty("trade_timestamp")
    private Long tradeTimestamp;
    
    @Schema(description = "시가", example = "1000000.0")
    @JsonProperty("opening_price")
    private BigDecimal openingPrice;
    
    @Schema(description = "고가", example = "1100000.0")
    @JsonProperty("high_price")
    private BigDecimal highPrice;
    
    @Schema(description = "저가", example = "900000.0")
    @JsonProperty("low_price")
    private BigDecimal lowPrice;
    
    @Schema(description = "종가(현재가)", example = "1050000.0")
    @JsonProperty("trade_price")
    private BigDecimal tradePrice;
    
    @Schema(description = "전일 종가", example = "전일 종가 (UTC 0시 기준)")
    @JsonProperty("prev_closing_price")
    private BigDecimal prevClosingPrice;
    
    @Schema(description = "변동률", example = "EVEN(보합), RISE(상승), FALL(하락)")
    @JsonProperty("change")
    private String change;
    
    @Schema(description = "변동가", example = "현재가 - 전일 종가 -> 15000.024 , -293.12 ")
    @JsonProperty("change_price")
    private BigDecimal changePrice;
    
    @Schema(description = "변동률", example = "(현재가 - 전일 종가) / 전일 종가 -> 0.015 = 1.5% 상승 ")
    @JsonProperty("change_rate")
    private BigDecimal changeRate;
    
    @Schema(description = "전일 종가 대비 가격 변화.", example = "현재가 - 전일 종가 : 50000.0, -293.12")
    @JsonProperty("signed_change_price")
    private BigDecimal signedChangePrice;
    
    @Schema(description = "전일 종가 대비 가격 변화율", example = "(현재가 - 전일 종가) / 전일 종가 -> 0.015 = 1.5% 상승 ")
    @JsonProperty("signed_change_rate")
    private BigDecimal signedChangeRate;
    
    @Schema(description = "최근 거래 수량", example = "100.0")
    @JsonProperty("trade_volume")
    private BigDecimal tradeVolume;
    
    @Schema(description = "총 누적 거래 금액", example = "100000000.0")
    @JsonProperty("acc_trade_price")
    private BigDecimal accTradePrice;
    
    @Schema(description = "24시간 누적 거래 금액", example = "100000000.0")
    @JsonProperty("acc_trade_price_24h")
    private BigDecimal accTradePrice24h;
    
    @Schema(description = "거래량", example = "100.0")
    @JsonProperty("acc_trade_volume")
    private BigDecimal accTradeVolume;
    
    @Schema(description = "24시간 누적 거래량", example = "100.0")
    @JsonProperty("acc_trade_volume_24h")
    private BigDecimal accTradeVolume24h;
    
    @Schema(description = "52주 신고가(최고가)", example = "1100000.0")
    @JsonProperty("highest_52_week_price")
    private BigDecimal highest52WeekPrice;
    
    @Schema(description = "52주 최고가(최고가) 일자", example = "2024-01-01")
    @JsonProperty("highest_52_week_date")
    private String highest52WeekDate;
    
    @Schema(description = "52주 최저가", example = "900000.0")
    @JsonProperty("lowest_52_week_price")
    private BigDecimal lowest52WeekPrice;
    
    @Schema(description = "52주 최저가 일자", example = "2024-01-01")
    @JsonProperty("lowest_52_week_date")
    private String lowest52WeekDate;
    
    @Schema(description = "현재가 정보가 반영된 시각의 타임스탬프(ms)", example = "1714732800000")
    @JsonProperty("timestamp")
    private Long timestamp;
    
}
