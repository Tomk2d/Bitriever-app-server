package com.bitreiver.app_server.domain.article.dto;

import com.bitreiver.app_server.domain.article.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {
    private Long id;
    private Integer articleId;
    private String headline;
    private String summary;
    private String originalUrl;
    private String reporterName;
    private String publisherName;
    private Integer publisherType;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ArticleResponse from(Article article) {
        return ArticleResponse.builder()
            .id(article.getId())
            .articleId(article.getArticleId())
            .headline(article.getHeadline())
            .summary(article.getSummary())
            .originalUrl(article.getOriginalUrl())
            .reporterName(article.getReporterName())
            .publisherName(article.getPublisherName())
            .publisherType(article.getPublisherType())
            .publishedAt(article.getPublishedAt())
            .createdAt(article.getCreatedAt())
            .updatedAt(article.getUpdatedAt())
            .build();
    }
}
