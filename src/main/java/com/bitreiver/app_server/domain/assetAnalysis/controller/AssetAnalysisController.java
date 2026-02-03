package com.bitreiver.app_server.domain.assetAnalysis.controller;

import com.bitreiver.app_server.domain.assetAnalysis.dto.AssetAnalysisResponse;
import com.bitreiver.app_server.domain.assetAnalysis.service.AssetAnalysisService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/asset-analysis")
@RequiredArgsConstructor
@Tag(name = "Asset Analysis", description = "자산 분석 API")
public class AssetAnalysisController {
    
    private final AssetAnalysisService assetAnalysisService;
    
    @Operation(
        summary = "자산 분석 조회",
        description = "현재 로그인한 사용자의 종합 자산 분석 데이터를 조회합니다. " +
                      "총 자산 가치, 수익률 분포, 코인별 보유 현황, 거래 패턴, 심리 분석 등을 포함합니다."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ApiResponse<AssetAnalysisResponse> getAssetAnalysis(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("자산 분석 조회 요청: userId={}", userId);
        
        AssetAnalysisResponse response = assetAnalysisService.getAssetAnalysis(userId);
        
        return ApiResponse.success(response);
    }
}
