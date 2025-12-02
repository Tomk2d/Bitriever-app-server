package com.bitreiver.app_server.domain.price.service;
import com.bitreiver.app_server.domain.price.dto.CoinTickerPriceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoinPriceWebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 모든 코인 주가 리스트를 브로드캐스팅
     * /topic/coins/all을 구독한 모든 클라이언트에게 한 번에 전송
     * 
     * @param coinPrices 모든 코인 주가 리스트
     */
    public void broadcastAllCoinPrices(List<CoinTickerPriceDto> coinPrices) {
        messagingTemplate.convertAndSend("/topic/coins/all", coinPrices);
    }

    // ========== 멀티캐스팅 기능 (주석처리) ==========
    
    // /**
    //  * 특정 quote_currency의 모든 마켓 주가를 멀티캐스팅
    //  * /topic/coins/quote/KRW를 구독한 모든 클라이언트에게 전송
    //  */
    // public void broadcastQuoteCurrencyPrices(String quoteCurrency, CoinTickerPriceDto coinPrice) {
    //     String destination = "/topic/coins/quote/" + quoteCurrency;
    //     messagingTemplate.convertAndSend(destination, coinPrice);
    // }

    // /**
    //  * 특정 마켓의 주가를 구독자들에게 멀티캐스팅
    //  * /topic/coins/KRW-BTC를 구독한 모든 클라이언트에게 동시 전송
    //  */
    // public void broadcastCoinPrice(CoinTickerPriceDto coinPrice) {
    //     String destination = "/topic/coins/" + coinPrice.getMarket();
    //     messagingTemplate.convertAndSend(destination, coinPrice);
    //     log.debug("주가 멀티캐스팅: market={}, price={}", coinPrice.getMarket(), coinPrice.getTradePrice());
    // }
}
