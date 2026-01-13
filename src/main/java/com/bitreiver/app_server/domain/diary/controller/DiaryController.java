package com.bitreiver.app_server.domain.diary.controller;

import com.bitreiver.app_server.domain.diary.dto.DiaryRequest;
import com.bitreiver.app_server.domain.diary.dto.DiaryResponse;
import com.bitreiver.app_server.domain.diary.dto.TradingHistoryWithDiaryResponse;
import com.bitreiver.app_server.domain.diary.service.DiaryService;
import com.bitreiver.app_server.domain.diary.service.DiaryImageService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
@Tag(name = "Diary", description = "매매 일지 관리 API")
public class DiaryController {
    
    private final DiaryService diaryService;
    private final DiaryImageService diaryImageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
        @PathVariable("id") Integer id
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
        @PathVariable("id") Integer id,
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
        @PathVariable("id") Integer id
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
    
    @Operation(summary = "매매 일지 이미지 업로드", description = "매매 일지에 이미지를 업로드합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업로드 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일지를 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<DiaryResponse> uploadImage(
        Authentication authentication,
        @Parameter(description = "일지 ID", required = true)
        @PathVariable("id") Integer id,
        @Parameter(
            description = "업로드할 이미지 파일 (JPEG, PNG, GIF, WEBP, 최대 5MB)",
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestParam("file") MultipartFile file
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        
        // 일지 조회 및 권한 확인
        DiaryResponse diary = diaryService.getDiaryById(userId, id);
        
        String imagePath = null;
        try {
            // 1. MinIO에 이미지 업로드
            imagePath = diaryImageService.uploadImage(id, file);
            
            // 2. content JSONB에 image 블록 추가
            String updatedContent = addImageBlockToContent(diary.getContent(), imagePath);
            
            // 3. 일지 업데이트 (DB 트랜잭션)
            DiaryRequest updateRequest = new DiaryRequest();
            updateRequest.setTradingHistoryId(diary.getTradingHistoryId());
            updateRequest.setContent(updatedContent);
            updateRequest.setTags(diary.getTags());
            updateRequest.setTradingMind(diary.getTradingMind());
            
            DiaryResponse updatedDiary = diaryService.updateDiary(userId, id, updateRequest);
            
            return ApiResponse.success(updatedDiary, "이미지가 업로드되었습니다.");
            
        } catch (Exception e) {
            // DB 업데이트 실패 시 MinIO에서 이미지 삭제 (보상 트랜잭션)
            if (imagePath != null) {
                try {
                    diaryImageService.deleteImage(id, imagePath);
                    log.warn("DB 업데이트 실패로 MinIO에서 이미지 삭제 (보상 트랜잭션): imagePath={}", imagePath);
                } catch (Exception cleanupException) {
                    log.error("보상 트랜잭션 실패: MinIO 이미지 삭제 실패 - imagePath={}", imagePath, cleanupException);
                }
            }
            throw e; // 원래 예외를 다시 던짐
        }
    }
    
    @Operation(summary = "매매 일지 이미지 조회", description = "매매 일지의 이미지를 파일명으로 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일지 또는 이미지를 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{id}/images/{filename}")
    public ResponseEntity<Resource> getImage(
        Authentication authentication,
        @Parameter(description = "일지 ID", required = true)
        @PathVariable("id") Integer id,
        @Parameter(description = "이미지 파일명 (예: 1_20250113150000.jpg)", required = true)
        @PathVariable("filename") String filename
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        
        // 일지 조회 및 권한 확인
        DiaryResponse diary = diaryService.getDiaryById(userId, id);
        
        // content에서 해당 파일명이 존재하는지 확인 (보안 검증)
        String imagePath = String.format("@diaryImage/%d/%s", id, filename);
        if (!isImagePathInContent(diary.getContent(), imagePath)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        
        // MinIO에서 이미지 다운로드
        Resource resource = diaryImageService.downloadImage(id, imagePath);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(resource);
    }
    
    @Operation(summary = "매매 일지 이미지 삭제", description = "매매 일지의 이미지를 파일명으로 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "일지 또는 이미지를 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}/images/{filename}")
    public ApiResponse<DiaryResponse> deleteImage(
        Authentication authentication,
        @Parameter(description = "일지 ID", required = true)
        @PathVariable("id") Integer id,
        @Parameter(description = "이미지 파일명 (예: 1_20250113150000.jpg)", required = true)
        @PathVariable("filename") String filename
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        
        // 일지 조회 및 권한 확인
        DiaryResponse diary = diaryService.getDiaryById(userId, id);
        
        // content에서 filename에 해당하는 이미지 경로 추출
        String imagePath = String.format("@diaryImage/%d/%s", id, filename);
        
        // content에 해당 이미지가 존재하는지 확인
        if (!isImagePathInContent(diary.getContent(), imagePath)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        
        try {
            // 1. DB에서 content 업데이트 (먼저 수행)
            String updatedContent = removeImageBlockFromContentByPath(diary.getContent(), imagePath);
            
            DiaryRequest updateRequest = new DiaryRequest();
            updateRequest.setTradingHistoryId(diary.getTradingHistoryId());
            updateRequest.setContent(updatedContent);
            updateRequest.setTags(diary.getTags());
            updateRequest.setTradingMind(diary.getTradingMind());
            
            DiaryResponse updatedDiary = diaryService.updateDiary(userId, id, updateRequest);
            
            // 2. MinIO에서 이미지 삭제
            try {
                diaryImageService.deleteImage(id, imagePath);
            } catch (Exception e) {
                // MinIO 삭제 실패 시 DB 롤백은 @Transactional이 자동으로 처리
                log.error("MinIO 삭제 실패, DB 롤백됨: imagePath={}", imagePath, e);
                throw new CustomException(ErrorCode.INTERNAL_ERROR, "이미지 삭제에 실패했습니다: " + e.getMessage());
            }
            
            return ApiResponse.success(updatedDiary, "이미지가 삭제되었습니다.");
            
        } catch (CustomException e) {
            // CustomException은 그대로 던짐
            throw e;
        } catch (Exception e) {
            // DB 업데이트 실패 시는 MinIO에 아무것도 하지 않음 (이미 존재)
            log.error("DB 업데이트 실패: imagePath={}", imagePath, e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "이미지 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    // Helper 메서드들
    private String addImageBlockToContent(String content, String imagePath) {
        try {
            Map<String, Object> contentMap;
            if (content == null || content.trim().isEmpty()) {
                contentMap = new HashMap<>();
                contentMap.put("blocks", new ArrayList<>());
            } else {
                contentMap = objectMapper.readValue(content, Map.class);
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> blocks = (List<Map<String, Object>>) contentMap.get("blocks");
            if (blocks == null) {
                blocks = new ArrayList<>();
                contentMap.put("blocks", blocks);
            }
            
            Map<String, Object> imageBlock = new HashMap<>();
            imageBlock.put("type", "image");
            imageBlock.put("path", imagePath);
            blocks.add(imageBlock);
            
            return objectMapper.writeValueAsString(contentMap);
        } catch (Exception e) {
            log.error("Content에 이미지 블록 추가 실패", e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "이미지 추가에 실패했습니다.");
        }
    }
    
    private boolean isImagePathInContent(String content, String imagePath) {
        try {
            if (content == null || content.trim().isEmpty() || imagePath == null) {
                return false;
            }
            
            Map<String, Object> contentMap = objectMapper.readValue(content, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> blocks = (List<Map<String, Object>>) contentMap.get("blocks");
            
            if (blocks == null) {
                return false;
            }
            
            for (Map<String, Object> block : blocks) {
                if ("image".equals(block.get("type"))) {
                    String path = (String) block.get("path");
                    if (imagePath.equals(path)) {
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("Content에서 이미지 경로 확인 실패", e);
            return false;
        }
    }
    
    private String extractImagePathFromContent(String content, int imageIndex) {
        try {
            if (content == null || content.trim().isEmpty()) {
                return null;
            }
            
            Map<String, Object> contentMap = objectMapper.readValue(content, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> blocks = (List<Map<String, Object>>) contentMap.get("blocks");
            
            if (blocks == null) {
                return null;
            }
            
            int imageCount = 0;
            for (Map<String, Object> block : blocks) {
                if ("image".equals(block.get("type"))) {
                    if (imageCount == imageIndex) {
                        return (String) block.get("path");
                    }
                    imageCount++;
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Content에서 이미지 경로 추출 실패", e);
            return null;
        }
    }
    
    private String removeImageBlockFromContent(String content, int imageIndex) {
        try {
            if (content == null || content.trim().isEmpty()) {
                return content;
            }
            
            Map<String, Object> contentMap = objectMapper.readValue(content, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> blocks = (List<Map<String, Object>>) contentMap.get("blocks");
            
            if (blocks == null) {
                return content;
            }
            
            int imageCount = 0;
            Iterator<Map<String, Object>> iterator = blocks.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> block = iterator.next();
                if ("image".equals(block.get("type"))) {
                    if (imageCount == imageIndex) {
                        iterator.remove();
                        break;
                    }
                    imageCount++;
                }
            }
            
            return objectMapper.writeValueAsString(contentMap);
        } catch (Exception e) {
            log.error("Content에서 이미지 블록 제거 실패", e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "이미지 삭제에 실패했습니다.");
        }
    }
    
    private String removeImageBlockFromContentByPath(String content, String imagePath) {
        try {
            if (content == null || content.trim().isEmpty()) {
                return content;
            }
            
            Map<String, Object> contentMap = objectMapper.readValue(content, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> blocks = (List<Map<String, Object>>) contentMap.get("blocks");
            
            if (blocks == null) {
                return content;
            }
            
            Iterator<Map<String, Object>> iterator = blocks.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> block = iterator.next();
                if ("image".equals(block.get("type"))) {
                    String path = (String) block.get("path");
                    if (imagePath.equals(path)) {
                        iterator.remove();
                        break;
                    }
                }
            }
            
            return objectMapper.writeValueAsString(contentMap);
        } catch (Exception e) {
            log.error("Content에서 이미지 블록 제거 실패", e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "이미지 삭제에 실패했습니다.");
        }
    }
}

