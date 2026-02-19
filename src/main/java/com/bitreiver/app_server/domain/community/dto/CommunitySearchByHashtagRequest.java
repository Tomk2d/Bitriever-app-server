package com.bitreiver.app_server.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "단일 해시태그로 커뮤니티 게시글 검색 요청")
public class CommunitySearchByHashtagRequest {

    @NotBlank(message = "해시태그는 필수입니다.")
    @Schema(description = "검색할 해시태그 (1개)", example = "비트코인", requiredMode = Schema.RequiredMode.REQUIRED)
    private String hashtag;

    @Schema(description = "카테고리 (선택)", example = "FREE")
    private String category;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private int page = 0;

    @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 최대 100까지 가능합니다.")
    private int size = 20;
}
