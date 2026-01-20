package com.bitreiver.app_server.domain.community.repository;

import com.bitreiver.app_server.domain.community.entity.CommunityComment;
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
public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    
    // 최상위 댓글 조회 (parentId가 null인 댓글)
    Page<CommunityComment> findByCommunityIdAndParentIdIsNullOrderByCreatedAtDesc(Integer communityId, Pageable pageable);
    
    // 대댓글 조회 (특정 부모 댓글의 대댓글)
    List<CommunityComment> findByParentIdOrderByCreatedAtAsc(Long parentId);
    
    // 작성자 확인용
    Optional<CommunityComment> findByIdAndUserId(Long id, UUID userId);
    
    // 댓글 존재 확인
    Optional<CommunityComment> findByIdAndCommunityId(Long id, Integer communityId);
    
    // 게시글별 댓글 개수 (삭제되지 않은 댓글만)
    @Query("SELECT COUNT(c) FROM CommunityComment c WHERE c.communityId = :communityId AND c.deleted = false")
    long countByCommunityId(@Param("communityId") Integer communityId);
    
    // 댓글 존재 여부
    boolean existsByIdAndCommunityId(Long id, Integer communityId);
    
    // 게시글별 모든 댓글 조회 (최상위 + 대댓글)
    List<CommunityComment> findByCommunityIdOrderByCreatedAtDesc(Integer communityId);
}
