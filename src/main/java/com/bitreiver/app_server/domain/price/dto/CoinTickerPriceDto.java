package com.bitreiver.app_server.domain.price.dto;

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
@Schema(description = "코인 티커 가격 정보 (브로드캐스팅용)")
public class CoinTickerPriceDto {
    
    @Schema(description = "페어(거래쌍)의 코드", example = "KRW-BTC")
    private String market;
    
    @Schema(description = "최근 체결 일자 (UTC 기준)", example = "20240101")
    private String tradeDate;
    
    @Schema(description = "최근 체결 시각 (UTC 기준)", example = "100000")
    private String tradeTime;
    
    @Schema(description = "최근 체결 일자 (KST 기준)", example = "20240101")
    private String tradeDateKst;
    
    @Schema(description = "최근 체결 시각 (KST 기준)", example = "190000")
    private String tradeTimeKst;
    
    @Schema(description = "현재가 정보가 반영된 시각의 타임스탬프(ms)", example = "1714732800000")
    private Long tradeTimestamp;
    
    @Schema(description = "시가", example = "1000000.0")
    private BigDecimal openingPrice;
    
    @Schema(description = "고가", example = "1100000.0")
    private BigDecimal highPrice;
    
    @Schema(description = "저가", example = "900000.0")
    private BigDecimal lowPrice;
    
    @Schema(description = "종가(현재가)", example = "1050000.0")
    private BigDecimal tradePrice;
    
    @Schema(description = "전일 종가 (UTC 0시 기준)", example = "1000000.0")
    private BigDecimal prevClosingPrice;
    
    @Schema(description = "가격 변동 상태", example = "EVEN(보합), RISE(상승), FALL(하락)")
    private String change;
    
    @Schema(description = "전일 종가 대비 가격 변화", example = "50000.0")
    private BigDecimal changePrice;
    
    @Schema(description = "전일 종가 대비 가격 변화율", example = "0.05")
    private BigDecimal changeRate;
    
    @Schema(description = "전일 종가 대비 가격 변화 (부호 포함)", example = "50000.0")
    private BigDecimal signedChangePrice;
    
    @Schema(description = "전일 종가 대비 가격 변화율 (부호 포함)", example = "0.05")
    private BigDecimal signedChangeRate;
    
    @Schema(description = "최근 거래 수량", example = "100.0")
    private BigDecimal tradeVolume;
    
    @Schema(description = "누적 거래 금액 (UTC 0시 기준)", example = "100000000.0")
    private BigDecimal accTradePrice;
    
    @Schema(description = "24시간 누적 거래 금액", example = "100000000.0")
    private BigDecimal accTradePrice24h;
    
    @Schema(description = "누적 거래량 (UTC 0시 기준)", example = "100.0")
    private BigDecimal accTradeVolume;
    
    @Schema(description = "24시간 누적 거래량", example = "100.0")
    private BigDecimal accTradeVolume24h;
    
    @Schema(description = "52주 신고가", example = "1100000.0")
    private BigDecimal highest52WeekPrice;
    
    @Schema(description = "52주 신고가 달성일", example = "2024-01-01")
    private String highest52WeekDate;
    
    @Schema(description = "52주 신저가", example = "900000.0")
    private BigDecimal lowest52WeekPrice;
    
    @Schema(description = "52주 신저가 달성일", example = "2024-01-01")
    private String lowest52WeekDate;
    
    @Schema(description = "현재가 정보가 반영된 시각의 타임스탬프(ms)", example = "1714732800000")
    private Long timestamp;
    
    public static CoinTickerPriceDto from(UpbitTickerResponse ticker) {
        return CoinTickerPriceDto.builder()
            .market(ticker.getMarket())
            .tradeDate(ticker.getTradeDate())
            .tradeTime(ticker.getTradeTime())
            .tradeDateKst(ticker.getTradeDateKst())
            .tradeTimeKst(ticker.getTradeTimeKst())
            .tradeTimestamp(ticker.getTradeTimestamp())
            .openingPrice(ticker.getOpeningPrice())
            .highPrice(ticker.getHighPrice())
            .lowPrice(ticker.getLowPrice())
            .tradePrice(ticker.getTradePrice())
            .prevClosingPrice(ticker.getPrevClosingPrice())
            .change(ticker.getChange())
            .changePrice(ticker.getChangePrice())
            .changeRate(ticker.getChangeRate())
            .signedChangePrice(ticker.getSignedChangePrice())
            .signedChangeRate(ticker.getSignedChangeRate())
            .tradeVolume(ticker.getTradeVolume())
            .accTradePrice(ticker.getAccTradePrice())
            .accTradePrice24h(ticker.getAccTradePrice24h())
            .accTradeVolume(ticker.getAccTradeVolume())
            .accTradeVolume24h(ticker.getAccTradeVolume24h())
            .highest52WeekPrice(ticker.getHighest52WeekPrice())
            .highest52WeekDate(ticker.getHighest52WeekDate())
            .lowest52WeekPrice(ticker.getLowest52WeekPrice())
            .lowest52WeekDate(ticker.getLowest52WeekDate())
            .timestamp(ticker.getTimestamp())
            .build();
    }
    
