package com.bitreiver.app_server.domain.coin.service;

import com.bitreiver.app_server.domain.coin.dto.CoinResponse;

import java.util.List;

public interface CoinService {
    List<CoinResponse> getAllCoins();
    CoinResponse getCoinById(Integer id);
    List<CoinResponse> getCoinsByExchange(String exchange);
}

