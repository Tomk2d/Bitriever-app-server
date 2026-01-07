package com.bitreiver.app_server.domain.economicIndex.controller;

import com.bitreiver.app_server.domain.economicIndex.dto.EconomicIndexResponse;
import com.bitreiver.app_server.domain.economicIndex.enums.EconomicIndexType;
import com.bitreiver.app_server.domain.economicIndex.service.EconomicIndexService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/economic-indices")
@RequiredArgsConstructor
@Tag(name = "Economic Index", description = "경제 지표 조회 API")
public class EconomicIndexController {
    
    private final EconomicIndexService economicIndexService;
    
    @Operation(
        summary = "전체 경제 지표 조회",
        description = "특정 지표 타입의 최근 1일치 데이터를 5분 간격으로 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "데이터를 찾을 수 없습니다"
        )
    })
    @GetMapping("/{type}")
    public ApiResponse<List<EconomicIndexResponse>> getByIndexType(
        @Parameter(
            name = "type",
            description = "지표 타입 (KOSPI, KOSDAQ, NASDAQ, S_P_500, DOW_JONES, USD_KRW)",
            required = true,
            in = ParameterIn.PATH
        )
        @PathVariable("type") EconomicIndexType type
    ) {
        List<EconomicIndexResponse> data = economicIndexService.getByIndexType(type);
        return ApiResponse.success(data, "경제 지표 조회 성공");
    }
    
    @Operation(
        summary = "기간별 경제 지표 조회",
        description = "특정 지표 타입의 기간별 데이터를 5분 간격으로 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "데이터를 찾을 수 없습니다"
        )
    })
    @GetMapping("/{type}/range")
    public ApiResponse<List<EconomicIndexResponse>> getByIndexTypeAndDateRange(
        @Parameter(
            name = "type",
            description = "지표 타입",
            required = true,
            in = ParameterIn.PATH
        )
        @PathVariable("type") EconomicIndexType type,
        @Parameter(
            description = "시작 날짜 (yyyy-MM-dd)",
            example = "2024-01-01"
        )
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @Parameter(
            description = "종료 날짜 (yyyy-MM-dd)",
            example = "2024-12-31"
        )
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        List<EconomicIndexResponse> data = economicIndexService.getByIndexTypeAndDateRange(
            type, startDate, endDate
        );
        return ApiResponse.success(data, "경제 지표 기간별 조회 성공");
    }
    
    @Operation(
        summary = "특정 날짜 경제 지표 조회",
        description = "특정 지표 타입의 특정 날짜 데이터(5분 간격)를 조회합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "해당 날짜의 데이터를 찾을 수 없습니다"
        )
    })
    @GetMapping("/{type}/{date}")
    public ApiResponse<EconomicIndexResponse> getByIndexTypeAndDate(
        @Parameter(
            name = "type",
            description = "지표 타입",
            required = true,
            in = ParameterIn.PATH
        )
        @PathVariable("type") EconomicIndexType type,
        @Parameter(
            name = "date",
            description = "날짜 (yyyy-MM-dd)",
            example = "2024-01-01",
            required = true,
            in = ParameterIn.PATH
        )
        @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        EconomicIndexResponse data = economicIndexService.getByIndexTypeAndDate(type, date);
        return ApiResponse.success(data, "경제 지표 조회 성공");
    }
}