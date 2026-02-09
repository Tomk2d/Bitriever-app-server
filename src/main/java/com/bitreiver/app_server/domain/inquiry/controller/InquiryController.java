package com.bitreiver.app_server.domain.inquiry.controller;

import com.bitreiver.app_server.domain.inquiry.dto.InquiryCreateRequest;
import com.bitreiver.app_server.domain.inquiry.dto.InquiryResponse;
import com.bitreiver.app_server.domain.inquiry.service.InquiryService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@Tag(name = "Inquiry", description = "문의하기 API")
public class InquiryController {

    private final InquiryService inquiryService;

    @Operation(summary = "문의 제출", description = "로그인한 사용자가 문의 내용을 제출합니다. JWT 인증 필요.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "문의 생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력 (문의 내용 누락 등)"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ResponseEntity<ApiResponse<InquiryResponse>> createInquiry(
            Authentication authentication,
            @Valid @RequestBody InquiryCreateRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        InquiryResponse response = inquiryService.createInquiry(userId, request.getContent());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "문의가 접수되었습니다."));
    }

    @Operation(summary = "내 문의 목록 조회", description = "로그인한 사용자의 문의 목록을 최신순으로 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ApiResponse<List<InquiryResponse>> getMyInquiries(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<InquiryResponse> list = inquiryService.getMyInquiries(userId);
        return ApiResponse.success(list);
    }

    @Operation(summary = "문의 단건 조회", description = "문의 ID로 본인이 제출한 문의를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "문의를 찾을 수 없음")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{id}")
    public ApiResponse<InquiryResponse> getInquiry(
            Authentication authentication,
            @PathVariable Long id
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        InquiryResponse response = inquiryService.getInquiry(userId, id);
        return ApiResponse.success(response);
    }
}
