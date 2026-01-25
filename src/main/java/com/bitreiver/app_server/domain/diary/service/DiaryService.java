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
    DiaryResponse updateDiaryWithImageManagement(UUID userId, Integer id, DiaryRequest request);
    void deleteDiary(UUID userId, Integer id);
    List<TradingHistoryWithDiaryResponse> getTradingHistoriesWithDiariesByDateRange(
        UUID userId,
        LocalDateTime startDate,
        LocalDateTime endDate
    );
    List<String> extractAllImagePaths(String content);
    
    /**
     * 새로 저장된 TradingHistory에 대해 빈 매매일지를 자동 생성합니다.
     * 이미 존재하는 매매일지는 건너뜁니다.
     * 
     * @param tradingHistoryIds 새로 저장된 TradingHistory ID 목록
     * @return 생성된 매매일지 개수
     */
    int createDiariesForTradingHistories(List<Integer> tradingHistoryIds);
}

