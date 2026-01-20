package com.bitreiver.app_server.domain.community.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "커뮤니티 게시글 검색 요청")
public class CommunitySearchRequest {
    
    @Schema(description = "카테고리", example = "FREE")
    private String category;
    
    @Schema(description = "해시태그 배열", example = "[\"비트코인\", \"투자\"]")
    private List<String> hashtags;
    
    @Schema(description = "검색 타입", example = "SINGLE", allowableValues = {"SINGLE", "MULTIPLE_AND", "MULTIPLE_OR"})
    private SearchType searchType;
    
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private int page = 0;
    
    @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 최대 100까지 가능합니다.")
    private int size = 20;
    
    public enum SearchType {
        SINGLE,      // 단일 해시태그 검색
        MULTIPLE_AND, // 다중 해시태그 AND 검색
        MULTIPLE_OR  // 다중 해시태그 OR 검색
    }
}
