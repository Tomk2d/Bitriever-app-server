package com.bitreiver.app_server.domain.coin.service;

import com.bitreiver.app_server.domain.coin.dto.CoinResponse;
import com.bitreiver.app_server.domain.coin.entity.Coin;
import com.bitreiver.app_server.domain.coin.repository.CoinRepository;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import com.bitreiver.app_server.global.cache.RedisCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {
    
    private final CoinRepository coinRepository;
    private final RedisCacheService redisCacheService;

    private static final String ALL_COINS_CACHE_KEY = "all_coins";
    private static final String EXCHANGE_PREFIX_CACHE_KEY = "coins:exchange:";
    private static final TypeReference<List<CoinResponse>> COIN_LIST_TYPE = new TypeReference<List<CoinResponse>>() {};

    private String getCacheKey(String exchange) {
        return exchange != null ? EXCHANGE_PREFIX_CACHE_KEY + exchange : ALL_COINS_CACHE_KEY;
    }
    
    @Override
    public List<CoinResponse> getAllCoins() {
        String cacheKey = getCacheKey(null);  // 모든 거래소 조회

        return redisCacheService.get(cacheKey, COIN_LIST_TYPE)
            .orElseGet(() -> {
                List<Coin> coins = coinRepository.findAllByIsActive(true);
                List<CoinResponse> coinResponses = coins.stream().map(CoinResponse::from).toList();
                redisCacheService.set(cacheKey, coinResponses);
                return coinResponses;
            });
    }
    
    @Override
    public CoinResponse getCoinById(Integer id) {
        Coin coin = coinRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.COIN_NOT_FOUND));
        return CoinResponse.from(coin);
    }
    
    @Override
    public List<CoinResponse> getCoinsByExchange(String exchange) {
        String cacheKey = getCacheKey(exchange);

        return redisCacheService.get(cacheKey, COIN_LIST_TYPE)
            .orElseGet(() -> {
                List<Coin> coins = coinRepository.findByExchangeAndIsActive(exchange, true);
                List<CoinResponse> coinResponses = coins.stream().map(CoinResponse::from).toList();
                redisCacheService.set(cacheKey, coinResponses);
                return coinResponses;
            });
    }

    @Override
    public List<CoinResponse> getAllCoinsByQuoteCurrency(String quoteCurrency) {
        List<Coin> coins = coinRepository.findAllByQuoteCurrencyAndIsActive(quoteCurrency, true);
        return coins.stream().map(CoinResponse::from).toList();
    }
}

