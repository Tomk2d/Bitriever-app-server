package com.bitreiver.app_server.domain.price.service;

import com.bitreiver.app_server.domain.price.repository.CoinPriceDayRepository;
import com.bitreiver.app_server.domain.price.dto.CoinPriceDayResponse;
import com.bitreiver.app_server.domain.price.dto.CoinPriceDayRangeRequest;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import com.bitreiver.app_server.domain.price.entity.CoinPriceDay;
import com.bitreiver.app_server.global.cache.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoinPriceDayServiceImpl implements CoinPriceDayService {

    private final CoinPriceDayRepository coinPriceDayRepository;
    private final RedisCacheService redisCacheService;

    private static final String COIN_PRICE_DAY_CACHE_KEY_PREFIX = "coin:price:day:";
    
    private String getCacheKey(Integer coinId) {
        return COIN_PRICE_DAY_CACHE_KEY_PREFIX + coinId;
    }

    private double toTimestamp(LocalDateTime dateTime) {
        return (double) dateTime.atZone(ZoneOffset.UTC).toEpochSecond();
    }

    @Override
    public CoinPriceDayResponse getCoinPriceDayById(Integer coinId) {
        CoinPriceDay coinPriceDay = coinPriceDayRepository.findById(coinId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        return CoinPriceDayResponse.from(coinPriceDay);
    }

    @Override
    public List<CoinPriceDayResponse> getCoinPriceDayAllById(Integer coinId) {
        String cacheKey = getCacheKey(coinId);

        List<CoinPriceDayResponse> cachedData = redisCacheService.zRangeAll(cacheKey, CoinPriceDayResponse.class);

        if(!cachedData.isEmpty()) {
            cachedData.sort((a, b) -> b.getCandleDateTimeUtc().compareTo(a.getCandleDateTimeUtc()));
            return cachedData;
        }

        List<CoinPriceDay> coinPriceDays = coinPriceDayRepository.findAllByCoinIdOrderByCandleDateTimeUtcDesc(coinId);
        List<CoinPriceDayResponse> response = coinPriceDays.stream()
            .map(CoinPriceDayResponse::from)
            .toList();
        
        if(response.isEmpty()) {
            cachedCoinPriceDays(coinId, response);
        }

        return response;
    }

    @Override
    public List<CoinPriceDayResponse> getCoinPriceDayRangeById(CoinPriceDayRangeRequest request) {
        Integer coinId = request.getCoinId();
        LocalDateTime startDate = request.getStartDate();
        LocalDateTime endDate = request.getEndDate();

        String cacheKey = getCacheKey(coinId);

        double minScore = toTimestamp(startDate);
        double maxScore = toTimestamp(endDate);

        List<CoinPriceDayResponse> cachedData = redisCacheService.zRangeByScore(
            cacheKey, 
            minScore, 
            maxScore, 
            CoinPriceDayResponse.class
        );

        if(!cachedData.isEmpty()) {
            cachedData.sort((a, b) -> b.getCandleDateTimeUtc().compareTo(a.getCandleDateTimeUtc()));
            return cachedData;
        }

        List<CoinPriceDay> coinPriceDays = coinPriceDayRepository.findByCoinIdAndUtcDateRange(
            coinId, 
            startDate, 
            endDate
        );

        List<CoinPriceDayResponse> response = coinPriceDays.stream()
            .map(CoinPriceDayResponse::from)
            .toList();

        // 전체 데이터가 캐시에 없을 수 있으므로, 조회한 데이터를 캐시에 추가
        // (전체 캐시가 있는지 확인 후, 없으면 전체를 로드하거나 개별 추가)
        if(!response.isEmpty()) {
            boolean hasFullCache = redisCacheService.zExists(cacheKey);

            if(!hasFullCache) {
                List<CoinPriceDay> allCoinPriceDays = coinPriceDayRepository.findAllByCoinIdOrderByCandleDateTimeUtcDesc(coinId);
                List<CoinPriceDayResponse> allResponse = allCoinPriceDays.stream()
                    .map(CoinPriceDayResponse::from)
                    .toList();

                cachedCoinPriceDays(coinId, allResponse);
            }
        }else{
            for(CoinPriceDayResponse coinPriceDay : response) {
                double score = toTimestamp(coinPriceDay.getCandleDateTimeUtc());
                redisCacheService.zAdd(cacheKey, coinPriceDay, score);
            }
        }

        return response;
        
    }

    private void cachedCoinPriceDays(Integer coinId, List<CoinPriceDayResponse> coinPriceDays) {
        if(coinPriceDays == null || coinPriceDays.isEmpty()) {
            log.debug("캐시에 추가할 데이터가 없습니다 - coinId: {}", coinId);
            return;
        }

        String cacheKey = getCacheKey(coinId);

        redisCacheService.zAddAll(
            cacheKey, 
            coinPriceDays, 
            coinPriceDay -> toTimestamp(coinPriceDay.getCandleDateTimeUtc())
        );
    }
}   
