package com.bitreiver.app_server.domain.coin.service;

import com.bitreiver.app_server.domain.coin.dto.CoinResponse;
import com.bitreiver.app_server.domain.coin.entity.Coin;
import com.bitreiver.app_server.domain.coin.repository.CoinRepository;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoinServiceImpl implements CoinService {
    
    private final CoinRepository coinRepository;
    
    @Override
    public List<CoinResponse> getAllCoins() {
        List<Coin> coins = coinRepository.findAllByIsActive(true);
        return coins.stream().map(CoinResponse::from).toList();
    }
    
    @Override
    public CoinResponse getCoinById(Integer id) {
        Coin coin = coinRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.COIN_NOT_FOUND));
        return CoinResponse.from(coin);
    }
    
    @Override
    public List<CoinResponse> getCoinsByExchange(String exchange) {
        List<Coin> coins = coinRepository.findByExchangeAndIsActive(exchange, true);
        return coins.stream().map(CoinResponse::from).toList();
    }

    @Override
    public List<CoinResponse> getAllCoinsByQuoteCurrency(String quoteCurrency) {
        List<Coin> coins = coinRepository.findAllByQuoteCurrencyAndIsActive(quoteCurrency, true);
        return coins.stream().map(CoinResponse::from).toList();
    }
}

