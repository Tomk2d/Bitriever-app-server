package com.bitreiver.app_server.domain.article.controller;

import com.bitreiver.app_server.domain.article.dto.ArticleResponse;
import com.bitreiver.app_server.domain.article.service.ArticleService;
import com.bitreiver.app_server.global.common.dto.PageResponse;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.bitreiver.app_server.domain.article.dto.ArticleRequest;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "Article", description = "기사 조회 API")
public class ArticleController {
    private final ArticleService articleService;
    
    @Operation(summary = "기사 ID로 조회", description = "기사 ID로 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "기사를 찾을 수 없습니다.")
    })
    @GetMapping("/{id}")
    public ApiResponse<ArticleResponse> getArticleById(
        @Parameter(name = "id", description = "기사 ID", required = true, in = ParameterIn.PATH)
        @PathVariable Long id
    ) {
        log.info("기사 조회 - id: {}", id);
        Optional<ArticleResponse> article = articleService.getArticleById(id);
        
        return article.map(ApiResponse::success)
            .orElseGet(() -> ApiResponse.error("ARTICLE_NOT_FOUND", "기사를 찾을 수 없습니다."));
    }
    
    @Operation(summary = "언론사 타입별 기사 조회", description = "언론사 타입으로 기사를 필터링하여 조회합니다.")
    @GetMapping("/publisher/{publisherType}")
    public ApiResponse<PageResponse<ArticleResponse>> getArticlesByPublisherType(
        @Parameter(name = "publisherType", description = "언론사 타입", required = true, in = ParameterIn.PATH)
        @PathVariable Integer publisherType,
        @Valid @ModelAttribute ArticleRequest pageRequest
    ) {
        Sort sortObj = Sort.by(Sort.Direction.fromString(pageRequest.getDirection()), pageRequest.getSort());
        Pageable pageable = PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sortObj);
        
        PageResponse<ArticleResponse> articles = articleService.getArticlesByPublisherType(publisherType, pageable);
        return ApiResponse.success(articles);
    }
    
    @Operation(summary = "최신 기사 조회", description = "발행일 기준 최신 기사를 조회합니다.")
    @GetMapping("/latest")
    public ApiResponse<PageResponse<ArticleResponse>> getLatestArticles(
        @Valid @ModelAttribute ArticleRequest pageRequest
    ) {
        Sort sortObj = Sort.by(Sort.Direction.fromString(pageRequest.getDirection()), pageRequest.getSort());
        Pageable pageable = PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sortObj);
        
        PageResponse<ArticleResponse> articles = articleService.getLatestArticles(pageable);
        return ApiResponse.success(articles);
    }
    
    @Operation(summary = "날짜 범위별 기사 조회", description = "발행일 기준으로 날짜 범위를 지정하여 기사를 조회합니다.")
    @GetMapping("/date-range")
    public ApiResponse<PageResponse<ArticleResponse>> getArticlesByDateRange(
        @Parameter(description = "시작 날짜 (yyyy-MM-ddTHH:mm:ss)", example = "2024-01-01T00:00:00")
        @RequestParam(value = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @Parameter(description = "종료 날짜 (yyyy-MM-ddTHH:mm:ss)", example = "2024-12-31T23:59:59")
        @RequestParam(value = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
        @Valid @ModelAttribute ArticleRequest pageRequest
    ) {
        Sort sortObj = Sort.by(Sort.Direction.fromString(pageRequest.getDirection()), pageRequest.getSort());
        Pageable pageable = PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sortObj);
        
        PageResponse<ArticleResponse> articles = articleService.getArticlesByDateRange(startDate, endDate, pageable);
        return ApiResponse.success(articles);
    }
    
    @Operation(summary = "기사 검색", description = "제목에 키워드를 포함하는 기사를 검색합니다.")
    @GetMapping("/search")
    public ApiResponse<PageResponse<ArticleResponse>> searchArticles(
        @Parameter(description = "검색 키워드", required = true)
        @RequestParam(value = "keyword") String keyword,
        @Valid @ModelAttribute ArticleRequest pageRequest
    ) {
        Sort sortObj = Sort.by(Sort.Direction.fromString(pageRequest.getDirection()), pageRequest.getSort());
        Pageable pageable = PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sortObj);
        
        PageResponse<ArticleResponse> articles = articleService.searchArticles(keyword, pageable);
        return ApiResponse.success(articles);
    }
    
    @Operation(summary = "언론사 타입별 기사 헤드라인 검색", description = "언론사 타입과 키워드로 기사 헤드라인을 검색합니다.")
    @GetMapping("/publisher/{publisherType}/search")
    public ApiResponse<PageResponse<ArticleResponse>> searchArticlesByPublisherType(
        @PathVariable Integer publisherType,
        @RequestParam(value = "keyword") String keyword,
        @Valid @ModelAttribute ArticleRequest pageRequest
    ) {
        Sort sortObj = Sort.by(Sort.Direction.fromString(pageRequest.getDirection()), pageRequest.getSort());
        Pageable pageable = PageRequest.of(pageRequest.getPage(), pageRequest.getSize(), sortObj);
        
        PageResponse<ArticleResponse> articles = articleService.searchArticlesByPublisherType(
            publisherType, keyword, pageable
        );
        return ApiResponse.success(articles);
    }
}
