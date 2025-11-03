package com.bitreiver.app_server.domain.diary.service;

import com.bitreiver.app_server.domain.diary.dto.DiaryRequest;
import com.bitreiver.app_server.domain.diary.dto.DiaryResponse;
import com.bitreiver.app_server.domain.diary.dto.TradingHistoryWithDiaryResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DiaryService {
    DiaryResponse createDiary(UUID userId, DiaryRequest request);
    DiaryResponse getDiaryById(UUID userId, Integer id);
    DiaryResponse getDiaryByTradingHistoryId(UUID userId, Integer tradingHistoryId);
    List<DiaryResponse> getUserDiaries(UUID userId);
    DiaryResponse updateDiary(UUID userId, Integer id, DiaryRequest request);
    void deleteDiary(UUID userId, Integer id);
    List<TradingHistoryWithDiaryResponse> getTradingHistoriesWithDiariesByDateRange(
        UUID userId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
}

