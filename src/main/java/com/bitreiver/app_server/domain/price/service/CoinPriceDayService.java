package com.bitreiver.app_server.domain.price.service;

import com.bitreiver.app_server.domain.price.dto.CoinPriceDayResponse;
import com.bitreiver.app_server.domain.price.dto.CoinPriceDayRangeRequest;

import java.util.List;

public interface CoinPriceDayService {
    CoinPriceDayResponse getCoinPriceDayById(Integer id);
    List<CoinPriceDayResponse> getCoinPriceDayAllById(Integer id);
    List<CoinPriceDayResponse> getCoinPriceDayRangeById(CoinPriceDayRangeRequest request);
}
