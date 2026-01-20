package com.bitreiver.app_server.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "커뮤니티 댓글 요청")
public class CommunityCommentRequest {
    
    @Schema(description = "댓글 내용", example = "좋은 글 감사합니다!", required = true)
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
    
    @Schema(description = "부모 댓글 ID (대댓글인 경우)", example = "1")
    private Long parentId;
}
