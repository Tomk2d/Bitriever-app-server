package com.bitreiver.app_server.domain.community.service;

import com.bitreiver.app_server.domain.community.dto.CommunityCommentRequest;
import com.bitreiver.app_server.domain.community.dto.CommunityCommentResponse;
import com.bitreiver.app_server.domain.community.entity.CommunityComment;
import com.bitreiver.app_server.domain.community.enums.ReactionType;
import com.bitreiver.app_server.domain.community.repository.CommunityCommentRepository;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityCommentServiceImpl implements CommunityCommentService {
    
    private final CommunityCommentRepository communityCommentRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final CommunityCommentReactionService communityCommentReactionService;
    
    @Override
    @Transactional
    public CommunityCommentResponse createComment(UUID userId, Integer communityId, CommunityCommentRequest request) {
        // 게시글 존재 확인
        if (!communityRepository.existsById(communityId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        
        Long parentId = request.getParentId();
        
        // 대댓글인 경우 부모 댓글이 최상위 댓글인지 확인 (2단계 깊이 제한)
        if (parentId != null) {
            CommunityComment parentComment = communityCommentRepository.findById(parentId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "부모 댓글을 찾을 수 없습니다."));
            
            // 부모 댓글이 해당 게시글의 댓글이 아닌 경우
            if (!parentComment.getCommunityId().equals(communityId)) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "부모 댓글이 해당 게시글의 댓글이 아닙니다.");
            }
            
            // 부모 댓글의 parentId가 null이 아니면 대댓글의 대댓글은 불가
            if (parentComment.getParentId() != null) {
                throw new CustomException(ErrorCode.BAD_REQUEST, "대댓글의 대댓글은 작성할 수 없습니다.");
            }
        }
        
        CommunityComment comment = CommunityComment.builder()
            .communityId(communityId)
            .userId(userId)
            .parentId(parentId)
            .content(request.getContent())
            .deleted(false)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        
        communityCommentRepository.save(comment);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        return CommunityCommentResponse.from(comment, user.getNickname(), 0L, 0L, null, List.of());
    }
    
    @Override
    public PageResponse<CommunityCommentResponse> getCommentsByCommunityId(Integer communityId, UUID userId, int page, int size) {
        // 게시글 존재 확인
        if (!communityRepository.existsById(communityId)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CommunityComment> topLevelComments = communityCommentRepository
            .findByCommunityIdAndParentIdIsNullOrderByCreatedAtDesc(communityId, pageable);
        
        // 최상위 댓글 ID 목록
        List<Long> topLevelCommentIds = topLevelComments.getContent().stream()
            .map(CommunityComment::getId)
            .collect(Collectors.toList());
        
        // 모든 대댓글 조회
        Map<Long, List<CommunityComment>> repliesMap = topLevelCommentIds.stream()
            .collect(Collectors.toMap(
                parentId -> parentId,
                parentId -> communityCommentRepository.findByParentIdOrderByCreatedAtAsc(parentId)
            ));
        
        // 사용자 정보 조회 (최상위 댓글 + 대댓글 작성자)
        List<UUID> allUserIds = topLevelComments.getContent().stream()
            .map(CommunityComment::getUserId)
            .collect(Collectors.toList());
        repliesMap.values().forEach(replies -> 
            replies.forEach(reply -> allUserIds.add(reply.getUserId()))
        );
        
        Map<UUID, String> userNicknameMap = userRepository.findAllById(allUserIds).stream()
            .collect(Collectors.toMap(User::getId, User::getNickname));
        
        // 최상위 댓글을 응답으로 변환
        List<CommunityCommentResponse> content = topLevelComments.getContent().stream()
            .map(comment -> {
                String userNickname = userNicknameMap.get(comment.getUserId());
                long likeCount = communityCommentReactionService.getLikeCount(comment.getId());
                long dislikeCount = communityCommentReactionService.getDislikeCount(comment.getId());
                ReactionType userReaction = userId != null ? 
                    communityCommentReactionService.getUserReaction(userId, comment.getId()) : null;
                
                // 대댓글 변환
                List<CommunityCommentResponse> replies = repliesMap.getOrDefault(comment.getId(), List.of()).stream()
                    .map(reply -> {
                        String replyUserNickname = userNicknameMap.get(reply.getUserId());
                        long replyLikeCount = communityCommentReactionService.getLikeCount(reply.getId());
                        long replyDislikeCount = communityCommentReactionService.getDislikeCount(reply.getId());
                        ReactionType replyUserReaction = userId != null ? 
                            communityCommentReactionService.getUserReaction(userId, reply.getId()) : null;
                        
                        return CommunityCommentResponse.from(
                            reply, replyUserNickname, replyLikeCount, replyDislikeCount, replyUserReaction, List.of()
                        );
                    })
                    .collect(Collectors.toList());
                
                return CommunityCommentResponse.from(
                    comment, userNickname, likeCount, dislikeCount, userReaction, replies
                );
            })
            .collect(Collectors.toList());
        
        return PageResponse.of(content, page, size, topLevelComments.getTotalElements());
    }
    
    @Override
    @Transactional
    public CommunityCommentResponse updateComment(UUID userId, Long id, CommunityCommentRequest request) {
        CommunityComment comment = communityCommentRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        
        if (comment.getDeleted()) {
            throw new CustomException(ErrorCode.BAD_REQUEST, "삭제된 댓글은 수정할 수 없습니다.");
        }
        
        if (request.getContent() != null) {
            comment.setContent(request.getContent());
        }
        
        comment.setUpdatedAt(LocalDateTime.now());
        communityCommentRepository.save(comment);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        long likeCount = communityCommentReactionService.getLikeCount(id);
        long dislikeCount = communityCommentReactionService.getDislikeCount(id);
        ReactionType userReaction = communityCommentReactionService.getUserReaction(userId, id);
        
        // 대댓글 조회
        List<CommunityComment> replies = communityCommentRepository.findByParentIdOrderByCreatedAtAsc(id);
        List<CommunityCommentResponse> replyResponses = replies.stream()
            .map(reply -> {
                User replyUser = userRepository.findById(reply.getUserId()).orElse(null);
                long replyLikeCount = communityCommentReactionService.getLikeCount(reply.getId());
                long replyDislikeCount = communityCommentReactionService.getDislikeCount(reply.getId());
                ReactionType replyUserReaction = communityCommentReactionService.getUserReaction(userId, reply.getId());
                
                return CommunityCommentResponse.from(
                    reply,
                    replyUser != null ? replyUser.getNickname() : null,
                    replyLikeCount,
                    replyDislikeCount,
                    replyUserReaction,
                    List.of()
                );
            })
            .collect(Collectors.toList());
        
        return CommunityCommentResponse.from(comment, user.getNickname(), likeCount, dislikeCount, userReaction, replyResponses);
    }
    
    @Override
    @Transactional
    public void deleteComment(UUID userId, Long id) {
        CommunityComment comment = communityCommentRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "댓글을 찾을 수 없습니다."));
        
        // 대댓글이 있는지 확인
        List<CommunityComment> replies = communityCommentRepository.findByParentIdOrderByCreatedAtAsc(id);
        
        if (!replies.isEmpty()) {
            // 대댓글이 있으면 soft delete
            comment.setDeleted(true);
            comment.setUpdatedAt(LocalDateTime.now());
            communityCommentRepository.save(comment);
        } else {
            // 대댓글이 없으면 hard delete
            communityCommentRepository.delete(comment);
        }
    }
    
    @Override
    public long getCommentCount(Integer communityId) {
        return communityCommentRepository.countByCommunityId(communityId);
    }
}
