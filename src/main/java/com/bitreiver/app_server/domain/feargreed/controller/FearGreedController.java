package com.bitreiver.app_server.domain.feargreed.controller;

import com.bitreiver.app_server.domain.feargreed.dto.FearGreedResponse;
import com.bitreiver.app_server.domain.feargreed.service.FearGreedService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/fear-greed")
@RequiredArgsConstructor
@Tag(name = "Fear & Greed Index", description = "공포/탐욕 지수 API")
public class FearGreedController {
    
    private final FearGreedService fearGreedService;

    @Operation(
        summary = "특정 날짜의 공포/탐욕 지수 조회",
        description = "데이터베이스에서 특정 날짜의 공포/탐욕 지수를 조회합니다. 날짜 형식: yyyy-MM-dd"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "조회 성공"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "해당 날짜의 데이터가 없음"
        )
    })
    @GetMapping("/{date}")
    public ApiResponse<FearGreedResponse> getByDate(
        @Parameter(
            name = "date",
            description = "조회할 날짜 (yyyy-MM-dd 형식)",
            example = "2025-12-08",
            required = true,
            in = ParameterIn.PATH
        )
        @PathVariable("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        FearGreedResponse response = fearGreedService.getByDate(date);
        return ApiResponse.success(response, "조회 성공");
    }
}

