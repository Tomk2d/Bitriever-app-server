package com.bitreiver.app_server.domain.community.service;

import com.bitreiver.app_server.domain.community.entity.CommunityCommentReaction;
import com.bitreiver.app_server.domain.community.enums.ReactionType;
import com.bitreiver.app_server.domain.community.repository.CommunityCommentReactionRepository;
import com.bitreiver.app_server.domain.community.repository.CommunityCommentRepository;
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
public class CommunityCommentReactionServiceImpl implements CommunityCommentReactionService {
    
    private final CommunityCommentReactionRepository communityCommentReactionRepository;
    private final CommunityCommentRepository communityCommentRepository;
    
    @Override
    @Transactional
    public void addReaction(UUID userId, Long commentId, ReactionType reactionType) {
        // 댓글 존재 확인
        if (!communityCommentRepository.existsById(commentId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다.");
        }
        
        // 기존 반응 조회
        CommunityCommentReaction existingReaction = communityCommentReactionRepository
            .findByUserIdAndCommentId(userId, commentId)
            .orElse(null);
        
        if (existingReaction != null) {
            // 같은 타입이면 삭제 (토글)
            if (existingReaction.getReactionType() == reactionType) {
                communityCommentReactionRepository.delete(existingReaction);
                log.info("댓글 반응 삭제: userId={}, commentId={}, reactionType={}", userId, commentId, reactionType);
            } else {
                // 다른 타입이면 변경
                existingReaction.setReactionType(reactionType);
                existingReaction.setCreatedAt(LocalDateTime.now());
                communityCommentReactionRepository.save(existingReaction);
                log.info("댓글 반응 변경: userId={}, commentId={}, oldType={}, newType={}", 
                    userId, commentId, existingReaction.getReactionType(), reactionType);
            }
        } else {
            // 새 반응 추가
            CommunityCommentReaction reaction = CommunityCommentReaction.builder()
                .userId(userId)
                .commentId(commentId)
                .reactionType(reactionType)
                .createdAt(LocalDateTime.now())
                .build();
            
            communityCommentReactionRepository.save(reaction);
            log.info("댓글 반응 추가: userId={}, commentId={}, reactionType={}", userId, commentId, reactionType);
        }
    }
    
    @Override
    @Transactional
    public void removeReaction(UUID userId, Long commentId) {
        CommunityCommentReaction reaction = communityCommentReactionRepository
            .findByUserIdAndCommentId(userId, commentId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "반응을 찾을 수 없습니다."));
        
        communityCommentReactionRepository.delete(reaction);
    }
    
    @Override
    public long getLikeCount(Long commentId) {
        return communityCommentReactionRepository.countByCommentIdAndReactionType(commentId, ReactionType.LIKE);
    }
    
    @Override
    public long getDislikeCount(Long commentId) {
        return communityCommentReactionRepository.countByCommentIdAndReactionType(commentId, ReactionType.DISLIKE);
    }
    
    @Override
    public ReactionType getUserReaction(UUID userId, Long commentId) {
        return communityCommentReactionRepository
            .findByUserIdAndCommentId(userId, commentId)
            .map(CommunityCommentReaction::getReactionType)
            .orElse(null);
    }
}
