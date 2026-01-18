package com.bitreiver.app_server.domain.notification.service;

import com.bitreiver.app_server.domain.notification.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class NotificationSseService {
    
    // userId -> SseEmitter 매핑
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L; // 30분
    
    public SseEmitter createConnection(UUID userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        
        try {
            emitter.send(SseEmitter.event()
                .name("connect")
                .data("SSE 연결이 성공했습니다."));
        } catch (IOException e) {
            emitter.completeWithError(e);
            return emitter;
        }
        
        emitter.onCompletion(() -> {
            emitters.remove(userId);
        });
        
        emitter.onTimeout(() -> {
            emitters.remove(userId);
            emitter.complete();
        });
        
        emitter.onError((ex) -> {
            emitters.remove(userId);
        });
        
        emitters.put(userId, emitter);
        
        return emitter;
    }
    
    public void sendNotification(UUID userId, NotificationResponse notification) {
        SseEmitter emitter = emitters.get(userId);
        
        if (emitter == null) {
            return;
        }
        
        try {
            emitter.send(SseEmitter.event()
                .name("notification")
                .data(notification));
        } catch (IOException e) {
            emitters.remove(userId);
            emitter.completeWithError(e);
        }
    }
    
    public void broadcastNotification(NotificationResponse notification) {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                    .name("notification")
                    .data(notification));
            } catch (IOException e) {
                emitters.remove(userId);
                emitter.completeWithError(e);
            }
        });
    }
    
    public int getConnectionCount() {
        return emitters.size();
    }
}
