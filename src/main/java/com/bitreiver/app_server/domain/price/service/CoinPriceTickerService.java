package com.bitreiver.app_server.domain.price.service;

import com.bitreiver.app_server.domain.coin.entity.Coin;
import com.bitreiver.app_server.domain.coin.repository.CoinRepository;
import com.bitreiver.app_server.domain.price.dto.CoinTickerPriceDto;
import com.bitreiver.app_server.domain.price.dto.CoinoneTickerResponse;
import com.bitreiver.app_server.domain.price.dto.UpbitTickerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CoinPriceTickerService {
    private final WebClient upbitTickerWebClient;
    private final WebClient coinoneTickerWebClient;
    private final CoinPriceWebSocketService coinPriceWebSocketService;
    private final CoinRepository coinRepository;
    
    public CoinPriceTickerService(
            @Qualifier("upbitTickerWebClient") WebClient upbitTickerWebClient,
            @Qualifier("coinoneTickerWebClient") WebClient coinoneTickerWebClient,
            CoinPriceWebSocketService coinPriceWebSocketService,
            CoinRepository coinRepository) {
        this.upbitTickerWebClient = upbitTickerWebClient;
        this.coinoneTickerWebClient = coinoneTickerWebClient;
        this.coinPriceWebSocketService = coinPriceWebSocketService;
        this.coinRepository = coinRepository;
    }

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
     * 코인원 KRW 마켓 조회 - 10초마다 실행 (5초 지연으로 시작)
     * 업비트와 시간이 겹치지 않도록 설정 (업비트: 0초, 3초, 6초)
     * 비동기로 실행되어 스케줄러 스레드를 블로킹하지 않음
     */
    @Async("coinPriceExecutor")
    @Scheduled(fixedRate = 10000, initialDelay = 5000)
    public void fetchCoinoneMarketPrices() {
        // 이전 요청이 진행 중이면 취소
        Disposable previousRequest = tickerDisposables.get("COINONE-KRW");
        if (previousRequest != null && !previousRequest.isDisposed()) {
            previousRequest.dispose();
        }
        
        // DB에서 활성 코인원 코인만 조회
        List<Coin> activeCoinoneCoins = coinRepository.findByExchangeAndIsActive("COINONE", true);
        Set<String> activeCoinoneSymbols = activeCoinoneCoins.stream()
            .map(Coin::getSymbol)
            .collect(Collectors.toSet());
        
        if (activeCoinoneSymbols.isEmpty()) {
            log.debug("활성 코인원 코인이 없습니다.");
            return;
        }
        
        // 새 요청 시작
        Disposable newRequest = coinoneTickerWebClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/public/v2/ticker_new/KRW")
                .queryParam("additional_data", true)
                .build())
            .retrieve()
            .bodyToMono(CoinoneTickerResponse.class)
            .map(response -> {
                // API 응답의 tickers 중에서 DB에 저장된 활성 코인만 필터링
                List<CoinTickerPriceDto> coinPrices = new ArrayList<>();
                
                if (response.getTickers() != null) {
                    // 대소문자 무시 비교를 위한 Map 생성 (targetCurrency -> symbol 매핑)
                    Map<String, String> targetCurrencyToSymbol = new HashMap<>();
                    for (String symbol : activeCoinoneSymbols) {
                        // 대소문자 무시하여 매핑
                        targetCurrencyToSymbol.put(symbol.toUpperCase(), symbol);
                        targetCurrencyToSymbol.put(symbol.toLowerCase(), symbol);
                        targetCurrencyToSymbol.put(symbol, symbol); // 원본도 포함
                    }
                    
                    for (CoinoneTickerResponse.CoinoneTicker ticker : response.getTickers()) {
                        String targetCurrency = ticker.getTargetCurrency();
                        if (targetCurrency == null || targetCurrency.isEmpty()) {
                            continue;
                        }
                        
                        // 대소문자 무시하여 매칭 시도
                        String matchedSymbol = targetCurrencyToSymbol.get(targetCurrency.toUpperCase());
                        if (matchedSymbol == null) {
                            matchedSymbol = targetCurrencyToSymbol.get(targetCurrency.toLowerCase());
                        }
                        if (matchedSymbol == null) {
                            matchedSymbol = targetCurrencyToSymbol.get(targetCurrency);
                        }
                        
                        // DB에 저장된 활성 코인인지 확인
                        if (matchedSymbol != null || activeCoinoneSymbols.contains(targetCurrency)) {
                            CoinTickerPriceDto priceDto = CoinTickerPriceDto.from(ticker);
                            coinPrices.add(priceDto);
                        }
                    }
                }
                
                return coinPrices;
            })
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
            .doOnError(error -> log.error("코인원 KRW 마켓 주가 조회 실패", error))
            .onErrorResume(error -> {
                log.error("코인원 KRW 마켓 주가 조회 중 에러 발생", error);
                return Mono.empty();
            })
            .doFinally(signalType -> {
                tickerDisposables.remove("COINONE-KRW");
            })
            .subscribe();
        
        // 진행 중인 요청 추적
        tickerDisposables.put("COINONE-KRW", newRequest);
    }
    
    /**
     * 전체 가격 조회 (클라이언트 최초 연결 시 사용)
     * @return 모든 코인의 현재 가격
     */
    public List<CoinTickerPriceDto> getAllCoinPrices() {
        return new ArrayList<>(coinPriceCache.values());
    }
    
    /**
     * 거래소별 가격 조회 (클라이언트 최초 연결 시 사용)
     * @param exchange 거래소 이름 (UPBIT, COINONE 등)
     * @return 해당 거래소의 코인 현재 가격
     */
    public List<CoinTickerPriceDto> getCoinPricesByExchange(String exchange) {
        // DB에서 활성 코인 조회하여 marketCode -> exchange 매핑 생성
        List<Coin> activeCoins = coinRepository.findAllByIsActive(true);
        Map<String, String> marketCodeToExchange = new HashMap<>();
        
        for (Coin coin : activeCoins) {
            if (coin.getMarketCode() != null) {
                // marketCode를 대문자로 변환하여 매핑 (일관성 유지)
                marketCodeToExchange.put(coin.getMarketCode(), coin.getExchange().toUpperCase());
            }
        }
        
        // 캐시에서 해당 거래소의 가격만 필터링
        List<CoinTickerPriceDto> filteredPrices = new ArrayList<>();
        String exchangeUpper = exchange != null ? exchange.toUpperCase() : null;
        
        // 대소문자 무시 비교를 위한 Map 생성 (대소문자 변형 포함)
        Map<String, String> marketCodeMap = new HashMap<>();
        for (String marketCode : marketCodeToExchange.keySet()) {
            marketCodeMap.put(marketCode, marketCode);
            marketCodeMap.put(marketCode.toUpperCase(), marketCode);
            marketCodeMap.put(marketCode.toLowerCase(), marketCode);
        }
        
        for (CoinTickerPriceDto price : coinPriceCache.values()) {
            String market = price.getMarket();
            if (market == null || market.isEmpty()) {
                continue;
            }
            
            // market은 marketCode와 동일한 형식 (업비트: "KRW-BTC", 코인원: "BTC-KRW")
            // 대소문자 무시하여 매칭 시도
            String matchedMarketCode = marketCodeMap.get(market);
            if (matchedMarketCode == null) {
                matchedMarketCode = marketCodeMap.get(market.toUpperCase());
            }
            if (matchedMarketCode == null) {
                matchedMarketCode = marketCodeMap.get(market.toLowerCase());
            }
            
            String exchangeFromDb = null;
            if (matchedMarketCode != null) {
                exchangeFromDb = marketCodeToExchange.get(matchedMarketCode);
            }
            
            // exchange 파라미터와 일치하는 경우만 추가
            if (exchangeUpper != null && exchangeUpper.equals(exchangeFromDb)) {
                filteredPrices.add(price);
            }
        }
        
        return filteredPrices;
    }
}