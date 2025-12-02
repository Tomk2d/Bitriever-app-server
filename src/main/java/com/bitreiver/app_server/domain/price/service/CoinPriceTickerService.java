package com.bitreiver.app_server.domain.price.service;

import com.bitreiver.app_server.domain.price.dto.CoinTickerPriceDto;
import com.bitreiver.app_server.domain.price.dto.UpbitTickerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoinPriceTickerService {
    private final WebClient upbitTickerWebClient;
    private final CoinPriceWebSocketService coinPriceWebSocketService;

    // 각 quote_currency별로 진행 중인 요청 추적 (이전 요청 취소용)
    private final Map<String, Disposable> tickerDisposables = new ConcurrentHashMap<>();
    
    // 서버에서 관리하는 모든 코인 가격 상태 (market -> CoinTickerPriceDto)
    private final Map<String, CoinTickerPriceDto> coinPriceCache = new ConcurrentHashMap<>();
    
    // 마지막 브로드캐스트 시간 추적용 타임스탬프 (지연된 응답 처리용)
    private final Map<String, Long> lastBroadcastTimestamp = new ConcurrentHashMap<>();

    /**
     * KRW 마켓 조회 - 10초마다 실행 (0초부터 시작)
     * 비동기로 실행되어 스케줄러 스레드를 블로킹하지 않음
     */
    @Async("coinPriceExecutor")
    @Scheduled(fixedRate = 10000, initialDelay = 0)
    public void fetchKrwMarketPrices() {
        fetchMarketPrices("KRW");
    }

    /**
     * BTC 마켓 조회 - 10초마다 실행 (3초 지연으로 분산)
     * 비동기로 실행되어 스케줄러 스레드를 블로킹하지 않음
     */
    @Async("coinPriceExecutor")
    @Scheduled(fixedRate = 10000, initialDelay = 3000)
    public void fetchBtcMarketPrices() {
        fetchMarketPrices("BTC");
    }
    
    /**
     * USDT 마켓 조회 - 10초마다 실행 (6초 지연으로 분산)
     * 비동기로 실행되어 스케줄러 스레드를 블로킹하지 않음
     */
    @Async("coinPriceExecutor")
    @Scheduled(fixedRate = 10000, initialDelay = 6000)
    public void fetchUsdtMarketPrices() {
        fetchMarketPrices("USDT");
    }
    
    /**
     * 특정 quote_currency의 모든 마켓 주가 조회
     * 10초마다 실행되며, 이전 요청이 진행 중이면 취소
     * 모든 코인을 수집한 후 한 번에 브로드캐스팅
     */
    private void fetchMarketPrices(String quoteCurrency) {
        // 이전 요청이 진행 중이면 취소 (10초마다 새 요청이 오므로 이전 요청은 불필요)
        Disposable previousRequest = tickerDisposables.get(quoteCurrency);
        if (previousRequest != null && !previousRequest.isDisposed()) {
            previousRequest.dispose();
        }
        
        // 새 요청 시작
        Disposable newRequest = upbitTickerWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/v1/ticker/all")
                .queryParam("quote_currencies", quoteCurrency)
                .build())
            .retrieve()
            .bodyToFlux(UpbitTickerResponse.class)
            .map(CoinTickerPriceDto::from)
            .collectList()  // 모든 코인을 리스트로 수집
            .doOnNext(coinPrices -> {
                // 변동된 가격만 필터링하여 브로드캐스팅
                List<CoinTickerPriceDto> changedPrices = filterChangedPrices(coinPrices);
                
                // 서버 상태 업데이트 (모든 가격 저장)
                updatePriceCache(coinPrices);
                
                // 변동된 가격만 WebSocket으로 전송
                if (!changedPrices.isEmpty()) {
                    coinPriceWebSocketService.broadcastAllCoinPrices(changedPrices);
                }
            })
            .doOnError(error -> log.error("{} 마켓 주가 조회 실패", quoteCurrency, error))
            .onErrorResume(error -> {
                log.error("{} 마켓 주가 조회 중 에러 발생", quoteCurrency, error);
                return Mono.empty();
            })
            .doFinally(signalType -> {
                tickerDisposables.remove(quoteCurrency);
            })
            .subscribe();
        
        // 진행 중인 요청 추적
        tickerDisposables.put(quoteCurrency, newRequest);
    }
    
    /**
     * 서버 상태 업데이트: 모든 가격을 캐시에 저장
     */
    private void updatePriceCache(List<CoinTickerPriceDto> prices) {
        for (CoinTickerPriceDto price : prices) {
            coinPriceCache.put(price.getMarket(), price);
        }
    }
    
    /**
     * 변동된 가격만 필터링: 타임스탬프가 변경된 가격만 반환
     * 지연된 응답 처리: 최신 데이터만 포함
     */
    private List<CoinTickerPriceDto> filterChangedPrices(List<CoinTickerPriceDto> prices) {
        List<CoinTickerPriceDto> changedPrices = new ArrayList<>();
        
        for (CoinTickerPriceDto price : prices) {
            String market = price.getMarket();
            Long lastTimestamp = lastBroadcastTimestamp.get(market);
            Long currentTimestamp = price.getTimestamp();
            
            // 타임스탬프가 더 최신이거나 첫 브로드캐스팅인 경우 (변동된 가격)
            if (lastTimestamp == null || currentTimestamp > lastTimestamp) {
                changedPrices.add(price);
                lastBroadcastTimestamp.put(market, currentTimestamp);
            }
        }
        
        return changedPrices;
    }
    
    /**
     * 전체 가격 조회 (클라이언트 최초 연결 시 사용)
     * @return 모든 코인의 현재 가격
     */
    public List<CoinTickerPriceDto> getAllCoinPrices() {
        return new ArrayList<>(coinPriceCache.values());
    }
}