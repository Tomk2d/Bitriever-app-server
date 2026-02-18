package com.bitreiver.app_server.domain.notification.controller;

import com.bitreiver.app_server.domain.notification.service.NotificationSseService;
import com.bitreiver.app_server.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
public class NotificationSseController {
    private final NotificationSseService notificationSseService;
    private final JwtTokenProvider jwtTokenProvider;
    
    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(@RequestParam("token") String token) {
        try {
            // 토큰 검증
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("SSE 연결 거절: JWT 만료 또는 유효하지 않음");
                SseEmitter emitter = new SseEmitter(0L);
                emitter.complete();
                return emitter;
            }
            
            // 토큰에서 userId 추출
            UUID userId = jwtTokenProvider.getUserIdFromToken(token);
            
            return notificationSseService.createConnection(userId);
            
        } catch (Exception e) {
            log.warn("SSE 연결 실패: {}", e.getMessage());
            SseEmitter emitter = new SseEmitter(0L);
            emitter.complete();
            return emitter;
        }
    }
}
