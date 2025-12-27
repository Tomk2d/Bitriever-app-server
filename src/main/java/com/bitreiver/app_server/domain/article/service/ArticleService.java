package com.bitreiver.app_server.domain.article.service;

import com.bitreiver.app_server.domain.article.dto.ArticleResponse;
import com.bitreiver.app_server.global.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ArticleService {
        
    PageResponse<ArticleResponse> getArticlesByPublisherType(Integer publisherType, Pageable pageable);
    
    PageResponse<ArticleResponse> getArticlesByDateRange(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
    
    PageResponse<ArticleResponse> getArticlesByPublisherTypeAndDateRange(
        Integer publisherType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
    
    PageResponse<ArticleResponse> getLatestArticles(Pageable pageable);
    
    PageResponse<ArticleResponse> getLatestArticlesByPublisherType(Integer publisherType, Pageable pageable);
    
    PageResponse<ArticleResponse> searchArticles(String keyword, Pageable pageable);
    
    PageResponse<ArticleResponse> searchArticlesByPublisherType(Integer publisherType, String keyword, Pageable pageable);
    
    Optional<ArticleResponse> getArticleById(Long id);
    
    Optional<ArticleResponse> getArticleByUrl(String originalUrl);
}
