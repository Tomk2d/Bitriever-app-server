package com.bitreiver.app_server.domain.notification.controller;
import com.bitreiver.app_server.domain.notification.dto.NotificationRequest;
import com.bitreiver.app_server.domain.notification.dto.NotificationResponse;
import com.bitreiver.app_server.domain.notification.dto.NotificationStatsResponse;
import com.bitreiver.app_server.domain.notification.enums.NotificationType;
import com.bitreiver.app_server.domain.notification.service.NotificationService;
import com.bitreiver.app_server.global.common.dto.PageResponse;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 API")
@SecurityRequirement(name = "JWT")
public class NotificationController {
    private final NotificationService notificationService;
    
    @Operation(summary = "알림 생성", description = "새로운 알림을 생성합니다.")
    @PostMapping
    public ApiResponse<NotificationResponse> createNotification(
        Authentication authentication,
        @Valid @RequestBody NotificationRequest request
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        NotificationResponse notification = notificationService.createNotification(
            userId,
            request.getType(),
            request.getTitle(),
            request.getContent(),
            request.getMetadata()
        );
        return ApiResponse.success(notification, "알림이 생성되었습니다.");
    }
    
    @Operation(summary = "알림 목록 조회", description = "사용자의 알림 목록을 페이징하여 조회합니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> getNotifications(
        Authentication authentication,
        @Parameter(description = "읽음 상태 필터 (true: 읽음, false: 안읽음, null: 전체)")
        @RequestParam(value = "read", required = false) Boolean read,
        @Parameter(description = "알림 타입 필터")
        @RequestParam(value = "type", required = false) NotificationType type,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(value = "page", defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        PageResponse<NotificationResponse> notifications = notificationService.getNotifications(
            userId, read, type, pageable
        );
        
        return ApiResponse.success(notifications);
    }
    
    @Operation(summary = "읽지 않은 알림 개수 조회", description = "사용자의 읽지 않은 알림 개수를 조회합니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/unread-count")
    public ApiResponse<NotificationStatsResponse> getUnreadCount(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        NotificationStatsResponse stats = notificationService.getUnreadCount(userId);
        return ApiResponse.success(stats);
    }
    
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}/read")
    public ApiResponse<String> markAsRead(
        Authentication authentication,
        @Parameter(name = "id", description = "알림 ID", required = true, in = ParameterIn.PATH)
        @PathVariable(value = "id") Long id
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        notificationService.markAsRead(id, userId);
        return ApiResponse.success("알림이 읽음 처리되었습니다.");
    }
    
    @Operation(summary = "전체 알림 읽음 처리", description = "사용자의 모든 알림을 읽음 처리합니다.")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/read-all")
    public ApiResponse<String> markAllAsRead(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        notificationService.markAllAsRead(userId);
        return ApiResponse.success("모든 알림이 읽음 처리되었습니다.");
    }
}
