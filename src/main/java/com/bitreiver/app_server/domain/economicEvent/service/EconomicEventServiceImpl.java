package com.bitreiver.app_server.domain.economicEvent.service;

import com.bitreiver.app_server.domain.economicEvent.dto.EconomicEventResponse;
import com.bitreiver.app_server.domain.economicEvent.entity.EconomicEvent;
import com.bitreiver.app_server.domain.economicEvent.repository.EconomicEventRepository;
import com.bitreiver.app_server.domain.economicEvent.dto.EconomicEventRedisDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bitreiver.app_server.global.cache.RedisCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EconomicEventServiceImpl implements EconomicEventService {
    private final EconomicEventRepository economicEventRepository;
    private final RedisCacheService redisCacheService;

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final String REDIS_KEY_PREFIX = "economic-events:upcoming:";
    private static final long TTL_SECONDS = 86400; // 1일

    @Override
    @Transactional(readOnly = true)
    public List<EconomicEventResponse> getEventsByYearMonth(String yearMonth) {        
        // yearMonth 파싱 (예: "2026-01")
        YearMonth yearMonthObj = YearMonth.parse(yearMonth, YEAR_MONTH_FORMATTER);
        LocalDate startDate = yearMonthObj.atDay(1);
        LocalDate endDate = yearMonthObj.atEndOfMonth();
        
        List<EconomicEvent> events = economicEventRepository.findByYearMonth(startDate, endDate);
                
        return events.stream()
            .map(EconomicEventResponse::from)
            .collect(Collectors.toList());
    }

    @Override
    public List<EconomicEventResponse> getUpcomingEvents(int limit) {
        try {
            String redisKey = REDIS_KEY_PREFIX + "top" + limit;
            TypeReference<List<EconomicEventRedisDto>> typeRef = new TypeReference<List<EconomicEventRedisDto>>() {};
            
            List<EconomicEventRedisDto> dtoList = redisCacheService.get(redisKey, typeRef)
                .orElse(Collections.emptyList());

            if (dtoList != null && !dtoList.isEmpty()) {
                return dtoList.stream()
                    .map(EconomicEventResponse::from)
                    .collect(Collectors.toList());
            }
            
            log.info("Redis 캐시 미스 - DB에서 조회 후 캐싱 - key: {}, limit: {}", redisKey, limit);
            
            LocalDate today = LocalDate.now();
            List<EconomicEvent> events = economicEventRepository.findUpcomingEvents(today, limit);
            
            if (events.isEmpty()) {
                log.warn("DB에서도 데이터를 찾을 수 없습니다 - limit: {}", limit);
                return Collections.emptyList();
            }
            
            List<EconomicEventRedisDto> redisDtoList = events.stream()
                .map(EconomicEventRedisDto::from)
                .collect(Collectors.toList());
            
            redisCacheService.set(redisKey, redisDtoList, TTL_SECONDS);
            log.info("DB 조회 후 Redis 캐시 저장 완료 - key: {}, 개수: {}", redisKey, redisDtoList.size());
            
            return events.stream()
                .map(EconomicEventResponse::from)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("다가오는 경제 지표 이벤트 조회 실패: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
