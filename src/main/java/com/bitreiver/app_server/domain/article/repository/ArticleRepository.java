package com.bitreiver.app_server.domain.article.repository;

import com.bitreiver.app_server.domain.article.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    // 기본 조회 메서드
    Optional<Article> findByOriginalUrl(String originalUrl);
    
    // 언론사 타입별 조회
    Page<Article> findByPublisherType(Integer publisherType, Pageable pageable);
    
    // 날짜 범위 조회
    Page<Article> findByPublishedAtBetween(
        LocalDateTime startDate, 
        LocalDateTime endDate, 
        Pageable pageable
    );
    
    // 언론사 타입 + 날짜 범위 조회
    Page<Article> findByPublisherTypeAndPublishedAtBetween(
        Integer publisherType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
    
    // 최신 기사 조회 (발행일 기준 내림차순)
    @Query("SELECT a FROM Article a ORDER BY a.publishedAt DESC")
    Page<Article> findLatestArticles(Pageable pageable);
    
    // 언론사 타입별 최신 기사 조회
    @Query("SELECT a FROM Article a WHERE a.publisherType = :publisherType ORDER BY a.publishedAt DESC")
    Page<Article> findLatestArticlesByPublisherType(
        @Param("publisherType") Integer publisherType, 
        Pageable pageable
    );
    
    // 검색 (제목에 키워드 포함)
    @Query("SELECT a FROM Article a WHERE a.headline LIKE %:keyword% ORDER BY a.publishedAt DESC")
    Page<Article> searchByHeadline(@Param("keyword") String keyword, Pageable pageable);
    
    // 언론사 타입별 검색
    @Query("SELECT a FROM Article a WHERE a.publisherType = :publisherType AND a.headline LIKE %:keyword% ORDER BY a.publishedAt DESC")
    Page<Article> searchByPublisherTypeAndHeadline(
        @Param("publisherType") Integer publisherType,
        @Param("keyword") String keyword,
        Pageable pageable
    );
}
