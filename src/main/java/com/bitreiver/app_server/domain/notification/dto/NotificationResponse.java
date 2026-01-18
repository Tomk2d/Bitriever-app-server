package com.bitreiver.app_server.domain.notification.dto;

import com.bitreiver.app_server.domain.notification.entity.Notification;
import com.bitreiver.app_server.domain.notification.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private UUID userId;
    private NotificationType type;
    private String title;
    private String content;
    private Boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private String metadata;
    
    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .userId(notification.getUserId())
            .type(notification.getType())
            .title(notification.getTitle())
            .content(notification.getContent())
            .read(notification.getRead())
            .readAt(notification.getReadAt())
            .createdAt(notification.getCreatedAt())
            .metadata(notification.getMetadata())
            .build();
    }
}
