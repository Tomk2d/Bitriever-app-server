package com.bitreiver.app_server.domain.community.service;

import com.bitreiver.app_server.domain.community.entity.CommunityReaction;
import com.bitreiver.app_server.domain.community.enums.ReactionType;
import com.bitreiver.app_server.domain.community.repository.CommunityReactionRepository;
import com.bitreiver.app_server.domain.community.repository.CommunityRepository;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityReactionServiceImpl implements CommunityReactionService {
    
    private final CommunityReactionRepository communityReactionRepository;
    private final CommunityRepository communityRepository;
    
    @Override
    @Transactional
    public void addReaction(UUID userId, Integer communityId, ReactionType reactionType) {
        // 게시글 존재 확인
        if (!communityRepository.existsById(communityId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        
        // 기존 반응 조회
        CommunityReaction existingReaction = communityReactionRepository
            .findByUserIdAndCommunityId(userId, communityId)
            .orElse(null);
        
        if (existingReaction != null) {
            // 같은 타입이면 삭제 (토글)
            if (existingReaction.getReactionType() == reactionType) {
                communityReactionRepository.delete(existingReaction);
            } else {
                // 다른 타입이면 변경
                existingReaction.setReactionType(reactionType);
                existingReaction.setCreatedAt(LocalDateTime.now());
                communityReactionRepository.save(existingReaction);
            }
        } else {
            // 새 반응 추가
            CommunityReaction reaction = CommunityReaction.builder()
                .userId(userId)
                .communityId(communityId)
                .reactionType(reactionType)
                .createdAt(LocalDateTime.now())
                .build();
            
            communityReactionRepository.save(reaction);
        }
    }
    
    @Override
    @Transactional
    public void removeReaction(UUID userId, Integer communityId) {
        CommunityReaction reaction = communityReactionRepository
            .findByUserIdAndCommunityId(userId, communityId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "반응을 찾을 수 없습니다."));
        
        communityReactionRepository.delete(reaction);
    }
    
    @Override
    public long getLikeCount(Integer communityId) {
        return communityReactionRepository.countByCommunityIdAndReactionType(communityId, ReactionType.LIKE);
    }
    
    @Override
    public long getDislikeCount(Integer communityId) {
        return communityReactionRepository.countByCommunityIdAndReactionType(communityId, ReactionType.DISLIKE);
    }
    
    @Override
    public ReactionType getUserReaction(UUID userId, Integer communityId) {
        return communityReactionRepository
            .findByUserIdAndCommunityId(userId, communityId)
            .map(CommunityReaction::getReactionType)
            .orElse(null);
    }
}
