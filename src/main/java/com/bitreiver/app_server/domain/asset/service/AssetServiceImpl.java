package com.bitreiver.app_server.domain.asset.service;

import com.bitreiver.app_server.domain.asset.dto.AssetResponse;
import com.bitreiver.app_server.domain.asset.entity.Asset;
import com.bitreiver.app_server.domain.asset.repository.AssetRepository;
import com.bitreiver.app_server.domain.coin.dto.CoinResponse;
import com.bitreiver.app_server.domain.coin.entity.Coin;
import com.bitreiver.app_server.domain.coin.repository.CoinRepository;
import com.bitreiver.app_server.domain.notification.service.NotificationService;
import com.bitreiver.app_server.domain.notification.enums.NotificationType;
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
    private final NotificationService notificationService;
    
    @Value("${external.upbit.server.url}")
    private String upbitServerUrl;
    
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
    
    @Override
    @Async("assetSyncExecutor")
    public void syncAssets(UUID userId) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("user_id", userId.toString());
            
            String url = upbitServerUrl + "/api/upbit/accounts";
            restTemplate.postForObject(url, requestBody, String.class);

            // 자산 동기화 완료 알림 생성
            try {
                notificationService.createNotification(
                    userId,
                    NotificationType.USER_UPDATE,
                    "자산 동기화 완료",
                    "거래소 자산 정보가 성공적으로 동기화되었습니다.",
                    null
                );
            } catch (Exception e) {
                log.error("자산 동기화 완료 '알림' 생성 실패: userId={}, error={}", userId, e.getMessage(), e);
            }
            
            
            // 자산 동기화 후, 매매내역도 업데이트
            try {
                Map<String, String> tradingHistoryRequestBody = new HashMap<>();
                tradingHistoryRequestBody.put("user_id", userId.toString());
                tradingHistoryRequestBody.put("exchange_provider_str", "UPBIT");
                
                String tradingHistoryUrl = upbitServerUrl + "/api/user/updateTradingHistory";
                restTemplate.postForObject(tradingHistoryUrl, tradingHistoryRequestBody, String.class);

                // 매매내역 동기화 완료 알림 생성
                try {
                    notificationService.createNotification(
                        userId,
                        NotificationType.USER_UPDATE,
                        "매매내역 동기화 완료",
                        "거래소 매매내역 정보가 성공적으로 동기화되었습니다.",
                        null
                    );
                } catch (Exception e) {
                    log.error("자산 동기화 완료 알림 생성 실패: userId={}, error={}", userId, e.getMessage(), e);
                }
                
            } catch (Exception e) {
                // 매매내역 업데이트 실패해도 자산 동기화는 성공했으므로 로그만 남기고 계속 진행
                log.error("매매내역 동기화 실패 (자산 동기화는 성공): userId={}, error={}", userId, e.getMessage(), e);

                // 매매내역 동기화 실패시 알림
                try {
                    notificationService.createNotification(
                        userId,
                        NotificationType.USER_UPDATE,
                        "매매내역 동기화 실패",
                        "거래소 매매내역 정보가 동기화에 실패했습니다. 거래소 토큰 유효기간을 확인해주세요. 지속적으로 실패한다면 FAQ 를 통해서 개발자에게 문의해주세요.",
                        null
                    );
                } catch (Exception e2) {
                    log.error("매매내역 동기화 완료 '알림' 생성 실패: userId={}, error={}", userId, e2.getMessage(), e2);
                }
            }
        } catch (Exception e) {
            log.error("자산 동기화 실패: userId={}, error={}", userId, e.getMessage(), e);

            // 자산 동기화 실패시 알림
            try {
                notificationService.createNotification(
                    userId,
                    NotificationType.USER_UPDATE,
                    "자산 동기화 실패",
                    "거래소 자산 정보가 동기화에 실패했습니다. 거래소 토큰 유효기간을 확인해주세요. 지속적으로 실패한다면 FAQ 를 통해서 개발자에게 문의해주세요.",
                    null
                );
            } catch (Exception e2) {
                log.error("자산 동기화 완료 '알림' 생성 실패: userId={}, error={}", userId, e2.getMessage(), e2);
            }
        }
    }
}

