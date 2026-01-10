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
}
