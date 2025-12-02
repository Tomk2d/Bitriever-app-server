package com.bitreiver.app_server.global.config;

import org.springframework.context.annotation.Configuration;

import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    /**
     * 메시지 브로커 설정 - 멀티캐스팅 지원
     * enableSimpleBroker: 메모리 기반 브로커 활성화
     * /topic: 1:N 브로드캐스팅 (멀티캐스팅) - 여러 클라이언트에게 동시 전송
     * /queue: 1:1 메시징
     */

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        // 클라이언트에서 보내는 메시지의 접두사를 설정
        config.setApplicationDestinationPrefixes("/app");
        // 메모리 브로커 활성화 - 멀티캐스팅 지원
        config.enableSimpleBroker("/topic", "/queue");
        
    }
    
    /**
     * WebSocket 엔드포인트 등록: 클라이언트가 연결할 URL
     * 인증 없이 누구나 접근 가능
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/coins")
            .setAllowedOriginPatterns("*")  // CORS 설정 (프로덕션에서는 특정 도메인으로 제한)
            .withSockJS();  // SockJS 폴백 지원 (브라우저 호환성)
    }
}
