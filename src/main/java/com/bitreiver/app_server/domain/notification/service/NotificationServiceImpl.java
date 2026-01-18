package com.bitreiver.app_server.domain.notification.service;

import com.bitreiver.app_server.domain.notification.dto.NotificationResponse;
import com.bitreiver.app_server.domain.notification.dto.NotificationStatsResponse;
import com.bitreiver.app_server.domain.notification.entity.Notification;
import com.bitreiver.app_server.domain.notification.enums.NotificationType;
import com.bitreiver.app_server.domain.notification.repository.NotificationRepository;
import com.bitreiver.app_server.global.common.dto.PageResponse;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationSseService notificationSseService;
    
    @Override
    @Transactional
    public NotificationResponse createNotification(UUID userId, NotificationType type, String title, String content, String metadata) {
        Notification notification = Notification.builder()
            .userId(userId)
            .type(type)
            .title(title)
            .content(content)
            .metadata(metadata)
            .read(false)
            .createdAt(LocalDateTime.now())
            .build();
        
        Notification saved = notificationRepository.save(notification);
        
        // SSE로 실시간 전송
        notificationSseService.sendNotification(userId, NotificationResponse.from(saved));
        
        return NotificationResponse.from(saved);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(UUID userId, Boolean read, NotificationType type, org.springframework.data.domain.Pageable pageable) {
        Page<Notification> notificationPage;
        
        if (read != null && type != null) {
            // 읽음 상태와 타입 모두 필터링
            notificationPage = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
            if (read) {
                // 읽은 알림만 필터링 (추가 필터링 필요)
                notificationPage = notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, true, pageable);
            }
        } else if (read != null) {
            notificationPage = notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, read, pageable);
        } else if (type != null) {
            notificationPage = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        } else {
            notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        
        return PageResponse.of(
            notificationPage.getContent().stream()
                .map(NotificationResponse::from)
                .collect(Collectors.toList()),
            notificationPage.getNumber(),
            notificationPage.getSize(),
            notificationPage.getTotalElements()
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public NotificationStatsResponse getUnreadCount(UUID userId) {
        Long count = notificationRepository.countUnreadByUserId(userId);
        return NotificationStatsResponse.of(count);
    }
    
    @Override
    @Transactional
    public void markAsRead(Long notificationId, UUID userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId, LocalDateTime.now());
        if (updated == 0) {
            throw new CustomException(ErrorCode.NOT_FOUND, "알림을 찾을 수 없습니다.");
        }
    }
    
    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        int updated = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }
}
