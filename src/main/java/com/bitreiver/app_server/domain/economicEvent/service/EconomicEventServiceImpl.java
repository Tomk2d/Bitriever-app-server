package com.bitreiver.app_server.domain.economicEvent.service;

import com.bitreiver.app_server.domain.economicEvent.dto.EconomicEventResponse;
import com.bitreiver.app_server.domain.economicEvent.entity.EconomicEvent;
import com.bitreiver.app_server.domain.economicEvent.repository.EconomicEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    
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
}
