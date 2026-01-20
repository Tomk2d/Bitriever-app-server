package com.bitreiver.app_server.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "커뮤니티 게시글 반응 요청")
public class CommunityReactionRequest {
    
    @Schema(description = "반응 타입", example = "LIKE", required = true, allowableValues = {"LIKE", "DISLIKE"})
    @NotNull(message = "반응 타입은 필수입니다.")
    private String reactionType;
}
