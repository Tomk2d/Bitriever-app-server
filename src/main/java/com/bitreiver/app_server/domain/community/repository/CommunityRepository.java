package com.bitreiver.app_server.domain.community.repository;

import com.bitreiver.app_server.domain.community.entity.Community;
import com.bitreiver.app_server.domain.community.enums.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Integer> {
    
    Page<Community> findByCategoryOrderByCreatedAtDesc(Category category, Pageable pageable);
    
    // 단일 해시태그 검색
    @Query(value = "SELECT * FROM communities WHERE :hashtag = ANY(hashtags) ORDER BY created_at DESC", nativeQuery = true)
    Page<Community> findByHashtagsContaining(@Param("hashtag") String hashtag, Pageable pageable);
    
    // 다중 해시태그 AND 검색 (모든 해시태그 포함)
    @Query(value = "SELECT * FROM communities WHERE hashtags @> CAST(:hashtags AS text[]) ORDER BY created_at DESC", nativeQuery = true)
    Page<Community> findByHashtagsContainingAll(@Param("hashtags") List<String> hashtags, Pageable pageable);
    
    // 다중 해시태그 OR 검색 (하나 이상의 해시태그 포함)
    @Query(value = "SELECT * FROM communities WHERE hashtags && CAST(:hashtags AS text[]) ORDER BY created_at DESC", nativeQuery = true)
    Page<Community> findByHashtagsContainingAny(@Param("hashtags") List<String> hashtags, Pageable pageable);
    
    // 카테고리 + 단일 해시태그
    @Query(value = "SELECT * FROM communities WHERE category = :category AND :hashtag = ANY(hashtags) ORDER BY created_at DESC", nativeQuery = true)
    Page<Community> findByCategoryAndHashtag(@Param("category") String category, @Param("hashtag") String hashtag, Pageable pageable);
    
    // 카테고리 + 다중 해시태그 AND
    @Query(value = "SELECT * FROM communities WHERE category = :category AND hashtags @> CAST(:hashtags AS text[]) ORDER BY created_at DESC", nativeQuery = true)
    Page<Community> findByCategoryAndHashtagsAll(@Param("category") String category, @Param("hashtags") List<String> hashtags, Pageable pageable);
    
    // 카테고리 + 다중 해시태그 OR
    @Query(value = "SELECT * FROM communities WHERE category = :category AND hashtags && CAST(:hashtags AS text[]) ORDER BY created_at DESC", nativeQuery = true)
    Page<Community> findByCategoryAndHashtagsAny(@Param("category") String category, @Param("hashtags") List<String> hashtags, Pageable pageable);
    
    Optional<Community> findByIdAndUserId(Integer id, UUID userId);
    
    Page<Community> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    // 전체 목록 조회 (최신순)
    Page<Community> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
