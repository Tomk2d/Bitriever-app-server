package com.bitreiver.app_server.domain.community.service;

import com.bitreiver.app_server.domain.community.enums.ReactionType;

import java.util.UUID;

public interface CommunityCommentReactionService {
    void addReaction(UUID userId, Long commentId, ReactionType reactionType);
    void removeReaction(UUID userId, Long commentId);
    long getLikeCount(Long commentId);
    long getDislikeCount(Long commentId);
    ReactionType getUserReaction(UUID userId, Long commentId);
}
