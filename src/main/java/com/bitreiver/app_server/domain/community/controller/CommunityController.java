package com.bitreiver.app_server.domain.community.controller;

import com.bitreiver.app_server.domain.community.dto.*;
import com.bitreiver.app_server.domain.community.enums.Category;
import com.bitreiver.app_server.domain.community.enums.ReactionType;
import com.bitreiver.app_server.domain.community.service.CommunityService;
import com.bitreiver.app_server.domain.community.service.CommunityServiceImpl;
import com.bitreiver.app_server.domain.community.service.CommunityImageService;
import com.bitreiver.app_server.domain.community.service.CommunityReactionService;
import com.bitreiver.app_server.global.common.dto.PageResponse;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/communities")
@RequiredArgsConstructor
@Tag(name = "Community", description = "커뮤니티 게시글 관리 API")
public class CommunityController {
    
    private final CommunityService communityService;
    private final CommunityImageService communityImageService;
    private final CommunityReactionService communityReactionService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Operation(summary = "게시글 작성", description = "새로운 커뮤니티 게시글을 작성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "작성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ApiResponse<CommunityResponse> createCommunity(
        Authentication authentication,
        @Valid @RequestBody CommunityRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        
        // 이미지 개수 검증
        if (request.getContent() != null) {
            List<String> imagePaths = communityService.extractAllImagePaths(request.getContent());
            if (imagePaths.size() > 5) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 5개까지 추가할 수 있습니다.");
            }
        }
        
        CommunityResponse response = communityService.createCommunity(userId, request);
        return ApiResponse.success(response, "게시글이 작성되었습니다.");
    }
    
