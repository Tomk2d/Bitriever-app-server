package com.bitreiver.app_server.domain.sync.controller;

import com.bitreiver.app_server.domain.diary.service.DiaryService;
import com.bitreiver.app_server.domain.notification.enums.NotificationType;
import com.bitreiver.app_server.domain.notification.service.NotificationService;
import com.bitreiver.app_server.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/callback")
@RequiredArgsConstructor
@Tag(name = "Sync Callback", description = "fetch-server에서 호출하는 동기화 완료 콜백 API")
@Hidden // Swagger에서 숨김 (내부용)
public class SyncCallbackController {
    
    private final NotificationService notificationService;
    private final DiaryService diaryService;
    
    @Operation(summary = "동기화 완료 콜백", description = "fetch-server에서 동기화 완료 시 호출하는 콜백 API")
    @PostMapping("/sync-complete")
    public ResponseEntity<ApiResponse<Object>> onSyncComplete(@RequestBody Map<String, Object> callbackData) {
        try {
            String userIdStr = (String) callbackData.get("user_id");
            String syncType = (String) callbackData.get("sync_type");
            Boolean success = (Boolean) callbackData.get("success");
            String message = (String) callbackData.get("message");
            
            UUID userId = UUID.fromString(userIdStr);
            
            log.info("동기화 콜백 수신: userId={}, syncType={}, success={}, message={}", 
                userId, syncType, success, message);
            
            if ("ASSET".equals(syncType)) {
                handleAssetSyncCallback(userId, callbackData, success, message);
            } else if ("TRADING_HISTORY".equals(syncType)) {
                handleTradingHistorySyncCallback(userId, callbackData, success, message);
            }
            
            return ResponseEntity.ok(ApiResponse.success(
                Map.of("received", true),
                "콜백 처리 완료"));
                
        } catch (Exception e) {
            log.error("동기화 콜백 처리 실패: error={}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.success(
                Map.of("received", true, "error", e.getMessage()),
                "콜백 수신됨 (처리 중 오류 발생)"));
        }
    }
    
    private void handleAssetSyncCallback(UUID userId, Map<String, Object> callbackData, 
                                          Boolean success, String message) {
        try {
            if (Boolean.TRUE.equals(success)) {
                notificationService.createNotification(
                    userId,
                    NotificationType.USER_UPDATE,
                    "자산 동기화 완료",
                    "거래소 자산 정보가 성공적으로 동기화되었습니다.",
                    null
                );
            } else {
                notificationService.createNotification(
                    userId,
                    NotificationType.USER_UPDATE,
                    "자산 동기화 실패",
                    message != null ? message : "거래소 자산 동기화에 실패했습니다.",
                    null
                );
            }
        } catch (Exception e) {
            log.error("자산 동기화 알림 생성 실패: userId={}, error={}", userId, e.getMessage());
        }
    }
    
    private void handleTradingHistorySyncCallback(UUID userId, Map<String, Object> callbackData, 
                                                   Boolean success, String message) {
        try {
            // 매매일지 자동 생성
            @SuppressWarnings("unchecked")
            List<Integer> savedIds = (List<Integer>) callbackData.get("saved_ids");
            if (savedIds != null && !savedIds.isEmpty()) {
                try {
                    int createdCount = diaryService.createDiariesForTradingHistories(savedIds);
                    log.info("매매일지 자동 생성 완료: userId={}, count={}", userId, createdCount);
                } catch (Exception e) {
                    log.error("매매일지 자동 생성 실패: userId={}, error={}", userId, e.getMessage());
                }
            }
            
            // 알림 생성
            @SuppressWarnings("unchecked")
            List<String> successExchanges = (List<String>) callbackData.get("success_exchanges");
            @SuppressWarnings("unchecked")
            List<String> failedExchanges = (List<String>) callbackData.get("failed_exchanges");
            
            if (Boolean.TRUE.equals(success)) {
                String notifyMessage = (successExchanges != null ? successExchanges.size() : 0) + 
                    "개 거래소 매매내역 동기화 완료";
                if (failedExchanges != null && !failedExchanges.isEmpty()) {
                    notifyMessage += " (" + failedExchanges.size() + "개 실패)";
                }
                
                notificationService.createNotification(
                    userId,
                    NotificationType.USER_UPDATE,
                    "매매내역 동기화 완료",
                    notifyMessage,
                    null
                );
            } else {
                notificationService.createNotification(
                    userId,
                    NotificationType.USER_UPDATE,
                    "매매내역 동기화 실패",
                    message != null ? message : "매매내역 동기화에 실패했습니다. 거래소 토큰을 확인해주세요.",
                    null
                );
            }
        } catch (Exception e) {
            log.error("거래내역 동기화 알림 생성 실패: userId={}, error={}", userId, e.getMessage());
        }
    }
}
