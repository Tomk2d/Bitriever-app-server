package com.bitreiver.app_server.domain.price.service;

import com.bitreiver.app_server.domain.price.repository.CoinPriceDayRepository;
import com.bitreiver.app_server.domain.price.dto.CoinPriceDayResponse;
import com.bitreiver.app_server.domain.price.dto.CoinPriceDayRangeRequest;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import com.bitreiver.app_server.domain.price.entity.CoinPriceDay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CoinPriceDayServiceImpl implements CoinPriceDayService {

    private final CoinPriceDayRepository coinPriceDayRepository;

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

        return coinPriceDays.stream()
            .map(CoinPriceDayResponse::from)
            .toList();
    }
}   
