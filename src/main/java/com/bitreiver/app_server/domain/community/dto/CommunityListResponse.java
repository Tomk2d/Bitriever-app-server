package com.bitreiver.app_server.domain.community.dto;

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
@Schema(description = "커뮤니티 게시글 목록 응답")
public class CommunityListResponse {
    
    @Schema(description = "게시글 ID", example = "1")
    private Integer id;
    
    @Schema(description = "작성자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID userId;
    
    @Schema(description = "작성자 닉네임", example = "user123")
    private String userNickname;
    
    @Schema(description = "작성자 프로필 이미지 URL", example = "/profile1")
    private String userProfileUrl;
    
    @Schema(description = "카테고리", example = "FREE")
    private String category;
    
    @Schema(description = "게시글 제목", example = "비트코인 투자 후기")
    private String title;
    
    @Schema(description = "해시태그 배열", example = "[\"비트코인\", \"투자\"]")
    private List<String> hashtags;
    
    @Schema(description = "좋아요 개수", example = "10")
    private Long likeCount;
    
    @Schema(description = "싫어요 개수", example = "2")
    private Long dislikeCount;
    
    @Schema(description = "댓글 개수", example = "15")
    private Long commentCount;
    
    @Schema(description = "사용자의 반응 (LIKE, DISLIKE, null)", example = "LIKE")
    private String userReaction;
    
    @Schema(description = "썸네일 이미지 URL (첫 번째 이미지)", example = "@communityImage/1/image.jpg")
    private String thumbnailImageUrl;
    
    @Schema(description = "간추린 텍스트 (첫 번째 텍스트 블록, 최대 100자)", example = "비트코인 투자 후기를 공유합니다...")
    private String previewText;
    
    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
}
