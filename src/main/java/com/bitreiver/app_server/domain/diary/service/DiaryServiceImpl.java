package com.bitreiver.app_server.domain.diary.service;

import com.bitreiver.app_server.domain.diary.dto.DiaryRequest;
import com.bitreiver.app_server.domain.diary.dto.DiaryResponse;
import com.bitreiver.app_server.domain.diary.dto.TradingHistoryWithDiaryResponse;
import com.bitreiver.app_server.domain.diary.entity.Diary;
import com.bitreiver.app_server.domain.diary.repository.DiaryRepository;
import com.bitreiver.app_server.domain.trading.entity.TradingHistory;
import com.bitreiver.app_server.domain.trading.repository.TradingHistoryRepository;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {
    
    private final DiaryRepository diaryRepository;
    private final TradingHistoryRepository tradingHistoryRepository;
    
    @Override
    @Transactional
    public DiaryResponse createDiary(UUID userId, DiaryRequest request) {
        TradingHistory tradingHistory = tradingHistoryRepository.findById(request.getTradingHistoryId())
            .orElseThrow(() -> new CustomException(ErrorCode.TRADING_HISTORY_NOT_FOUND));
        
        if (!tradingHistory.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        
        if (diaryRepository.existsByTradingHistoryId(request.getTradingHistoryId())) {
            throw new CustomException(ErrorCode.DIARY_ALREADY_EXISTS);
        }
        
        Diary diary = Diary.builder()
            .tradingHistoryId(request.getTradingHistoryId())
            .content(request.getContent())
            .tags(request.getTags())
            .tradingMind(request.getTradingMindAsEnum())
            .build();
        
        diaryRepository.save(diary);
        
        return DiaryResponse.from(diary);
    }
    
    @Override
    public DiaryResponse getDiaryById(UUID userId, Integer id) {
        Diary diary = diaryRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        
        return DiaryResponse.from(diary);
    }
    
    @Override
    public DiaryResponse getDiaryByTradingHistoryId(UUID userId, Integer tradingHistoryId) {
        TradingHistory tradingHistory = tradingHistoryRepository.findById(tradingHistoryId)
            .orElseThrow(() -> new CustomException(ErrorCode.TRADING_HISTORY_NOT_FOUND));
        
        if (!tradingHistory.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        
        Diary diary = diaryRepository.findByTradingHistoryId(tradingHistoryId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        
        return DiaryResponse.from(diary);
    }
    
    @Override
    public List<DiaryResponse> getUserDiaries(UUID userId) {
        List<Diary> diaries = diaryRepository.findByUserId(userId);
        
        return diaries.stream()
            .map(DiaryResponse::from)
            .toList();
    }
    
    @Override
    @Transactional
    public DiaryResponse updateDiary(UUID userId, Integer id, DiaryRequest request) {
        Diary diary = diaryRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        
        if (request.getTradingHistoryId() != null && 
            !request.getTradingHistoryId().equals(diary.getTradingHistoryId())) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "tradingHistoryId는 변경할 수 없습니다.");
        }
        
        if (request.getContent() != null) {
            diary.setContent(request.getContent());
        }
        
        if (request.getTags() != null) {
            diary.setTags(request.getTags());
        }
        
        if (request.getTradingMind() != null) {
            diary.setTradingMind(request.getTradingMindAsEnum());
        }
        
        diaryRepository.save(diary);
        
        return DiaryResponse.from(diary);
    }
    
    @Override
    @Transactional
    public void deleteDiary(UUID userId, Integer id) {
        Diary diary = diaryRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        
        diaryRepository.delete(diary);
    }
    
    @Override
    public List<TradingHistoryWithDiaryResponse> getTradingHistoriesWithDiariesByDateRange(
        UUID userId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        List<Object[]> results = diaryRepository.findTradingHistoriesWithDiariesByDateRange(
            userId, startDate, endDate
        );
        
        return results.stream()
            .map(result -> {
                TradingHistory tradingHistory = (TradingHistory) result[0];
                Diary diary = (Diary) result[1];
                return TradingHistoryWithDiaryResponse.from(tradingHistory, diary);
            })
            .toList();
    }
}

