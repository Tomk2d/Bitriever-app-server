package com.bitreiver.app_server.domain.notification.service;

import com.bitreiver.app_server.domain.notification.dto.NotificationResponse;
import com.bitreiver.app_server.domain.notification.dto.NotificationStatsResponse;
import com.bitreiver.app_server.domain.notification.enums.NotificationType;
import com.bitreiver.app_server.global.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    NotificationResponse createNotification(UUID userId, NotificationType type, String title, String content, String metadata);
    PageResponse<NotificationResponse> getNotifications(UUID userId, Boolean read, NotificationType type, Pageable pageable);
    NotificationStatsResponse getUnreadCount(UUID userId);
    void markAsRead(Long notificationId, UUID userId);
    void markAllAsRead(UUID userId);
}
