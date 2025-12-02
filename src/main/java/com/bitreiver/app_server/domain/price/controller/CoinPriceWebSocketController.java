package com.bitreiver.app_server.domain.price.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping; 
import org.springframework.messaging.handler.annotation.SendTo; 
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class CoinPriceWebSocketController {
    
    /**
     * 클라이언트가 /app/coins/subscribe로 메시지를 보내면 호출
     * 응답은 /topic/coins/subscribed로 전송
     * 인증 없이 누구나 사용 가능
     */
    @MessageMapping("/coins/subscribe")
    @SendTo("/topic/coins/subscribed")
    public String handleSubscribe() {
        log.info("코인 주가 구독 신청");
        return "구독 완료";
    }
}
