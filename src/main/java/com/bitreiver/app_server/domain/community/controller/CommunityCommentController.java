package com.bitreiver.app_server.domain.community.controller;

import com.bitreiver.app_server.domain.community.dto.CommunityCommentRequest;
import com.bitreiver.app_server.domain.community.dto.CommunityCommentReactionRequest;
import com.bitreiver.app_server.domain.community.dto.CommunityCommentResponse;
import com.bitreiver.app_server.domain.community.enums.ReactionType;
import com.bitreiver.app_server.domain.community.service.CommunityCommentService;
import com.bitreiver.app_server.domain.community.service.CommunityCommentReactionService;
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

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/communities/{communityId}/comments")
@RequiredArgsConstructor
@Tag(name = "Community Comment", description = "커뮤니티 댓글 관리 API")
public class CommunityCommentController {
    
    private final CommunityCommentService communityCommentService;
    private final CommunityCommentReactionService communityCommentReactionService;
    
    @Operation(summary = "댓글 작성", description = "게시글에 댓글을 작성합니다. parentId를 지정하면 대댓글을 작성합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "작성 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 또는 부모 댓글을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping
    public ApiResponse<CommunityCommentResponse> createComment(
        Authentication authentication,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("communityId") Integer communityId,
        @Valid @RequestBody CommunityCommentRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        CommunityCommentResponse response = communityCommentService.createComment(userId, communityId, request);
        return ApiResponse.success(response, "댓글이 작성되었습니다.");
    }
    
    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 계층 구조로 조회합니다. (인증 불필요)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없습니다.")
    })
    @GetMapping
    public ApiResponse<PageResponse<CommunityCommentResponse>> getComments(
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("communityId") Integer communityId,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(value = "page", defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(value = "size", defaultValue = "20") int size,
        Authentication authentication
    ) {
        UUID userId = authentication != null ? UUID.fromString(authentication.getName()) : null;
        PageResponse<CommunityCommentResponse> response = communityCommentService.getCommentsByCommunityId(communityId, userId, page, size);
        return ApiResponse.success(response);
    }
    
    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}")
    public ApiResponse<CommunityCommentResponse> updateComment(
        Authentication authentication,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("communityId") Integer communityId,
        @Parameter(description = "댓글 ID", required = true)
        @PathVariable("id") Long id,
        @Valid @RequestBody CommunityCommentRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        CommunityCommentResponse response = communityCommentService.updateComment(userId, id, request);
        return ApiResponse.success(response, "댓글이 수정되었습니다.");
    }
    
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. 대댓글이 있으면 soft delete, 없으면 hard delete됩니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "삭제 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteComment(
        Authentication authentication,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("communityId") Integer communityId,
        @Parameter(description = "댓글 ID", required = true)
        @PathVariable("id") Long id
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        communityCommentService.deleteComment(userId, id);
        return ApiResponse.success(null, "댓글이 삭제되었습니다.");
    }
    
    @Operation(summary = "댓글 좋아요/싫어요 추가/변경", description = "댓글에 좋아요 또는 싫어요를 추가하거나 변경합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 입력"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없습니다.")
    })
    @SecurityRequirement(name = "JWT")
    @PostMapping("/{id}/reactions")
    public ApiResponse<Void> addReaction(
        Authentication authentication,
        @Parameter(description = "게시글 ID", required = true)
        @PathVariable("communityId") Integer communityId,
        @Parameter(description = "댓글 ID", required = true)
        @PathVariable("id") Long id,
        @Valid @RequestBody CommunityCommentReactionRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        ReactionType reactionType = ReactionType.fromCode(request.getReactionType());
        communityCommentReactionService.addReaction(userId, id, reactionType);
        return ApiResponse.success(null, "반응이 추가되었습니다.");
    }
    
    @Operation(summary = "댓글 좋아요/싫어요 삭제", description = "댓글의 좋아요 또는 싫어요를 삭제합니다.")
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
        @PathVariable("communityId") Integer communityId,
        @Parameter(description = "댓글 ID", required = true)
        @PathVariable("id") Long id
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        communityCommentReactionService.removeReaction(userId, id);
        return ApiResponse.success(null, "반응이 삭제되었습니다.");
    }
}
