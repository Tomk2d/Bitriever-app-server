package com.bitreiver.app_server.domain.trading.controller;

import com.bitreiver.app_server.domain.trading.dto.TradingHistoryDateRangeRequest;
import com.bitreiver.app_server.domain.trading.dto.TradingHistoryResponse;
import com.bitreiver.app_server.domain.trading.service.TradingHistoryService;
import com.bitreiver.app_server.global.common.dto.PageResponse;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/trading-histories")
@RequiredArgsConstructor
@Tag(name = "Trading History", description = "매매 내역 조회 API")
public class TradingHistoryController {
    
    private final TradingHistoryService tradingHistoryService;
    
    @Operation(summary = "매매 내역 조회", description = "현재 로그인한 사용자의 매매 내역을 페이지네이션으로 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ApiResponse<PageResponse<TradingHistoryResponse>> getUserTradingHistories(
        Authentication authentication,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        PageResponse<TradingHistoryResponse> response = tradingHistoryService.getUserTradingHistories(userId, page, size);
        
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "기간별 매매 내역 조회", description = "현재 로그인한 사용자의 매매 내역을 시작일과 종료일 기준으로 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 날짜 형식 또는 날짜 범위"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping("/range")
    public ApiResponse<List<TradingHistoryResponse>> getUserTradingHistoriesByDateRange(
        Authentication authentication,
        @Valid @RequestBody TradingHistoryDateRangeRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        List<TradingHistoryResponse> response = tradingHistoryService.getUserTradingHistoriesByDateRange(
            userId, request.getStartDate(), request.getEndDate()
        );
        return ApiResponse.success(response);
    }
}

