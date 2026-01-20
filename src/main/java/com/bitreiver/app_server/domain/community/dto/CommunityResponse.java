package com.bitreiver.app_server.domain.community.dto;

import com.bitreiver.app_server.domain.community.entity.Community;
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
@Schema(description = "커뮤니티 게시글 응답")
public class CommunityResponse {
    
    @Schema(description = "게시글 ID", example = "1")
    private Integer id;
    
    @Schema(description = "작성자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
    
    @Schema(description = "작성자 닉네임", example = "user123")
    private String userNickname;
    
    @Schema(description = "카테고리", example = "FREE")
    private String category;
    
    @Schema(description = "게시글 제목", example = "비트코인 투자 후기")
    private String title;
    
    @Schema(description = "게시글 내용 (JSONB 형식)", example = "{\"blocks\":[{\"type\":\"text\",\"content\":\"오늘 비트코인을 매수했습니다...\"}]}")
    private String content;
    
    @Schema(description = "해시태그 배열", example = "[\"비트코인\", \"투자\"]")
    private List<String> hashtags;
    
    @Schema(description = "좋아요 개수", example = "10")
    private Long likeCount;
    
    @Schema(description = "싫어요 개수", example = "2")
    private Long dislikeCount;
    
    @Schema(description = "사용자의 반응 (LIKE, DISLIKE, null)", example = "LIKE")
    private String userReaction;
    
    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;
    
    public static CommunityResponse from(Community community, String userNickname, Long likeCount, Long dislikeCount, ReactionType userReaction) {
        return CommunityResponse.builder()
            .id(community.getId())
            .userId(community.getUserId())
            .userNickname(userNickname)
            .category(community.getCategory() != null ? community.getCategory().getCode() : null)
            .title(community.getTitle())
            .content(community.getContent())
            .hashtags(community.getHashtags())
            .likeCount(likeCount != null ? likeCount : 0L)
            .dislikeCount(dislikeCount != null ? dislikeCount : 0L)
            .userReaction(userReaction != null ? userReaction.getCode() : null)
            .createdAt(community.getCreatedAt())
            .updatedAt(community.getUpdatedAt())
            .build();
    }
}
