package com.bitreiver.app_server.domain.community.service;

import com.bitreiver.app_server.domain.community.dto.CommunityRequest;
import com.bitreiver.app_server.domain.community.dto.CommunityResponse;
import com.bitreiver.app_server.domain.community.dto.CommunityListResponse;
import com.bitreiver.app_server.domain.community.dto.CommunitySearchByHashtagRequest;
import com.bitreiver.app_server.domain.community.dto.CommunitySearchRequest;
import com.bitreiver.app_server.domain.community.enums.Category;
import com.bitreiver.app_server.global.common.dto.PageResponse;

import java.util.List;
import java.util.UUID;

public interface CommunityService {
    CommunityResponse createCommunity(UUID userId, CommunityRequest request);
    CommunityResponse getCommunityById(Integer id, UUID userId);
    CommunityResponse getCommunityByIdWithReactions(Integer id, UUID userId);
    PageResponse<CommunityListResponse> getCommunitiesByCategory(Category category, int page, int size);
    PageResponse<CommunityListResponse> searchCommunities(CommunitySearchRequest request, UUID userId);
    PageResponse<CommunityListResponse> searchCommunitiesByHashtag(CommunitySearchByHashtagRequest request, UUID userId);
    CommunityResponse updateCommunity(UUID userId, Integer id, CommunityRequest request);
    CommunityResponse updateCommunityWithImageManagement(UUID userId, Integer id, CommunityRequest request);
    void deleteCommunity(UUID userId, Integer id);
    PageResponse<CommunityListResponse> getMyCommunities(UUID userId, int page, int size);
    PageResponse<CommunityListResponse> getAllCommunities(int page, int size);
    List<String> extractAllImagePaths(String content);
}
