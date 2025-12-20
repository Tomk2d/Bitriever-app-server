package com.bitreiver.app_server.domain.feargreed.service;

import com.bitreiver.app_server.domain.feargreed.dto.FearGreedResponse;
import com.bitreiver.app_server.domain.feargreed.entity.FearGreedIndex;
import com.bitreiver.app_server.domain.feargreed.repository.FearGreedIndexRepository;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.bitreiver.app_server.global.cache.RedisCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.bitreiver.app_server.domain.feargreed.dto.FearGreedRedisDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FearGreedServiceImpl implements FearGreedService {
    
    private final FearGreedIndexRepository fearGreedIndexRepository;
    private final RedisCacheService redisCacheService;
    
    private static final String REDIS_KEY_HISTORY = "feargreed:history";
    private static final String REDIS_KEY_TODAY = "feargreed:today";
    
    @Override
    public FearGreedResponse getByDate(LocalDate date) {
        TypeReference<List<FearGreedRedisDto>> typeRef = new TypeReference<List<FearGreedRedisDto>>() {};
        Optional<List<FearGreedRedisDto>> historyList = redisCacheService.get(REDIS_KEY_HISTORY, typeRef);
        
        if (historyList.isPresent()) {
            Optional<FearGreedRedisDto> found = historyList.get().stream()
                .filter(item -> item.getDate().equals(date))
                .findFirst();
            
            if (found.isPresent()) {
                FearGreedRedisDto dto = found.get();
                return FearGreedResponse.builder()
                    .date(dto.getDate())
                    .value(dto.getValue())
                    .build();
            }
        }
        
        log.warn("Redis 캐시 미스 - db 조회 시도 합니다. - date: {}", date);
        FearGreedIndex index = fearGreedIndexRepository.findByDate(date)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));
        
        return FearGreedResponse.builder()
            .id(index.getId())
            .date(index.getDate())
            .value(index.getValue())
            .build();
    }

    @Override
    public FearGreedResponse getToday() {
        Optional<FearGreedRedisDto> todayData = redisCacheService.get(REDIS_KEY_TODAY, FearGreedRedisDto.class);
        
        if (todayData.isEmpty()) {
            log.warn("Redis에서 오늘 데이터를 찾을 수 없습니다 - key: {}", REDIS_KEY_TODAY);
            throw new CustomException(ErrorCode.NOT_FOUND, "오늘의 공포/탐욕 지수 데이터가 없습니다.");
        }
        
        FearGreedRedisDto dto = todayData.get();
        return FearGreedResponse.builder()
            .date(dto.getDate())
            .value(dto.getValue())
            .build();
    }

    @Override
    public List<FearGreedResponse> getAllHistory() {
        TypeReference<List<FearGreedRedisDto>> typeRef = new TypeReference<List<FearGreedRedisDto>>() {};
        Optional<List<FearGreedRedisDto>> historyList = redisCacheService.get(REDIS_KEY_HISTORY, typeRef);
        
        if (historyList.isEmpty()) {
            log.warn("Redis에서 공포/탐욕 지수 히스토리 데이터를 찾을 수 없습니다 - key: {}", REDIS_KEY_HISTORY);
            return List.of();
        }
        
        return historyList.get().stream()
            .map(dto -> FearGreedResponse.builder()
                .date(dto.getDate())
                .value(dto.getValue())
                .build())
            .collect(Collectors.toList());
    }
}
