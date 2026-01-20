package com.bitreiver.app_server.domain.community.service;

import com.bitreiver.app_server.domain.community.dto.CommunityCommentRequest;
import com.bitreiver.app_server.domain.community.dto.CommunityCommentResponse;
import com.bitreiver.app_server.global.common.dto.PageResponse;

import java.util.UUID;

public interface CommunityCommentService {
    CommunityCommentResponse createComment(UUID userId, Integer communityId, CommunityCommentRequest request);
    PageResponse<CommunityCommentResponse> getCommentsByCommunityId(Integer communityId, UUID userId, int page, int size);
    CommunityCommentResponse updateComment(UUID userId, Long id, CommunityCommentRequest request);
    void deleteComment(UUID userId, Long id);
    long getCommentCount(Integer communityId);
}
