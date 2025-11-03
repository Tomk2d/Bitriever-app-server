package com.bitreiver.app_server.domain.trading.service;

import com.bitreiver.app_server.domain.trading.dto.TradingHistoryResponse;
import com.bitreiver.app_server.domain.trading.repository.TradingHistoryRepository;
import com.bitreiver.app_server.global.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TradingHistoryServiceImpl implements TradingHistoryService {
    
    private final TradingHistoryRepository tradingHistoryRepository;
    
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
        var tradingHistories = tradingHistoryRepository.findByUserIdAndTradeTimeBetween(
            userId, startDate, endDate
        );
        
        return tradingHistories.stream()
            .map(TradingHistoryResponse::from)
            .toList();
    }
}

