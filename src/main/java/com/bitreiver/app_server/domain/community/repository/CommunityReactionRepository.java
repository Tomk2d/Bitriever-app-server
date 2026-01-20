package com.bitreiver.app_server.domain.community.repository;

import com.bitreiver.app_server.domain.community.entity.CommunityReaction;
import com.bitreiver.app_server.domain.community.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityReactionRepository extends JpaRepository<CommunityReaction, Long> {
    
    Optional<CommunityReaction> findByUserIdAndCommunityId(UUID userId, Integer communityId);
    
    long countByCommunityIdAndReactionType(Integer communityId, ReactionType reactionType);
    
    boolean existsByUserIdAndCommunityId(UUID userId, Integer communityId);
    
    void deleteByUserIdAndCommunityId(UUID userId, Integer communityId);
    
    @Query("SELECT COUNT(cr) FROM CommunityReaction cr WHERE cr.communityId = :communityId AND cr.reactionType = :reactionType")
    long countReactionsByCommunityIdAndType(@Param("communityId") Integer communityId, @Param("reactionType") ReactionType reactionType);
}
