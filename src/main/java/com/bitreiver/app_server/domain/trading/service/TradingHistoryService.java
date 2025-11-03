package com.bitreiver.app_server.domain.trading.service;

import com.bitreiver.app_server.domain.trading.dto.TradingHistoryResponse;
import com.bitreiver.app_server.global.common.dto.PageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TradingHistoryService {
    PageResponse<TradingHistoryResponse> getUserTradingHistories(UUID userId, int page, int size);
    
    List<TradingHistoryResponse> getUserTradingHistoriesByDateRange(
        UUID userId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
}

