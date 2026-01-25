package com.bitreiver.app_server.domain.community.dto;

import com.bitreiver.app_server.domain.community.entity.CommunityComment;
import com.bitreiver.app_server.domain.community.enums.ReactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "커뮤니티 댓글 응답")
public class CommunityCommentResponse {
    
    @Schema(description = "댓글 ID", example = "1")
    private Long id;
    
    @Schema(description = "게시글 ID", example = "1")
    private Integer communityId;
    
    @Schema(description = "작성자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
    
    @Schema(description = "작성자 닉네임", example = "user123")
    private String userNickname;
    
    @Schema(description = "작성자 프로필 이미지 URL", example = "/profile1")
    private String userProfileUrl;
    
    @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "1")
    private Long parentId;
    
    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!")
    private String content;
    
    @Schema(description = "좋아요 개수", example = "10")
    private Long likeCount;
    
    @Schema(description = "싫어요 개수", example = "2")
    private Long dislikeCount;
    
    @Schema(description = "사용자의 반응 (LIKE, DISLIKE, null)", example = "LIKE")
    private String userReaction;
    
    @Schema(description = "대댓글 목록")
    private List<CommunityCommentResponse> replies;
    
    @Schema(description = "삭제 여부", example = "false")
    private Boolean deleted;
    
    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
    
    public static CommunityCommentResponse from(
        CommunityComment comment,
        String userNickname,
        String userProfileUrl,
        Long likeCount,
        Long dislikeCount,
        ReactionType userReaction,
        List<CommunityCommentResponse> replies
    ) {
        return CommunityCommentResponse.builder()
            .id(comment.getId())
            .communityId(comment.getCommunityId())
            .userId(comment.getUserId())
            .userNickname(userNickname)
            .userProfileUrl(userProfileUrl)
            .parentId(comment.getParentId())
            .content(comment.getDeleted() ? "삭제된 댓글입니다." : comment.getContent())
            .likeCount(likeCount != null ? likeCount : 0L)
            .dislikeCount(dislikeCount != null ? dislikeCount : 0L)
            .userReaction(userReaction != null ? userReaction.getCode() : null)
            .replies(replies != null ? replies : List.of())
            .deleted(comment.getDeleted())
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .build();
    }
}