    @Operation(summary = "게시글 조회", description = "게시글 ID로 게시글을 조회합니다. (인증 불필요)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    })
    @GetMapping("/{id}")
    public ApiResponse<CommunityResponse> getCommunityById(
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("id") Integer id,
        Authentication authentication
    ) {
        UUID userId = authentication != null ? UUID.fromString(authentication.getName()) : null;
        CommunityResponse response = communityService.getCommunityById(id, userId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "게시글 목록 조회", description = "카테고리별 게시글 목록을 조회합니다. (인증 불필요)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ApiResponse<PageResponse<CommunityListResponse>> getCommunities(
        @Parameter(description = "카테고리", example = "FREE")
        @RequestParam(value = "category", required = false) String category,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(value = "page", defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(value = "size", defaultValue = "20") int size,
        Authentication authentication
    ) {
        UUID userId = authentication != null ? UUID.fromString(authentication.getName()) : null;
        if (category != null) {
            Category categoryEnum = Category.fromCode(category);
            PageResponse<CommunityListResponse> response;
            if (communityService instanceof CommunityServiceImpl) {
                response = ((CommunityServiceImpl) communityService).getCommunitiesByCategory(categoryEnum, page, size, userId);
            } else {
                response = communityService.getCommunitiesByCategory(categoryEnum, page, size);
            }
            return ApiResponse.success(response);
        } else {
            PageResponse<CommunityListResponse> response;
            if (communityService instanceof CommunityServiceImpl) {
                response = ((CommunityServiceImpl) communityService).getAllCommunities(page, size, userId);
            } else {
                response = communityService.getAllCommunities(page, size);
            }
            return ApiResponse.success(response);
        }
    }
    
    @Operation(summary = "게시글 검색", description = "해시태그로 게시글을 검색합니다. (인증 불필요)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "검색 성공")
    })
    @GetMapping("/search")
    public ApiResponse<PageResponse<CommunityListResponse>> searchCommunities(
        @Valid @ModelAttribute CommunitySearchRequest request,
        Authentication authentication
    ) {
        UUID userId = authentication != null ? UUID.fromString(authentication.getName()) : null;
        PageResponse<CommunityListResponse> response = communityService.searchCommunities(request, userId);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}")
    public ApiResponse<CommunityResponse> updateCommunity(
        Authentication authentication,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("id") Integer id,
        @Valid @RequestBody CommunityRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        CommunityResponse response = communityService.updateCommunity(userId, id, request);
        return ApiResponse.success(response, "게시글이 수정되었습니다.");
    }
    
    @Operation(summary = "게시글 수정 (이미지 관리 포함)", description = "게시글을 수정하고 삭제된 이미지를 MinIO에서 제거합니다. 이미지는 클라이언트에서 이미 업로드되어 있어야 합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}/update-content")
    public ApiResponse<CommunityResponse> updateCommunityWithImageManagement(
        Authentication authentication,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("id") Integer id,
        @Valid @RequestBody CommunityRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        CommunityResponse response = communityService.updateCommunityWithImageManagement(userId, id, request);
        return ApiResponse.success(response, "게시글이 수정되었습니다.");
    }
    
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCommunity(
        Authentication authentication,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("id") Integer id
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        communityService.deleteCommunity(userId, id);
        return ApiResponse.success(null, "게시글이 삭제되었습니다.");
    }
    
    @Operation(summary = "내 게시글 목록 조회", description = "현재 로그인한 사용자의 게시글 목록을 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @SecurityRequirement(name = "JWT")
    @GetMapping("/my")
    public ApiResponse<PageResponse<CommunityListResponse>> getMyCommunities(
        Authentication authentication,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(value = "page", defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        PageResponse<CommunityListResponse> response = communityService.getMyCommunities(userId, page, size);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "게시글 이미지 업로드", description = "게시글에 이미지를 업로드합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "업로드 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<CommunityResponse> uploadImage(
        Authentication authentication,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("id") Integer id,
        @Parameter(
            description = "업로드할 이미지 파일 (JPEG, PNG, GIF, WEBP, 최대 5MB)",
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        @RequestParam("file") MultipartFile file
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        
        // 게시글 조회 및 권한 확인
        CommunityResponse community = communityService.getCommunityById(id, userId);
        
        // 현재 이미지 개수 확인
        List<String> currentImagePaths = communityService.extractAllImagePaths(community.getContent());
        if (currentImagePaths.size() >= 5) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 5개까지 추가할 수 있습니다.");
        }
        
        String imagePath = null;
        try {
            // 1. MinIO에 이미지 업로드
            imagePath = communityImageService.uploadImage(id, file);
            
            // 2. content JSONB에 image 블록 추가
            String updatedContent = addImageBlockToContent(community.getContent(), imagePath);
            
            // 3. 게시글 업데이트 (DB 트랜잭션)
            CommunityRequest updateRequest = new CommunityRequest();
            updateRequest.setCategory(community.getCategory());
            updateRequest.setTitle(community.getTitle());
            updateRequest.setContent(updatedContent);
            updateRequest.setHashtags(community.getHashtags());
            
            CommunityResponse updatedCommunity = communityService.updateCommunity(userId, id, updateRequest);
            
            return ApiResponse.success(updatedCommunity, "이미지가 업로드되었습니다.");
            
        } catch (Exception e) {
            // DB 업데이트 실패 시 MinIO에서 이미지 삭제 (보상 트랜잭션)
            if (imagePath != null) {
                try {
                    communityImageService.deleteImage(id, imagePath);
                    log.warn("DB 업데이트 실패로 MinIO에서 이미지 삭제 (보상 트랜잭션): imagePath={}", imagePath);
                } catch (Exception cleanupException) {
                    log.error("보상 트랜잭션 실패: MinIO 이미지 삭제 실패 - imagePath={}", imagePath, cleanupException);
                }
            }
            throw e;
        }
    }
    
    @Operation(summary = "게시글 이미지 조회", description = "게시글의 이미지를 파일명으로 조회합니다. (인증 불필요)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 또는 이미지를 찾을 수 없습니다.")
    })
    @GetMapping("/{id}/images/{filename}")
    public ResponseEntity<Resource> getImage(
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("id") Integer id,
        @Parameter(description = "이미지 파일명 (예: 1_20250113150000.jpg)", required = true)
        @PathVariable("filename") String filename
    ) {
        // content에서 해당 파일명이 존재하는지 확인 (보안 검증)
        CommunityResponse community = communityService.getCommunityById(id, null);
        String imagePath = String.format("@communityImage/%d/%s", id, filename);
        if (!isImagePathInContent(community.getContent(), imagePath)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        
        // MinIO에서 이미지 다운로드
        Resource resource = communityImageService.downloadImage(id, imagePath);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(resource);
    }
    
    @Operation(summary = "게시글 이미지 삭제", description = "게시글의 이미지를 파일명으로 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 또는 이미지를 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}/images/{filename}")
    public ApiResponse<CommunityResponse> deleteImage(
        Authentication authentication,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("id") Integer id,
        @Parameter(description = "이미지 파일명 (예: 1_20250113150000.jpg)", required = true)
        @PathVariable("filename") String filename
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        
        // 게시글 조회 및 권한 확인
        CommunityResponse community = communityService.getCommunityById(id, userId);
        
        // content에서 filename에 해당하는 이미지 경로 추출
        String imagePath = String.format("@communityImage/%d/%s", id, filename);
        
        // content에 해당 이미지가 존재하는지 확인
        if (!isImagePathInContent(community.getContent(), imagePath)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        
        try {
            // 1. DB에서 content 업데이트 (먼저 수행)
            String updatedContent = removeImageBlockFromContentByPath(community.getContent(), imagePath);
            
            CommunityRequest updateRequest = new CommunityRequest();
            updateRequest.setCategory(community.getCategory());
            updateRequest.setTitle(community.getTitle());
            updateRequest.setContent(updatedContent);
            updateRequest.setHashtags(community.getHashtags());
            
            CommunityResponse updatedCommunity = communityService.updateCommunity(userId, id, updateRequest);
            
            // 2. MinIO에서 이미지 삭제
            try {
                communityImageService.deleteImage(id, imagePath);
            } catch (Exception e) {
                log.error("MinIO 삭제 실패: imagePath={}", imagePath, e);
                throw new CustomException(ErrorCode.INTERNAL_ERROR, "이미지 삭제에 실패했습니다: " + e.getMessage());
            }
            
            return ApiResponse.success(updatedCommunity, "이미지가 삭제되었습니다.");
            
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("DB 업데이트 실패: imagePath={}", imagePath, e);
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "이미지 삭제에 실패했습니다: " + e.getMessage());
        }
    }
    
    @Operation(summary = "좋아요/싫어요 추가/변경", description = "게시글에 좋아요 또는 싫어요를 추가하거나 변경합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping("/{id}/reactions")
    public ApiResponse<Void> addReaction(
        Authentication authentication,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("id") Integer id,
        @Valid @RequestBody CommunityReactionRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        ReactionType reactionType = ReactionType.fromCode(request.getReactionType());
        communityReactionService.addReaction(userId, id, reactionType);
        return ApiResponse.success(null, "반응이 추가되었습니다.");
    }
    
    @Operation(summary = "좋아요/싫어요 삭제", description = "게시글의 좋아요 또는 싫어요를 삭제합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "반응을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}/reactions")
    public ApiResponse<Void> removeReaction(
        Authentication authentication,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("id") Integer id
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        communityReactionService.removeReaction(userId, id);
        return ApiResponse.success(null, "반응이 삭제되었습니다.");
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
