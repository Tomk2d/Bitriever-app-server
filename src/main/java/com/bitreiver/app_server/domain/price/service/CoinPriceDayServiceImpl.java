package com.bitreiver.app_server.domain.price.service;

import com.bitreiver.app_server.domain.coin.repository.CoinRepository;
import com.bitreiver.app_server.domain.price.dto.CoinPriceDayRangeRequest;
import com.bitreiver.app_server.domain.price.dto.CoinPriceDayResponse;
import com.bitreiver.app_server.domain.price.dto.CoinPriceDayTodayDto;
import com.bitreiver.app_server.domain.price.entity.CoinPriceDay;
import com.bitreiver.app_server.domain.price.repository.CoinPriceDayRepository;
import com.bitreiver.app_server.global.cache.RedisCacheService;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoinPriceDayServiceImpl implements CoinPriceDayService {

    private final CoinPriceDayRepository coinPriceDayRepository;
    private final CoinRepository coinRepository;
    private final RedisCacheService redisCacheService;

    @Override
    public CoinPriceDayResponse getCoinPriceDayById(Integer coinId) {
        CoinPriceDay coinPriceDay = coinPriceDayRepository.findById(coinId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        return CoinPriceDayResponse.from(coinPriceDay);
    }

    @Override
    public List<CoinPriceDayResponse> getCoinPriceDayAllById(Integer coinId) {
        List<CoinPriceDay> coinPriceDays = coinPriceDayRepository.findAllByCoinIdOrderByCandleDateTimeUtcDesc(coinId);
        return coinPriceDays.stream()
            .map(CoinPriceDayResponse::from)
            .toList();
    }

    @Override
    public List<CoinPriceDayResponse> getCoinPriceDayRangeById(CoinPriceDayRangeRequest request) {
        List<CoinPriceDay> coinPriceDays = coinPriceDayRepository.findByCoinIdAndUtcDateRange(
            request.getCoinId(),
            request.getStartDate(),
            request.getEndDate()
        );
        List<CoinPriceDayResponse> result = new ArrayList<>(coinPriceDays.stream()
            .map(CoinPriceDayResponse::from)
            .toList());

        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        if (!request.getEndDate().toLocalDate().isBefore(todayUtc)) {
            Optional<CoinPriceDayTodayDto> todayOpt = coinRepository.findById(request.getCoinId())
                .flatMap(coin -> redisCacheService.get(
                    CoinPriceDayTodayDto.redisKey(coin.getExchange(), coin.getMarketCode()),
                    CoinPriceDayTodayDto.class
                ));
            todayOpt.map(CoinPriceDayResponse::fromTodayDto).ifPresent(dto -> result.add(0, dto));
        }
        return result;
    }
}   
