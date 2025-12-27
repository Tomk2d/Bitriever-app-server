package com.bitreiver.app_server.domain.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "페이징 요청")
public class ArticleRequest {
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private int page = 0;
    
    @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 최대 100까지 가능합니다.")
    private int size = 20;
    
    @Schema(description = "정렬 필드", example = "publishedAt", defaultValue = "publishedAt")
    private String sort = "publishedAt";
    
    @Schema(description = "정렬 방향 (ASC, DESC)", example = "DESC", defaultValue = "DESC")
    private String direction = "DESC";
}
