package com.bitreiver.app_server.domain.community.service;

import com.bitreiver.app_server.domain.community.dto.CommunityListResponse;
import com.bitreiver.app_server.domain.community.dto.CommunityRequest;
import com.bitreiver.app_server.domain.community.dto.CommunityResponse;
import com.bitreiver.app_server.domain.community.dto.CommunitySearchRequest;
import com.bitreiver.app_server.domain.community.entity.Community;
import com.bitreiver.app_server.domain.community.enums.Category;
import com.bitreiver.app_server.domain.community.enums.ReactionType;
import com.bitreiver.app_server.domain.community.repository.CommunityRepository;
import com.bitreiver.app_server.domain.user.entity.User;
import com.bitreiver.app_server.domain.user.repository.UserRepository;
import com.bitreiver.app_server.global.common.dto.PageResponse;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {
    
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final CommunityReactionService communityReactionService;
    private final CommunityImageService communityImageService;
    private final CommunityCommentService communityCommentService;
    
    @Override
    @Transactional
    public CommunityResponse createCommunity(UUID userId, CommunityRequest request) {
        // 제목 바이트 수 검증 (100byte = 한글 33자 제한)
        if (request.getTitle() != null) {
            int titleBytes = request.getTitle().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            if (titleBytes > 100) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "제목은 100byte 이하로 작성해주세요. (한글 33자)");
            }
        }
        
        Category category = request.getCategoryAsEnum();
        if (category == null) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "유효하지 않은 카테고리입니다.");
        }
        
        Community community = Community.builder()
            .userId(userId)
            .category(category)
            .title(request.getTitle())
            .content(request.getContent())
            .hashtags(request.getHashtags())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        communityRepository.save(community);
        
        return CommunityResponse.from(community, null, null, 0L, 0L, null);
    }
    
    @Override
    public CommunityResponse getCommunityById(Integer id, UUID userId) {
        Community community = communityRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        
        User user = userRepository.findById(community.getUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        long likeCount = communityReactionService.getLikeCount(id);
        long dislikeCount = communityReactionService.getDislikeCount(id);
        ReactionType userReaction = userId != null ? communityReactionService.getUserReaction(userId, id) : null;
        
        return CommunityResponse.from(community, user.getNickname(), user.getProfileUrl(), likeCount, dislikeCount, userReaction);
    }
    
    @Override
    public CommunityResponse getCommunityByIdWithReactions(Integer id, UUID userId) {
        return getCommunityById(id, userId);
    }
    
    @Override
    public PageResponse<CommunityListResponse> getCommunitiesByCategory(Category category, int page, int size) {
        return getCommunitiesByCategory(category, page, size, null);
    }
    
    public PageResponse<CommunityListResponse> getCommunitiesByCategory(Category category, int page, int size, UUID userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Community> communities = communityRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
        
        List<CommunityListResponse> content = communities.getContent().stream()
            .map(community -> {
                User user = userRepository.findById(community.getUserId())
                    .orElse(null);
                long likeCount = communityReactionService.getLikeCount(community.getId());
                long dislikeCount = communityReactionService.getDislikeCount(community.getId());
                long commentCount = communityCommentService.getCommentCount(community.getId());
                com.bitreiver.app_server.domain.community.enums.ReactionType userReaction = userId != null ? 
                    communityReactionService.getUserReaction(userId, community.getId()) : null;
                
                // content에서 썸네일 이미지와 간추린 텍스트 추출
                String thumbnailImageUrl = extractFirstImagePath(community.getContent());
                String previewText = extractFirstTextPreview(community.getContent());
                
                return CommunityListResponse.builder()
                    .id(community.getId())
                    .userId(community.getUserId())
                    .userNickname(user != null ? user.getNickname() : null)
                    .userProfileUrl(user != null ? user.getProfileUrl() : null)
                    .category(community.getCategory() != null ? community.getCategory().getCode() : null)
                    .title(community.getTitle())
                    .hashtags(community.getHashtags())
                    .likeCount(likeCount)
                    .dislikeCount(dislikeCount)
                    .commentCount(commentCount)
                    .userReaction(userReaction != null ? userReaction.getCode() : null)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .previewText(previewText)
                    .createdAt(community.getCreatedAt())
                    .build();
            })
            .collect(Collectors.toList());
        
        return PageResponse.of(content, page, size, communities.getTotalElements());
    }
    
    @Override
    public PageResponse<CommunityListResponse> searchCommunities(CommunitySearchRequest request, UUID userId) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Community> communities;
        
        Category category = request.getCategory() != null ? Category.fromCode(request.getCategory()) : null;
        List<String> hashtags = request.getHashtags();
        
        if (hashtags == null || hashtags.isEmpty()) {
            // 해시태그 없이 카테고리만 검색
            if (category != null) {
                communities = communityRepository.findByCategoryOrderByCreatedAtDesc(category, pageable);
            } else {
                communities = communityRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        } else {
            // 해시태그 검색
            CommunitySearchRequest.SearchType searchType = request.getSearchType() != null 
                ? request.getSearchType() 
                : CommunitySearchRequest.SearchType.SINGLE;
            
            if (hashtags.size() == 1) {
                // 단일 해시태그
                String hashtag = hashtags.get(0);
                if (category != null) {
                    communities = communityRepository.findByCategoryAndHashtag(category.getCode(), hashtag, pageable);
                } else {
                    communities = communityRepository.findByHashtagsContaining(hashtag, pageable);
                }
            } else {
                // 다중 해시태그
                if (searchType == CommunitySearchRequest.SearchType.MULTIPLE_AND) {
                    if (category != null) {
                        communities = communityRepository.findByCategoryAndHashtagsAll(category.getCode(), hashtags, pageable);
                    } else {
                        communities = communityRepository.findByHashtagsContainingAll(hashtags, pageable);
                    }
                } else {
                    if (category != null) {
                        communities = communityRepository.findByCategoryAndHashtagsAny(category.getCode(), hashtags, pageable);
                    } else {
                        communities = communityRepository.findByHashtagsContainingAny(hashtags, pageable);
                    }
                }
            }
        }
        
        List<CommunityListResponse> content = communities.getContent().stream()
            .map(community -> {
                User user = userRepository.findById(community.getUserId())
                    .orElse(null);
                long likeCount = communityReactionService.getLikeCount(community.getId());
                long dislikeCount = communityReactionService.getDislikeCount(community.getId());
                long commentCount = communityCommentService.getCommentCount(community.getId());
                com.bitreiver.app_server.domain.community.enums.ReactionType userReaction = userId != null ? 
                    communityReactionService.getUserReaction(userId, community.getId()) : null;
                
                // content에서 썸네일 이미지와 간추린 텍스트 추출
                String thumbnailImageUrl = extractFirstImagePath(community.getContent());
                String previewText = extractFirstTextPreview(community.getContent());
                
                return CommunityListResponse.builder()
                    .id(community.getId())
                    .userId(community.getUserId())
                    .userNickname(user != null ? user.getNickname() : null)
                    .userProfileUrl(user != null ? user.getProfileUrl() : null)
                    .category(community.getCategory() != null ? community.getCategory().getCode() : null)
                    .title(community.getTitle())
                    .hashtags(community.getHashtags())
                    .likeCount(likeCount)
                    .dislikeCount(dislikeCount)
                    .commentCount(commentCount)
                    .userReaction(userReaction != null ? userReaction.getCode() : null)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .previewText(previewText)
                    .createdAt(community.getCreatedAt())
                    .build();
            })
            .collect(Collectors.toList());
        
        return PageResponse.of(content, request.getPage(), request.getSize(), communities.getTotalElements());
    }
    
    @Override
    @Transactional
    public CommunityResponse updateCommunity(UUID userId, Integer id, CommunityRequest request) {
        // 제목 바이트 수 검증 (100byte = 한글 33자 제한)
        if (request.getTitle() != null) {
            int titleBytes = request.getTitle().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            if (titleBytes > 100) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "제목은 100byte 이하로 작성해주세요. (한글 33자)");
            }
        }
        Community community = communityRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        
        if (request.getCategoryAsEnum() != null) {
            community.setCategory(request.getCategoryAsEnum());
        }
        
        if (request.getTitle() != null) {
            community.setTitle(request.getTitle());
        }
        
        if (request.getContent() != null) {
            community.setContent(request.getContent());
        }
        
        if (request.getHashtags() != null) {
            community.setHashtags(request.getHashtags());
        }
        
        community.setUpdatedAt(LocalDateTime.now());
        communityRepository.save(community);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        long likeCount = communityReactionService.getLikeCount(id);
        long dislikeCount = communityReactionService.getDislikeCount(id);
        ReactionType userReaction = communityReactionService.getUserReaction(userId, id);
        
        return CommunityResponse.from(community, user.getNickname(), user.getProfileUrl(), likeCount, dislikeCount, userReaction);
    }
    
    @Override
    @Transactional
    public CommunityResponse updateCommunityWithImageManagement(UUID userId, Integer id, CommunityRequest request) {
        // 제목 바이트 수 검증 (100byte = 한글 33자 제한)
        if (request.getTitle() != null) {
            int titleBytes = request.getTitle().getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
            if (titleBytes > 100) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "제목은 100byte 이하로 작성해주세요. (한글 33자)");
            }
        }
        
        CommunityResponse existingCommunity = getCommunityById(id, userId);
        
        // 이미지 개수 검증
        if (request.getContent() != null) {
            List<String> newImagePaths = extractAllImagePaths(request.getContent());
            if (newImagePaths.size() > 5) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "이미지는 최대 5개까지 추가할 수 있습니다.");
            }
        }
        
        List<String> existingImagePaths = extractAllImagePaths(existingCommunity.getContent());
        List<String> newImagePaths = extractAllImagePaths(request.getContent());
        
        List<String> imagesToDelete = existingImagePaths.stream()
                .filter(path -> !newImagePaths.contains(path))
                .collect(Collectors.toList());
        
        if (!imagesToDelete.isEmpty()) {
            try {
                communityImageService.deleteAllImages(id, imagesToDelete);
                log.info("삭제된 이미지 제거 완료: communityId={}, count={}", id, imagesToDelete.size());
            } catch (Exception e) {
                log.error("MinIO 이미지 삭제 실패: communityId={}, images={}", id, imagesToDelete, e);
            }
        }
        
        return updateCommunity(userId, id, request);
    }
    
    @Override
    @Transactional
    public void deleteCommunity(UUID userId, Integer id) {
        Community community = communityRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다."));
        
        // content에서 모든 이미지 경로 추출 및 삭제
        if (community.getContent() != null) {
            List<String> imagePaths = extractAllImagePaths(community.getContent());
            if (!imagePaths.isEmpty()) {
                communityImageService.deleteAllImages(id, imagePaths);
            }
        }
        
        communityRepository.delete(community);
    }
    
    @Override
    public PageResponse<CommunityListResponse> getMyCommunities(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Community> communities = communityRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        List<CommunityListResponse> content = communities.getContent().stream()
            .map(community -> {
                long likeCount = communityReactionService.getLikeCount(community.getId());
                long dislikeCount = communityReactionService.getDislikeCount(community.getId());
                long commentCount = communityCommentService.getCommentCount(community.getId());
                com.bitreiver.app_server.domain.community.enums.ReactionType userReaction = 
                    communityReactionService.getUserReaction(userId, community.getId());
                
                // content에서 썸네일 이미지와 간추린 텍스트 추출
                String thumbnailImageUrl = extractFirstImagePath(community.getContent());
                String previewText = extractFirstTextPreview(community.getContent());
                
                return CommunityListResponse.builder()
                    .id(community.getId())
                    .userId(community.getUserId())
                    .userNickname(user.getNickname())
                    .userProfileUrl(user.getProfileUrl())
                    .category(community.getCategory() != null ? community.getCategory().getCode() : null)
                    .title(community.getTitle())
                    .hashtags(community.getHashtags())
                    .likeCount(likeCount)
                    .dislikeCount(dislikeCount)
                    .commentCount(commentCount)
                    .userReaction(userReaction != null ? userReaction.getCode() : null)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .previewText(previewText)
                    .createdAt(community.getCreatedAt())
                    .build();
            })
            .collect(Collectors.toList());
        
        return PageResponse.of(content, page, size, communities.getTotalElements());
    }
    
    @Override
    public PageResponse<CommunityListResponse> getAllCommunities(int page, int size) {
        return getAllCommunities(page, size, null);
    }
    
    public PageResponse<CommunityListResponse> getAllCommunities(int page, int size, UUID userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Community> communities = communityRepository.findAllByOrderByCreatedAtDesc(pageable);
        
        List<CommunityListResponse> content = communities.getContent().stream()
            .map(community -> {
                User user = userRepository.findById(community.getUserId())
                    .orElse(null);
                long likeCount = communityReactionService.getLikeCount(community.getId());
                long dislikeCount = communityReactionService.getDislikeCount(community.getId());
                long commentCount = communityCommentService.getCommentCount(community.getId());
                com.bitreiver.app_server.domain.community.enums.ReactionType userReaction = userId != null ? 
                    communityReactionService.getUserReaction(userId, community.getId()) : null;
                
                // content에서 썸네일 이미지와 간추린 텍스트 추출
                String thumbnailImageUrl = extractFirstImagePath(community.getContent());
                String previewText = extractFirstTextPreview(community.getContent());
                
                return CommunityListResponse.builder()
                    .id(community.getId())
                    .userId(community.getUserId())
                    .userNickname(user != null ? user.getNickname() : null)
                    .userProfileUrl(user != null ? user.getProfileUrl() : null)
                    .category(community.getCategory() != null ? community.getCategory().getCode() : null)
                    .title(community.getTitle())
                    .hashtags(community.getHashtags())
                    .likeCount(likeCount)
                    .dislikeCount(dislikeCount)
                    .commentCount(commentCount)
                    .userReaction(userReaction != null ? userReaction.getCode() : null)
                    .thumbnailImageUrl(thumbnailImageUrl)
                    .previewText(previewText)
                    .createdAt(community.getCreatedAt())
                    .build();
            })
            .collect(Collectors.toList());
        
        return PageResponse.of(content, page, size, communities.getTotalElements());
    }
    
    private String extractFirstImagePath(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> contentMap = objectMapper.readValue(content, java.util.Map.class);
            @SuppressWarnings("unchecked")
            List<java.util.Map<String, Object>> blocks = (List<java.util.Map<String, Object>>) contentMap.get("blocks");
            
            if (blocks != null) {
                for (java.util.Map<String, Object> block : blocks) {
                    if ("image".equals(block.get("type"))) {
                        String path = (String) block.get("path");
                        if (path != null) {
                            return path;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Content에서 첫 번째 이미지 경로 추출 실패", e);
        }
        
        return null;
    }
    
    private String extractFirstTextPreview(String content) {
        if (content == null || content.trim().isEmpty()) {
            return null;
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> contentMap = objectMapper.readValue(content, java.util.Map.class);
            @SuppressWarnings("unchecked")
            List<java.util.Map<String, Object>> blocks = (List<java.util.Map<String, Object>>) contentMap.get("blocks");
            
            if (blocks != null) {
                for (java.util.Map<String, Object> block : blocks) {
                    if ("text".equals(block.get("type"))) {
                        String text = (String) block.get("content");
                        if (text != null && !text.trim().isEmpty()) {
                            // 줄바꿈을 공백으로 변환
                            String trimmed = text.trim().replaceAll("\\s+", " ");
                            // 60자로 제한
                            if (trimmed.length() > 60) {
                                return trimmed.substring(0, 60) + "...";
                            }
                            return trimmed;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Content에서 첫 번째 텍스트 블록 추출 실패", e);
        }
        
        return null;
    }
    
    @Override
    public List<String> extractAllImagePaths(String content) {
        List<String> imagePaths = new java.util.ArrayList<>();
        
        if (content == null || content.trim().isEmpty()) {
            return imagePaths;
        }
        
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> contentMap = objectMapper.readValue(content, java.util.Map.class);
            @SuppressWarnings("unchecked")
            List<java.util.Map<String, Object>> blocks = (List<java.util.Map<String, Object>>) contentMap.get("blocks");
            
            if (blocks != null) {
                for (java.util.Map<String, Object> block : blocks) {
                    if ("image".equals(block.get("type"))) {
                        String path = (String) block.get("path");
                        if (path != null) {
                            imagePaths.add(path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Content에서 이미지 경로 추출 실패", e);
        }
        
        return imagePaths;
    }
}
