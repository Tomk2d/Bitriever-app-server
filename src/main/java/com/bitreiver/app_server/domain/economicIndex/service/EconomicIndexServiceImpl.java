package com.bitreiver.app_server.domain.economicIndex.service;

import com.bitreiver.app_server.domain.economicIndex.dto.EconomicIndexRedisDto;
import com.bitreiver.app_server.domain.economicIndex.dto.EconomicIndexResponse;
import com.bitreiver.app_server.domain.economicIndex.enums.EconomicIndexType;
import com.bitreiver.app_server.global.cache.RedisCacheService;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EconomicIndexServiceImpl implements EconomicIndexService {
    
    private final RedisCacheService redisCacheService;
    
    private static final String REDIS_KEY_PREFIX = "economic-index:";
    
    @Override
    public List<EconomicIndexResponse> getByIndexType(EconomicIndexType indexType) {
        String redisKey = REDIS_KEY_PREFIX + indexType.name();
        TypeReference<List<EconomicIndexRedisDto>> typeRef = new TypeReference<List<EconomicIndexRedisDto>>() {};
        
        List<EconomicIndexRedisDto> redisData = redisCacheService.get(redisKey, typeRef)
            .orElse(new ArrayList<>());
        
        if (redisData.isEmpty()) {
            log.warn("경제 지표 데이터가 없습니다 - type: {}, key: {}", indexType, redisKey);
            throw new CustomException(ErrorCode.NOT_FOUND, 
                "경제 지표 데이터를 찾을 수 없습니다: " + indexType);
        }
        
        return redisData.stream()
            .map(dto -> EconomicIndexResponse.from(dto, indexType))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<EconomicIndexResponse> getByIndexTypeAndDateRange(
        EconomicIndexType indexType,
        LocalDate startDate,
        LocalDate endDate
    ) {
        List<EconomicIndexResponse> allData = getByIndexType(indexType);
        
        return allData.stream()
            .filter(response -> {
                LocalDate date = response.getDateTime().toLocalDate();
                return (date.isEqual(startDate) || date.isAfter(startDate)) &&
                       (date.isEqual(endDate) || date.isBefore(endDate));
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public EconomicIndexResponse getByIndexTypeAndDate(
        EconomicIndexType indexType,
        LocalDate date
    ) {
        List<EconomicIndexResponse> allData = getByIndexType(indexType);
        
        return allData.stream()
            .filter(response -> response.getDateTime().toLocalDate().equals(date))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND,
                "해당 날짜의 경제 지표 데이터를 찾을 수 없습니다: " + indexType + ", " + date));
    }
}
