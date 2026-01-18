package com.bitreiver.app_server.domain.notification.repository;

import com.bitreiver.app_server.domain.notification.entity.Notification;
import com.bitreiver.app_server.domain.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 사용자별 알림 조회 (페이징, 최신순)
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    
    // 사용자별 읽지 않은 알림 조회
    Page<Notification> findByUserIdAndReadOrderByCreatedAtDesc(UUID userId, Boolean read, Pageable pageable);
    
    // 사용자별 타입별 알림 조회
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(UUID userId, NotificationType type, Pageable pageable);
    
    // 읽지 않은 알림 개수
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.read = false")
    Long countUnreadByUserId(@Param("userId") UUID userId);
    
    // 특정 알림 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt WHERE n.id = :id AND n.userId = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);
    
    // 사용자의 모든 알림 읽음 처리
    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :readAt WHERE n.userId = :userId AND n.read = false")
    int markAllAsRead(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt);
}
