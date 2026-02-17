package com.bitreiver.app_server.domain.tradeEvaluation.controller;

import com.bitreiver.app_server.domain.tradeEvaluation.dto.TradeEvaluationRequestDto;
import com.bitreiver.app_server.domain.tradeEvaluation.dto.TradeEvaluationStatusResponse;
import com.bitreiver.app_server.domain.tradeEvaluation.enums.TradeEvaluationJobStatus;
import com.bitreiver.app_server.domain.tradeEvaluation.service.TradeEvaluationRequestService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/trade-evaluation")
@RequiredArgsConstructor
@Tag(name = "Trade Evaluation", description = "매매 분석 요청/조회 API")
public class TradeEvaluationController {

    private final TradeEvaluationRequestService tradeEvaluationRequestService;

    @Operation(summary = "매매 분석 요청 또는 결과 조회", description = "JWT 검증 후 상태 확인. 완료 시 결과 반환(200), 진행 중 시 202, 미시작/재시도 시 분석 진행 후 결과 또는 202 반환.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "분석 완료, 결과 반환"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "분석 진행 중"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<TradeEvaluationStatusResponse>> requestOrGetResult(
        Authentication authentication,
        @Valid @RequestBody TradeEvaluationRequestDto request
    ) {
        UUID userId = (UUID) authentication.getPrincipal();
        TradeEvaluationStatusResponse response = tradeEvaluationRequestService.requestOrGetResult(userId, request);
        if (response.getStatus() == TradeEvaluationJobStatus.COMPLETED && response.getResult() != null) {
            return ResponseEntity
                .ok()
                .body(ApiResponse.success(response, "매매 분석 결과입니다."));
        }
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(response, "분석 진행 중입니다."));
    }
}
