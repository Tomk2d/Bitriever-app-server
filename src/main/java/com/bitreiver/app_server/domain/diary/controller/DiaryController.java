package com.bitreiver.app_server.domain.diary.controller;

import com.bitreiver.app_server.domain.diary.dto.DiaryRequest;
import com.bitreiver.app_server.domain.diary.dto.DiaryResponse;
import com.bitreiver.app_server.domain.diary.dto.TradingHistoryWithDiaryResponse;
import com.bitreiver.app_server.domain.diary.service.DiaryService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
@Tag(name = "Diary", description = "매매 일지 관리 API")
public class DiaryController {
    
    private final DiaryService diaryService;
    
    @Operation(summary = "매매 일지 생성", description = "매매 내역에 대한 일지를 생성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "매매 내역을 찾을 수 없습니다."),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 일지입니다.")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ApiResponse<DiaryResponse> createDiary(
        Authentication authentication,
        @Valid @RequestBody DiaryRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        DiaryResponse response = diaryService.createDiary(userId, request);
        return ApiResponse.success(response, "매매 일지가 생성되었습니다.");
    }
    
    @Operation(summary = "매매 일지 조회", description = "일지 ID로 매매 일지를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일지를 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{id}")
    public ApiResponse<DiaryResponse> getDiaryById(
        Authentication authentication,
        @PathVariable Integer id
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        DiaryResponse response = diaryService.getDiaryById(userId, id);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "매매 내역으로 일지 조회", description = "매매 내역 ID로 일지를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일지 또는 매매 내역을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping("/trading-history/{tradingHistoryId}")
    public ApiResponse<DiaryResponse> getDiaryByTradingHistoryId(
        Authentication authentication,
        @PathVariable("tradingHistoryId") Integer tradingHistoryId
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        DiaryResponse response = diaryService.getDiaryByTradingHistoryId(userId, tradingHistoryId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "사용자별 일지 목록 조회", description = "현재 로그인한 사용자의 모든 일지 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping("/user")
    public ApiResponse<List<DiaryResponse>> getUserDiaries(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        List<DiaryResponse> response = diaryService.getUserDiaries(userId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "매매 일지 수정", description = "등록된 매매 일지를 수정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일지를 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}")
    public ApiResponse<DiaryResponse> updateDiary(
        Authentication authentication,
        @PathVariable Integer id,
        @Valid @RequestBody DiaryRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        DiaryResponse response = diaryService.updateDiary(userId, id, request);
        return ApiResponse.success(response, "매매 일지가 수정되었습니다.");
    }
    
    @Operation(summary = "매매 일지 삭제", description = "등록된 매매 일지를 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일지를 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDiary(
        Authentication authentication,
        @PathVariable Integer id
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        diaryService.deleteDiary(userId, id);
        return ApiResponse.success(null, "매매 일지가 삭제되었습니다.");
    }
    
    @Operation(summary = "기간별 거래 내역 및 매매 일지 조회", 
               description = "현재 로그인한 사용자의 거래 내역과 매매 일지를 시작일과 종료일 기준으로 조회합니다. " +
                           "거래 내역은 일지가 없어도 반환되며, 일지가 있는 경우 함께 반환됩니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 날짜 형식"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping("/range")
    public ApiResponse<List<TradingHistoryWithDiaryResponse>> getTradingHistoriesWithDiariesByDateRange(
        Authentication authentication,
        @Parameter(description = "시작 날짜 및 시간 (ISO 8601 형식)", example = "2025-07-01T00:00:00", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @Parameter(description = "종료 날짜 및 시간 (ISO 8601 형식, 미포함)", example = "2025-08-01T00:00:00", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        List<TradingHistoryWithDiaryResponse> response = diaryService.getTradingHistoriesWithDiariesByDateRange(
            userId, startDate, endDate
        );
        return ApiResponse.success(response);
    }
}

