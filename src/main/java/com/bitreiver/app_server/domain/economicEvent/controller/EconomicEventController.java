package com.bitreiver.app_server.domain.economicEvent.controller;

import com.bitreiver.app_server.domain.economicEvent.dto.EconomicEventResponse;
import com.bitreiver.app_server.domain.economicEvent.service.EconomicEventService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/economic-events")
@RequiredArgsConstructor
@Tag(name = "EconomicEvent", description = "경제 지표 캘린더 조회 API")
public class EconomicEventController {
    
    private final EconomicEventService economicEventService;
    
    @Operation(summary = "월별 경제 지표 이벤트 조회", 
               description = "특정 년월의 경제 지표 이벤트 목록을 조회합니다. (예: 2026-01)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "잘못된 년월 형식"
        )
    })
    @GetMapping("/month/{yearMonth}")
    public ApiResponse<List<EconomicEventResponse>> getEventsByYearMonth(
        @Parameter(
            name = "yearMonth", 
            description = "년월 (형식: yyyy-MM, 예: 2026-01)", 
            required = true, 
            in = ParameterIn.PATH,
            example = "2026-01"
        )
        @PathVariable("yearMonth") String yearMonth
    ) {        
        List<EconomicEventResponse> events = economicEventService.getEventsByYearMonth(yearMonth);
        return ApiResponse.success(events);
    }
    
    @Operation(summary = "다가오는 경제 지표 이벤트 조회", 
               description = "Redis에 캐싱된 다가오는 경제 지표 이벤트 목록을 조회합니다. (기본값: 5개)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "조회 성공"
        )
    })
    @GetMapping("/upcoming")
    public ApiResponse<List<EconomicEventResponse>> getUpcomingEvents(
        @Parameter(
            name = "limit", 
            description = "조회할 이벤트 개수 (기본값: 5)", 
            required = false, 
            in = ParameterIn.QUERY,
            example = "5"
        )
        @RequestParam(value = "limit", defaultValue = "5") int limit
    ) {
        List<EconomicEventResponse> events = economicEventService.getUpcomingEvents(limit);
        return ApiResponse.success(events);
    }

    @Operation(summary = "오늘 날짜 경제 지표 이벤트 개수 조회", 
            description = "오늘 날짜에 예정된 경제 지표 이벤트의 개수를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "조회 성공"
        )
    })
    @GetMapping("/today/count")
    public ApiResponse<Integer> getTodayEventCount() {
        log.info("오늘 날짜 경제 지표 이벤트 개수 조회 요청");
        int count = economicEventService.getTodayEventCount();
        return ApiResponse.success(count);
    }
}
