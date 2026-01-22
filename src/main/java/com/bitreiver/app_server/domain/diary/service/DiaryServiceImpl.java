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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final DiaryRepository diaryRepository;
    private final TradingHistoryRepository tradingHistoryRepository;
    private final DiaryImageService diaryImageService;
    
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
    public DiaryResponse updateDiaryWithImageManagement(UUID userId, Integer id, DiaryRequest request) {
        DiaryResponse existingDiary = getDiaryById(userId, id);
        
        // 이미지 개수 검증
        if (request.getContent() != null) {
            List<String> newImagePaths = extractAllImagePaths(request.getContent());
            if (newImagePaths.size() > 5) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 5개까지 추가할 수 있습니다.");
            }
        }
        
        List<String> existingImagePaths = extractAllImagePaths(existingDiary.getContent());
        List<String> newImagePaths = extractAllImagePaths(request.getContent());
        
        List<String> imagesToDelete = existingImagePaths.stream()
                .filter(path -> !newImagePaths.contains(path))
                .collect(java.util.stream.Collectors.toList());
        
        if (!imagesToDelete.isEmpty()) {
            try {
                diaryImageService.deleteAllImages(id, imagesToDelete);
                log.info("삭제된 이미지 제거 완료: diaryId={}, count={}", id, imagesToDelete.size());
            } catch (Exception e) {
                log.error("MinIO 이미지 삭제 실패: diaryId={}, images={}", id, imagesToDelete, e);
            }
        }
        
        return updateDiary(userId, id, request);
    }
    
    @Override
    @Transactional
    public void deleteDiary(UUID userId, Integer id) {
        Diary diary = diaryRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.DIARY_NOT_FOUND));
        
        // content에서 모든 이미지 경로 추출
        List<String> imagePaths = extractAllImagePaths(diary.getContent());
        
        // MinIO에서 이미지 파일 삭제
        if (!imagePaths.isEmpty()) {
            diaryImageService.deleteAllImages(id, imagePaths);
        }
        
        diaryRepository.delete(diary);
    }
    
    @Override
    public List<String> extractAllImagePaths(String content) {
        List<String> imagePaths = new ArrayList<>();
        
        if (content == null || content.trim().isEmpty()) {
            return imagePaths;
        }
        
        try {
            Map<String, Object> contentMap = objectMapper.readValue(content, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> blocks = (List<Map<String, Object>>) contentMap.get("blocks");
            
            if (blocks != null) {
                for (Map<String, Object> block : blocks) {
                    if ("image".equals(block.get("type"))) {
                        String path = (String) block.get("path");
                        if (path != null) {
                            imagePaths.add(path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Content에서 이미지 경로 추출 실패", e);
        }
        
        return imagePaths;
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

