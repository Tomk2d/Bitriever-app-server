package com.bitreiver.app_server.domain.community.dto;

import com.bitreiver.app_server.domain.community.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "커뮤니티 게시글 요청")
public class CommunityRequest {
    
    @Schema(description = "카테고리", example = "FREE", required = true)
    @NotNull(message = "카테고리는 필수입니다.")
    private String category;
    
    @Schema(description = "게시글 제목", example = "비트코인 투자 후기", required = true)
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    
    @Schema(description = "게시글 내용 (JSONB 형식)", example = "{\"blocks\":[{\"type\":\"text\",\"content\":\"오늘 비트코인을 매수했습니다...\"}]}")
    private String content;
    
    @Schema(description = "해시태그 배열", example = "[\"비트코인\", \"투자\"]")
    private List<String> hashtags;
    
    @Schema(hidden = true)
    public Category getCategoryAsEnum() {
        if (category == null) {
            return null;
        }
        try {
            return Category.fromCode(category);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
