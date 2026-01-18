package com.bitreiver.app_server.domain.notification.entity;

import com.bitreiver.app_server.domain.notification.enums.NotificationType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications",
    indexes = {
        @Index(name = "idx_notification_user_id", columnList = "user_id"),
        @Index(name = "idx_notification_user_read", columnList = "user_id, read"),
        @Index(name = "idx_notification_created_at", columnList = "created_at")
    })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "read", nullable = false)
    @Builder.Default
    private Boolean read = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Type(JsonType.class)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;  // JSON 형태로 추가 데이터 저장 (예: {"link": "/articles/123", "relatedId": 123})
    
    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }
}