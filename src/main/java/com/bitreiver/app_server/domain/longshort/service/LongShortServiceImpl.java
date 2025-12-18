package com.bitreiver.app_server.domain.longshort.service;

import org.springframework.stereotype.Service;
import com.bitreiver.app_server.domain.longshort.dto.LongShortResponse;
import com.bitreiver.app_server.domain.longshort.dto.BinanceLongShortRatioResponse;
import com.bitreiver.app_server.global.cache.RedisCacheService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class LongShortServiceImpl implements LongShortService {
    
    private final RedisCacheService redisCacheService;

    private static final String LONG_SHORT_RATIO_CACHE_KEY_PREFIX = "binance:longShortRatio:";

    private String getCacheKey(String symbol, String period) {
        return LONG_SHORT_RATIO_CACHE_KEY_PREFIX + symbol + ":" + period;
    }

    @Override
    public List<LongShortResponse> getLongShortRatio(String symbol, String period) {
        String cacheKey = getCacheKey(symbol, period);

        TypeReference<List<BinanceLongShortRatioResponse>> typeRef =
                new TypeReference<List<BinanceLongShortRatioResponse>>() {};

        Optional<List<BinanceLongShortRatioResponse>> cached =
                redisCacheService.get(cacheKey, typeRef);

        if (cached.isEmpty()) {
            log.info("롱숏 비율 캐시 미스 - symbol: {}, period: {}, key: {}", symbol, period, cacheKey);
            return List.of();
        }

        return cached.get().stream()
                .map(LongShortResponse::from)
                .toList();
    }
}