    public static CoinTickerPriceDto from(CoinoneTickerResponse.CoinoneTicker ticker) {
        // String을 BigDecimal로 변환
        BigDecimal last = parseBigDecimal(ticker.getLast());
        BigDecimal first = parseBigDecimal(ticker.getFirst());
        BigDecimal high = parseBigDecimal(ticker.getHigh());
        BigDecimal low = parseBigDecimal(ticker.getLow());
        BigDecimal yesterdayLast = parseBigDecimal(ticker.getYesterdayLast());
        BigDecimal quoteVolume = parseBigDecimal(ticker.getQuoteVolume());
        BigDecimal targetVolume = parseBigDecimal(ticker.getTargetVolume());
        
        // DB/클라이언트와 동일한 형식: quote-target = "KRW-BTC" (fetch-server CoinoneServiceImpl marketCode와 일치)
        String market = (ticker.getQuoteCurrency() + "-" + ticker.getTargetCurrency()).toUpperCase();
        
        // 전일 종가 대비 변화 계산
        BigDecimal changePrice = null;
        BigDecimal changeRate = null;
        BigDecimal signedChangePrice = null;
        BigDecimal signedChangeRate = null;
        String change = "EVEN";
        
        if (yesterdayLast != null && yesterdayLast.compareTo(BigDecimal.ZERO) != 0) {
            // 변화가 계산
            changePrice = last.subtract(yesterdayLast);
            signedChangePrice = changePrice;
            
            // 변화율 계산
            signedChangeRate = changePrice.divide(yesterdayLast, 8, java.math.RoundingMode.HALF_UP);
            changeRate = signedChangeRate.abs();
            
            // change 상태 결정
            if (changePrice.compareTo(BigDecimal.ZERO) > 0) {
                change = "RISE";
            } else if (changePrice.compareTo(BigDecimal.ZERO) < 0) {
                change = "FALL";
            } else {
                change = "EVEN";
            }
        }
        
        // 타임스탬프에서 날짜/시간 추출 (간단한 형식)
        String tradeDate = null;
        String tradeTime = null;
        String tradeDateKst = null;
        String tradeTimeKst = null;
        Long tradeTimestamp = ticker.getTimestamp();
        
        if (tradeTimestamp != null) {
            java.time.Instant instant = java.time.Instant.ofEpochMilli(tradeTimestamp);
            java.time.ZonedDateTime utcTime = instant.atZone(java.time.ZoneId.of("UTC"));
            java.time.ZonedDateTime kstTime = instant.atZone(java.time.ZoneId.of("Asia/Seoul"));
            
            tradeDate = utcTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            tradeTime = utcTime.format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
            tradeDateKst = kstTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            tradeTimeKst = kstTime.format(java.time.format.DateTimeFormatter.ofPattern("HHmmss"));
        }
        
        return CoinTickerPriceDto.builder()
            .market(market)
            .tradeDate(tradeDate)
            .tradeTime(tradeTime)
            .tradeDateKst(tradeDateKst)
            .tradeTimeKst(tradeTimeKst)
            .tradeTimestamp(tradeTimestamp)
            .openingPrice(first)
            .highPrice(high)
            .lowPrice(low)
            .tradePrice(last)
            .prevClosingPrice(yesterdayLast)
            .change(change)
            .changePrice(changePrice)
            .changeRate(changeRate)
            .signedChangePrice(signedChangePrice)
            .signedChangeRate(signedChangeRate)
            .tradeVolume(null) // 코인원 API에는 최근 거래 수량이 없음
            .accTradePrice(null) // 코인원 API에는 UTC 0시 기준 누적 거래 금액이 없음
            .accTradePrice24h(quoteVolume)
            .accTradeVolume(null) // 코인원 API에는 UTC 0시 기준 누적 거래량이 없음
            .accTradeVolume24h(targetVolume)
            .highest52WeekPrice(null) // 코인원 API에는 52주 신고가 정보가 없음
            .highest52WeekDate(null)
            .lowest52WeekPrice(null) // 코인원 API에는 52주 신저가 정보가 없음
            .lowest52WeekDate(null)
            .timestamp(tradeTimestamp)
            .build();
    }
    
    private static BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
