package com.bitreiver.app_server.domain.notification.service;

import com.bitreiver.app_server.domain.notification.dto.NotificationResponse;
import com.bitreiver.app_server.domain.notification.dto.TradeEvaluationEventPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSseService {
    
    // userId -> SseEmitter 매핑
    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    
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
            log.warn("SSE 연결이 없습니다. userId={}", userId);
            return;
        }
        
        try {
            // ObjectMapper를 사용하여 JSON으로 직렬화
            String jsonData = objectMapper.writeValueAsString(notification);
            
            emitter.send(SseEmitter.event()
                .name("notification")
                .data(jsonData));
        } catch (IOException e) {
            log.error("SSE 알림 전송 실패: userId={}, error={}", userId, e.getMessage(), e);
            emitters.remove(userId);
            emitter.completeWithError(e);
        } catch (Exception e) {
            log.error("SSE 알림 직렬화 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
    
    /**
     * 매매 분석 요청 완료(성공/실패) 시 해당 사용자 SSE로 알림 전송.
     * 날짜·시간·심볼 포함.
     */
    public void sendTradeEvaluationEvent(UUID userId, TradeEvaluationEventPayload payload) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) {
            log.warn("SSE 연결이 없습니다. 매매 분석 이벤트 미전송: userId={}, tradeId={}", userId, payload.getTradeId());
            return;
        }
        try {
            String jsonData = objectMapper.writeValueAsString(payload);
            emitter.send(SseEmitter.event()
                .name("trade-evaluation")
                .data(jsonData));
        } catch (IOException e) {
            log.error("SSE 매매 분석 이벤트 전송 실패: userId={}, tradeId={}, error={}", userId, payload.getTradeId(), e.getMessage(), e);
            emitters.remove(userId);
            emitter.completeWithError(e);
        } catch (Exception e) {
            log.error("SSE 매매 분석 이벤트 직렬화 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    public void broadcastNotification(NotificationResponse notification) {
        try {
            // ObjectMapper를 사용하여 JSON으로 직렬화
            String jsonData = objectMapper.writeValueAsString(notification);
            
            emitters.forEach((userId, emitter) -> {
                try {
                    emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(jsonData));
                } catch (IOException e) {
                    log.error("SSE 브로드캐스트 알림 전송 실패: userId={}, error={}", userId, e.getMessage(), e);
                    emitters.remove(userId);
                    emitter.completeWithError(e);
                }
            });
        } catch (Exception e) {
            log.error("SSE 브로드캐스트 알림 직렬화 실패: error={}", e.getMessage(), e);
        }
    }
    
    public int getConnectionCount() {
        return emitters.size();
    }
}
