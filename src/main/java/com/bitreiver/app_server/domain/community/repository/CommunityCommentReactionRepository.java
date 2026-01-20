package com.bitreiver.app_server.domain.community.repository;

import com.bitreiver.app_server.domain.community.entity.CommunityCommentReaction;
import com.bitreiver.app_server.domain.community.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityCommentReactionRepository extends JpaRepository<CommunityCommentReaction, Long> {
    
    Optional<CommunityCommentReaction> findByUserIdAndCommentId(UUID userId, Long commentId);
    
    long countByCommentIdAndReactionType(Long commentId, ReactionType reactionType);
    
    boolean existsByUserIdAndCommentId(UUID userId, Long commentId);
    
    void deleteByUserIdAndCommentId(UUID userId, Long commentId);
    
    @Query("SELECT COUNT(cr) FROM CommunityCommentReaction cr WHERE cr.commentId = :commentId AND cr.reactionType = :reactionType")
    long countReactionsByCommentIdAndType(@Param("commentId") Long commentId, @Param("reactionType") ReactionType reactionType);
}
