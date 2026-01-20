package com.bitreiver.app_server.domain.community.service;

import com.bitreiver.app_server.domain.community.enums.ReactionType;

import java.util.UUID;

public interface CommunityReactionService {
    void addReaction(UUID userId, Integer communityId, ReactionType reactionType);
    void removeReaction(UUID userId, Integer communityId);
    long getLikeCount(Integer communityId);
    long getDislikeCount(Integer communityId);
    ReactionType getUserReaction(UUID userId, Integer communityId);
}
