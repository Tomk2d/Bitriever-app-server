package com.bitreiver.app_server.domain.asset.service;

import com.bitreiver.app_server.domain.asset.dto.AssetResponse;
import com.bitreiver.app_server.domain.asset.entity.Asset;
import com.bitreiver.app_server.domain.asset.repository.AssetRepository;
import com.bitreiver.app_server.domain.coin.dto.CoinResponse;
import com.bitreiver.app_server.domain.coin.entity.Coin;
import com.bitreiver.app_server.domain.coin.repository.CoinRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {
    
    private final AssetRepository assetRepository;
    private final CoinRepository coinRepository;
    private final RestTemplate restTemplate;
    
    @Value("${external.fetch.server.url}")
    private String fetchServerUrl;
    
    private static final String CALLBACK_URL = "/api/callback/sync-complete";
    
    @Override
    public List<AssetResponse> getUserAssets(UUID userId) {
        List<Asset> assets = assetRepository.findByUserId(userId);
        
        if (assets.isEmpty()) {
            return List.of();
        }
        
        return mapAssetsToResponses(assets);
    }
    
    @Override
    public List<AssetResponse> getUserAssetsByExchange(UUID userId, Short exchangeCode) {
        List<Asset> assets = assetRepository.findByUserIdAndExchangeCode(userId, exchangeCode);
        
        if (assets.isEmpty()) {
            return List.of();
        }
        
        return mapAssetsToResponses(assets);
    }
    
    private List<AssetResponse> mapAssetsToResponses(List<Asset> assets) {
        List<Integer> coinIds = assets.stream()
            .map(Asset::getCoinId)
            .filter(coinId -> coinId != null)
            .distinct()
            .toList();
        
        Map<Integer, CoinResponse> coinMap = Map.of();
        if (!coinIds.isEmpty()) {
            coinMap = coinRepository.findAllById(coinIds).stream()
                .collect(Collectors.toMap(Coin::getId, CoinResponse::from));
        }
        
        final Map<Integer, CoinResponse> finalCoinMap = coinMap;
        return assets.stream()
            .map(asset -> {
                CoinResponse coin = asset.getCoinId() != null ? finalCoinMap.get(asset.getCoinId()) : null;
                return coin != null 
                    ? AssetResponse.from(asset, coin)
                    : AssetResponse.from(asset);
            })
            .toList();
    }
    
    /**
     * 비동기 자산 및 거래내역 동기화 요청
     * fetch-server에 비동기 요청을 보내고 즉시 반환
     * 처리 완료 시 fetch-server가 콜백 API를 호출
     */
    @Override
    @Async("assetSyncExecutor")
    public void syncAssets(UUID userId) {
        try {
            // 1. 자산 동기화 비동기 요청
            Map<String, Object> assetRequestBody = new HashMap<>();
            assetRequestBody.put("user_id", userId.toString());
            assetRequestBody.put("callback_url", CALLBACK_URL);
            
            String asyncAssetUrl = fetchServerUrl + "/api/assets/sync-all/async";
            restTemplate.postForEntity(asyncAssetUrl, assetRequestBody, Map.class);
            
        } catch (Exception e) {
            log.error("비동기 자산 동기화 요청 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
        
        try {
            // 2. 거래내역 동기화 비동기 요청 (자산 동기화와 별도로 진행)
            Map<String, Object> tradingRequestBody = new HashMap<>();
            tradingRequestBody.put("user_id", userId.toString());
            tradingRequestBody.put("exchanges", List.of("UPBIT", "BITHUMB", "COINONE"));  // 지원하는 모든 거래소
            tradingRequestBody.put("callback_url", CALLBACK_URL);
            
            String asyncTradingUrl = fetchServerUrl + "/api/user/updateTradingHistory/async";
            restTemplate.postForEntity(asyncTradingUrl, tradingRequestBody, Map.class);
            
        } catch (Exception e) {
            log.error("비동기 거래내역 동기화 요청 실패: userId={}, error={}", userId, e.getMessage(), e);
        }
    }
}

