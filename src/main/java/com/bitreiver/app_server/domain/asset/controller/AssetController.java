package com.bitreiver.app_server.domain.asset.controller;

import com.bitreiver.app_server.domain.asset.dto.AssetResponse;
import com.bitreiver.app_server.domain.asset.service.AssetService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@Tag(name = "Asset", description = "자산 조회 API")
public class AssetController {
    
    private final AssetService assetService;
    
    @Operation(summary = "자산 조회", description = "현재 로그인한 사용자의 모든 자산을 조회합니다.\n거래소 코드를 전달하지 않으면 모든 거래소의 자산을 조회합니다.\n거래소 코드를 전달하면 해당 거래소의 자산을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ApiResponse<List<AssetResponse>> getUserAssets(
        Authentication authentication,
        @Parameter(name = "exchangeCode", description = "거래소 코드 (선택, 1:UPBIT, 2:BITHUMB, 3:COINONE, 11:BINANCE, 12:BYBIT, 13:COINBASE, 14:OKX)", example = "1", required = false)
        @RequestParam(value = "exchangeCode", required = false) Short exchangeCode
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        
        List<AssetResponse> response;
        if (exchangeCode != null) {
            response = assetService.getUserAssetsByExchange(userId, exchangeCode);
        } else {
            response = assetService.getUserAssets(userId);
        }
        
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "자산 동기화", description = "현재 로그인한 사용자의 자산을 외부 서비스에서 동기화합니다. 비동기로 처리되며 즉시 응답을 반환합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "동기화 요청 수락"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping("/sync")
    public ApiResponse<Void> syncAssets(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        assetService.syncAssets(userId);
        return ApiResponse.success(null, "자산 동기화가 시작되었습니다.");
    }
}

