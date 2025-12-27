package com.bitreiver.app_server.domain.article.service;

import com.bitreiver.app_server.domain.article.dto.ArticleResponse;
import com.bitreiver.app_server.domain.article.entity.Article;
import com.bitreiver.app_server.domain.article.repository.ArticleRepository;
import com.bitreiver.app_server.global.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;

    @Override
    public PageResponse<ArticleResponse> getArticlesByPublisherType(Integer publisherType, Pageable pageable) {
        Page<Article> articlePage = articleRepository.findByPublisherType(publisherType, pageable);
        return convertToPageResponse(articlePage);
    }

    @Override
    public PageResponse<ArticleResponse> getArticlesByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        Page<Article> articlePage = articleRepository.findByPublishedAtBetween(startDate, endDate, pageable);
        return convertToPageResponse(articlePage);
    }

    @Override
    public PageResponse<ArticleResponse> getArticlesByPublisherTypeAndDateRange(
        Integer publisherType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ) {
        Page<Article> articlePage = articleRepository.findByPublisherTypeAndPublishedAtBetween(
            publisherType, startDate, endDate, pageable
        );
        return convertToPageResponse(articlePage);
    }
    
    @Override
    public PageResponse<ArticleResponse> getLatestArticles(Pageable pageable) {
        Page<Article> articlePage = articleRepository.findLatestArticles(pageable);
        return convertToPageResponse(articlePage);
    }
    
    @Override
    public PageResponse<ArticleResponse> getLatestArticlesByPublisherType(Integer publisherType, Pageable pageable) {
        Page<Article> articlePage = articleRepository.findLatestArticlesByPublisherType(publisherType, pageable);
        return convertToPageResponse(articlePage);
    }
    
    @Override
    public PageResponse<ArticleResponse> searchArticles(String keyword, Pageable pageable) {
        Page<Article> articlePage = articleRepository.searchByHeadline(keyword, pageable);
        return convertToPageResponse(articlePage);
    }
    
    @Override
    public PageResponse<ArticleResponse> searchArticlesByPublisherType(Integer publisherType, String keyword, Pageable pageable) {
        Page<Article> articlePage = articleRepository.searchByPublisherTypeAndHeadline(publisherType, keyword, pageable);
        return convertToPageResponse(articlePage);
    }
    
    @Override
    public Optional<ArticleResponse> getArticleById(Long id) {
        return articleRepository.findById(id)
            .map(ArticleResponse::from);
    }
    
    @Override
    public Optional<ArticleResponse> getArticleByUrl(String originalUrl) {
        return articleRepository.findByOriginalUrl(originalUrl)
            .map(ArticleResponse::from);
    }

    private PageResponse<ArticleResponse> convertToPageResponse(Page<Article> articlePage) {
        return PageResponse.of(
            articlePage.getContent().stream()
                .map(ArticleResponse::from)
                .collect(Collectors.toList()),
            articlePage.getNumber(),
            articlePage.getSize(),
            articlePage.getTotalElements()
        );
    }
}
