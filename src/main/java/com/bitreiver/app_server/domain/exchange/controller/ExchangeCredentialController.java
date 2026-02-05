package com.bitreiver.app_server.domain.exchange.controller;

import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialRequest;
import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialResponse;
import com.bitreiver.app_server.domain.exchange.service.ExchangeCredentialService;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
@RequestMapping("/api/exchange-credentials")
@RequiredArgsConstructor
@Tag(name = "Exchange Credential", description = "거래소 자격증명 관리 API (사용자당 여러 거래소 연동 가능)")
public class ExchangeCredentialController {

    private final ExchangeCredentialService exchangeCredentialService;

    @Operation(summary = "거래소 자격증명 등록/수정", description = "해당 거래소 자격증명을 등록하거나, 이미 있으면 수정합니다. 사용자당 거래소별로 여러 개 등록 가능합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "등록/수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ApiResponse<ExchangeCredentialResponse> saveCredential(
        Authentication authentication,
        @Valid @RequestBody ExchangeCredentialRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("거래소 자격증명 저장 요청 - userId: {}, exchangeProvider: {}", userId, request.getExchangeProvider());
        ExchangeCredentialResponse response = exchangeCredentialService.saveCredential(userId, request);
        log.info("거래소 자격증명 저장 완료 - userId: {}", userId);
        return ApiResponse.success(response, "거래소 자격증명이 저장되었습니다.");
    }

    @Operation(summary = "거래소 자격증명 조회", description = "exchangeProvider가 있으면 해당 거래소 1건, 없으면 연동된 모든 거래소 목록을 반환합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 거래소 자격증명을 찾을 수 없습니다 (단건 조회 시)")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ApiResponse<?> getCredential(
        Authentication authentication,
        @Parameter(description = "거래소 코드 (1:업비트, 2:빗썸, 3:코인원 등). 생략 시 전체 목록", in = ParameterIn.QUERY)
        @RequestParam(value = "exchangeProvider", required = false) Short exchangeProvider
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("거래소 자격증명 조회 - userId: {}, exchangeProvider: {}", userId, exchangeProvider);

        if (exchangeProvider != null) {
            return exchangeCredentialService.getCredential(userId, exchangeProvider)
                .map(ApiResponse::success)
                .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_CREDENTIAL_NOT_FOUND, "해당 거래소 자격증명을 찾을 수 없습니다."));
        }
        List<ExchangeCredentialResponse> list = exchangeCredentialService.getAllCredentials(userId);
        return ApiResponse.success(list);
    }

    @Operation(summary = "거래소 자격증명 수정", description = "이미 등록된 해당 거래소의 API 키를 수정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 거래소 자격증명을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @PutMapping
    public ApiResponse<ExchangeCredentialResponse> updateCredential(
        Authentication authentication,
        @Valid @RequestBody ExchangeCredentialRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("거래소 자격증명 수정 요청 - userId: {}, exchangeProvider: {}", userId, request.getExchangeProvider());
        ExchangeCredentialResponse response = exchangeCredentialService.updateCredential(userId, request);
        log.info("거래소 자격증명 수정 완료 - userId: {}", userId);
        return ApiResponse.success(response, "거래소 자격증명이 수정되었습니다.");
    }

    @Operation(summary = "거래소 자격증명 삭제", description = "해당 거래소의 자격증명만 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 거래소 자격증명을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @DeleteMapping
    public ApiResponse<Void> deleteCredential(
        Authentication authentication,
        @Parameter(description = "거래소 코드 (1:업비트, 2:빗썸, 3:코인원 등)", required = true, in = ParameterIn.QUERY)
        @RequestParam(value = "exchangeProvider") Short exchangeProvider
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        log.info("거래소 자격증명 삭제 요청 - userId: {}, exchangeProvider: {}", userId, exchangeProvider);
        exchangeCredentialService.deleteCredential(userId, exchangeProvider);
        log.info("거래소 자격증명 삭제 완료 - userId: {}", userId);
        return ApiResponse.success(null, "거래소 자격증명이 삭제되었습니다.");
    }
}
