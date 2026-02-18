package com.bitreiver.app_server.domain.price.service;

import com.bitreiver.app_server.domain.coin.entity.Coin;
import com.bitreiver.app_server.domain.coin.repository.CoinRepository;
import com.bitreiver.app_server.domain.price.dto.CoinPriceDayTodayDto;
import com.bitreiver.app_server.domain.price.dto.CoinTickerPriceDto;
import com.bitreiver.app_server.domain.price.event.TickerPricesUpdatedEvent;
import com.bitreiver.app_server.global.cache.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Redis에 저장된 당일 일봉을 ticker 데이터로 high/low/trade_price 갱신.
 * ticker 수집이 완료될 때마다(TickerPricesUpdatedEvent) 실행된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoinPriceDayTodayMergeService {

    private final RedisCacheService redisCacheService;
    private final CoinPriceTickerService coinPriceTickerService;
    private final CoinRepository coinRepository;

    /**
     * Ticker 수집 완료 시에만 당일 일봉 Redis를 ticker와 병합: high=max, low=min, trade_price는 변경 시에만 갱신.
     */
    @Async("coinPriceExecutor")
    @EventListener(TickerPricesUpdatedEvent.class)
    public void onTickerPricesUpdated(TickerPricesUpdatedEvent event) {
        mergeTodayCandlesWithTicker();
    }

    public void mergeTodayCandlesWithTicker() {
        List<Coin> upbitCoins = coinRepository.findByExchangeAndIsActive("UPBIT", true);
        List<Coin> coinoneCoins = coinRepository.findByExchangeAndIsActive("COINONE", true);
        if (upbitCoins.isEmpty() && coinoneCoins.isEmpty()) {
            return;
        }
        Map<String, CoinTickerPriceDto> tickerByMarket = new HashMap<>();
        for (CoinTickerPriceDto dto : coinPriceTickerService.getAllCoinPrices()) {
            if (dto.getMarket() != null) {
                tickerByMarket.put(dto.getMarket(), dto);
            }
        }
        int updated = 0;
        for (Coin coin : upbitCoins) {
            if (coin.getMarketCode() == null) continue;
            if (mergeOne(coin.getExchange(), coin.getMarketCode(), tickerByMarket.get(coin.getMarketCode()))) {
                updated++;
            }
        }
        for (Coin coin : coinoneCoins) {
            if (coin.getMarketCode() == null) continue;
            if (mergeOne(coin.getExchange(), coin.getMarketCode(), tickerByMarket.get(coin.getMarketCode()))) {
                updated++;
            }
        }
        if (updated > 0 && log.isTraceEnabled()) {
            log.trace("당일 봉 ticker 병합 완료: {}건", updated);
        }
    }

    /**
     * @return Redis에 당일 봉이 있고 ticker로 갱신했으면 true
     */
    private boolean mergeOne(String exchange, String marketCode, CoinTickerPriceDto ticker) {
        String redisKey = CoinPriceDayTodayDto.redisKey(exchange, marketCode);
        Optional<CoinPriceDayTodayDto> opt = redisCacheService.get(redisKey, CoinPriceDayTodayDto.class);
        if (opt.isEmpty() || ticker == null) {
            return false;
        }
        CoinPriceDayTodayDto today = opt.get();
        BigDecimal newHigh = today.getHighPrice();
        BigDecimal newLow = today.getLowPrice();
        BigDecimal newTrade = today.getTradePrice();
        if (ticker.getHighPrice() != null) {
            newHigh = today.getHighPrice() != null
                ? today.getHighPrice().max(ticker.getHighPrice())
                : ticker.getHighPrice();
        }
        if (ticker.getLowPrice() != null) {
            newLow = today.getLowPrice() != null
                ? today.getLowPrice().min(ticker.getLowPrice())
                : ticker.getLowPrice();
        }
        if (ticker.getTradePrice() != null && !ticker.getTradePrice().equals(today.getTradePrice())) {
            newTrade = ticker.getTradePrice();
        }
        CoinPriceDayTodayDto updated = CoinPriceDayTodayDto.builder()
            .coinId(today.getCoinId())
            .exchange(today.getExchange())
            .marketCode(today.getMarketCode())
            .candleDateTimeUtc(today.getCandleDateTimeUtc())
            .candleDateTimeKst(today.getCandleDateTimeKst())
            .openingPrice(today.getOpeningPrice())
            .highPrice(newHigh)
            .lowPrice(newLow)
            .tradePrice(newTrade)
            .timestamp(today.getTimestamp())
            .candleAccTradePrice(today.getCandleAccTradePrice())
            .candleAccTradeVolume(today.getCandleAccTradeVolume())
            .prevClosingPrice(today.getPrevClosingPrice())
            .changePrice(today.getChangePrice())
            .changeRate(today.getChangeRate())
            .convertedTradePrice(today.getConvertedTradePrice())
            .build();
        redisCacheService.set(redisKey, updated, CoinPriceDayTodayDto.REDIS_TTL_SECONDS);
        return true;
    }
}
