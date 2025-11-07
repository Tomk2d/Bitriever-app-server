package com.bitreiver.app_server.domain.trading.service;

import com.bitreiver.app_server.domain.coin.dto.CoinResponse;
import com.bitreiver.app_server.domain.coin.entity.Coin;
import com.bitreiver.app_server.domain.coin.repository.CoinRepository;
import com.bitreiver.app_server.domain.trading.dto.TradingHistoryResponse;
import com.bitreiver.app_server.domain.trading.repository.TradingHistoryRepository;
import com.bitreiver.app_server.global.common.dto.PageResponse;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradingHistoryServiceImpl implements TradingHistoryService {
    
    private final TradingHistoryRepository tradingHistoryRepository;
    private final CoinRepository coinRepository;
    
    @Override
    public PageResponse<TradingHistoryResponse> getUserTradingHistories(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var tradingPage = tradingHistoryRepository.findByUserIdOrderByTradeTimeDesc(userId, pageable);
        
        var content = tradingPage.getContent().stream()
            .map(TradingHistoryResponse::from)
            .toList();
        
        return PageResponse.of(
            content,
            tradingPage.getNumber(),
            tradingPage.getSize(),
            tradingPage.getTotalElements()
        );
    }
    
    @Override
    public List<TradingHistoryResponse> getUserTradingHistoriesByDateRange(
        UUID userId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        validateDateRange(startDate, endDate);
        
        var tradingHistories = tradingHistoryRepository.findByUserIdAndTradeTimeBetween(
            userId, startDate, endDate
        );
        
        if (tradingHistories.isEmpty()) {
            return List.of();
        }
        
        List<Integer> coinIds = tradingHistories.stream()
            .map(history -> history.getCoinId())
            .distinct()
            .toList();
        
        Map<Integer, CoinResponse> coinMap = coinRepository.findAllById(coinIds).stream()
            .collect(Collectors.toMap(Coin::getId, CoinResponse::from));
        
        return tradingHistories.stream()
            .map(history -> TradingHistoryResponse.from(history, coinMap.get(history.getCoinId())))
            .toList();
    }
    
    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }
        
        if (startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE);
        }
    }
}

