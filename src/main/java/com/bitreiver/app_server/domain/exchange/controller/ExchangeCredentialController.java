package com.bitreiver.app_server.domain.exchange.controller;

import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialRequest;
import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialResponse;
import com.bitreiver.app_server.domain.exchange.service.ExchangeCredentialService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/exchange-credentials")
@RequiredArgsConstructor
@Tag(name = "Exchange Credential", description = "거래소 자격증명 관리 API")
public class ExchangeCredentialController {
    
    private final ExchangeCredentialService exchangeCredentialService;
    
    @Operation(summary = "거래소 자격증명 등록", description = "사용자의 거래소 API 키를 등록합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ApiResponse<ExchangeCredentialResponse> createCredential(
        Authentication authentication,
        @Valid @RequestBody ExchangeCredentialRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("거래소 자격증명 생성 요청 - userId: {}", userId);
        ExchangeCredentialResponse response = exchangeCredentialService.createCredential(userId, request);
        log.info("거래소 자격증명 생성 완료 - userId: {}", userId);
        return ApiResponse.success(response, "거래소 자격증명이 등록되었습니다.");
    }
    
    @Operation(summary = "거래소 자격증명 조회", description = "현재 로그인한 사용자의 거래소 자격증명을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자격증명을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ApiResponse<ExchangeCredentialResponse> getCredential(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("거래소 자격증명 조회 - userId: {}", userId);
        ExchangeCredentialResponse response = exchangeCredentialService.getCredential(userId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "거래소 자격증명 수정", description = "등록된 거래소 API 키를 수정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자격증명을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @PutMapping
    public ApiResponse<ExchangeCredentialResponse> updateCredential(
        Authentication authentication,
        @Valid @RequestBody ExchangeCredentialRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("거래소 자격증명 수정 요청 - userId: {}", userId);
        ExchangeCredentialResponse response = exchangeCredentialService.updateCredential(userId, request);
        log.info("거래소 자격증명 수정 완료 - userId: {}", userId);
        return ApiResponse.success(response, "거래소 자격증명이 수정되었습니다.");
    }
    
    @Operation(summary = "거래소 자격증명 삭제", description = "등록된 거래소 API 키를 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "자격증명을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @DeleteMapping
    public ApiResponse<Void> deleteCredential(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("거래소 자격증명 삭제 요청 - userId: {}", userId);
        exchangeCredentialService.deleteCredential(userId);
        log.info("거래소 자격증명 삭제 완료 - userId: {}", userId);
        return ApiResponse.success(null, "거래소 자격증명이 삭제되었습니다.");
    }
}

