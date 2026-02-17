package com.bitreiver.app_server.domain.tradeEvaluation.service;

import com.bitreiver.app_server.domain.tradeEvaluation.dto.TradeEvaluationRequestDto;
import com.bitreiver.app_server.domain.tradeEvaluation.dto.TradeEvaluationStatusResponse;
import com.bitreiver.app_server.domain.tradeEvaluation.entity.TradeEvaluationStatus;
import com.bitreiver.app_server.domain.tradeEvaluation.enums.TradeEvaluationJobStatus;
import com.bitreiver.app_server.domain.tradeEvaluation.repository.TradeEvaluationResultRepository;
import com.bitreiver.app_server.domain.tradeEvaluation.repository.TradeEvaluationStatusRepository;
import com.bitreiver.app_server.domain.trading.repository.TradingHistoryRepository;
import com.bitreiver.app_server.domain.coin.repository.CoinRepository;
import com.bitreiver.app_server.domain.notification.dto.TradeEvaluationEventPayload;
import com.bitreiver.app_server.domain.notification.enums.NotificationType;
import com.bitreiver.app_server.domain.notification.service.NotificationService;
import com.bitreiver.app_server.domain.notification.service.NotificationSseService;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class TradeEvaluationRequestServiceImpl implements TradeEvaluationRequestService {

    private final TradeEvaluationStatusRepository statusRepository;
    private final TradeEvaluationResultRepository resultRepository;
    private final TradingHistoryRepository tradingHistoryRepository;
    private final WebClient aiServerWebClient;
    private final CoinRepository coinRepository;
    private final NotificationSseService notificationSseService;
    private final NotificationService notificationService;

    public TradeEvaluationRequestServiceImpl(
        TradeEvaluationStatusRepository statusRepository,
        TradeEvaluationResultRepository resultRepository,
        TradingHistoryRepository tradingHistoryRepository,
        @Qualifier("aiServerWebClient") WebClient aiServerWebClient,
        CoinRepository coinRepository,
        NotificationSseService notificationSseService,
        NotificationService notificationService
    ) {
        this.statusRepository = statusRepository;
        this.resultRepository = resultRepository;
        this.tradingHistoryRepository = tradingHistoryRepository;
        this.aiServerWebClient = aiServerWebClient;
        this.coinRepository = coinRepository;
        this.notificationSseService = notificationSseService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TradeEvaluationStatusResponse requestOrGetResult(UUID userId, TradeEvaluationRequestDto request) {
        Integer tradeId = request.getTradeId();
        LocalDate targetDate = request.getTargetDate();
        Integer coinId = request.getCoinId();

        tradingHistoryRepository.findById(tradeId)
            .filter(t -> t.getUserId().equals(userId))
            .orElseThrow(() -> new CustomException(ErrorCode.TRADING_HISTORY_NOT_FOUND));

        var statusOpt = statusRepository.findByUserIdAndTradeId(userId, tradeId);

        if (statusOpt.isPresent()) {
            TradeEvaluationStatus statusEntity = statusOpt.get();
            if (statusEntity.getStatus() == TradeEvaluationJobStatus.COMPLETED) {
                return resultRepository.findTopByTradeIdOrderByCreatedAtDesc(tradeId)
                    .map(r -> TradeEvaluationStatusResponse.builder()
                        .status(TradeEvaluationJobStatus.COMPLETED)
                        .result(r.getResult())
                        .build())
                    .orElse(TradeEvaluationStatusResponse.builder()
                        .status(TradeEvaluationJobStatus.IN_PROGRESS)
                        .build());
            }
            if (statusEntity.getStatus() == TradeEvaluationJobStatus.IN_PROGRESS) {
                return TradeEvaluationStatusResponse.builder()
                    .status(TradeEvaluationJobStatus.IN_PROGRESS)
                    .build();
            }
            // PENDING or FAILED: 아래에서 재시도로 IN_PROGRESS 저장 후 ai 호출
        }

        TradeEvaluationStatus statusEntity = statusOpt.map(s -> {
            s.setStatus(TradeEvaluationJobStatus.IN_PROGRESS);
            s.setUpdatedAt(LocalDateTime.now());
            return s;
        }).orElseGet(() ->
            TradeEvaluationStatus.builder()
                .userId(userId)
                .tradeId(tradeId)
                .targetDate(targetDate)
                .coinId(coinId)
                .status(TradeEvaluationJobStatus.IN_PROGRESS)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()
        );
        statusRepository.save(statusEntity);

        try {
            Map<String, Object> requestBody = Map.of(
                "user_id", userId.toString(),
                "trade_id", tradeId,
                "target_date", targetDate.toString(),
                "coin_id", coinId
            );
            Map<String, Object> responseBody = aiServerWebClient.post()
                .uri("/api/trade-evaluation/evaluate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            if (responseBody != null && Boolean.TRUE.equals(responseBody.get("success")) && responseBody.get("data") != null) {
                statusEntity.setStatus(TradeEvaluationJobStatus.COMPLETED);
                statusEntity.setUpdatedAt(LocalDateTime.now());
                statusRepository.save(statusEntity);
                saveTradeEvaluationNotification(userId, tradeId, targetDate, coinId, TradeEvaluationJobStatus.COMPLETED);
                sendTradeEvaluationSse(userId, tradeId, targetDate, coinId, TradeEvaluationJobStatus.COMPLETED);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                return TradeEvaluationStatusResponse.builder()
                    .status(TradeEvaluationJobStatus.COMPLETED)
                    .result(data)
                    .build();
            }
        } catch (WebClientResponseException e) {
            log.warn("ai-server 매매 분석 요청 실패: tradeId={}, status={}", tradeId, e.getStatusCode(), e);
            statusEntity.setStatus(TradeEvaluationJobStatus.FAILED);
            statusEntity.setUpdatedAt(LocalDateTime.now());
            statusRepository.save(statusEntity);
            saveTradeEvaluationNotification(userId, tradeId, targetDate, coinId, TradeEvaluationJobStatus.FAILED);
            sendTradeEvaluationSse(userId, tradeId, targetDate, coinId, TradeEvaluationJobStatus.FAILED);
            return TradeEvaluationStatusResponse.builder()
                .status(TradeEvaluationJobStatus.FAILED)
                .build();
        } catch (Exception e) {
            log.warn("ai-server 매매 분석 요청 중 오류: tradeId={}", tradeId, e);
            statusEntity.setStatus(TradeEvaluationJobStatus.FAILED);
            statusEntity.setUpdatedAt(LocalDateTime.now());
            statusRepository.save(statusEntity);
            saveTradeEvaluationNotification(userId, tradeId, targetDate, coinId, TradeEvaluationJobStatus.FAILED);
            sendTradeEvaluationSse(userId, tradeId, targetDate, coinId, TradeEvaluationJobStatus.FAILED);
            return TradeEvaluationStatusResponse.builder()
                .status(TradeEvaluationJobStatus.FAILED)
                .build();
        }

        return TradeEvaluationStatusResponse.builder()
            .status(TradeEvaluationJobStatus.IN_PROGRESS)
            .build();
    }

    /** DB notifications 테이블에 저장 + createNotification 내부에서 SSE 'notification' 이벤트 전송 */
    private void saveTradeEvaluationNotification(UUID userId, Integer tradeId, LocalDate targetDate, Integer coinId, TradeEvaluationJobStatus status) {
        String symbol = coinRepository.findById(coinId).map(c -> c.getSymbol()).orElse("?");
        LocalDateTime completedAt = LocalDateTime.now();
        boolean success = status == TradeEvaluationJobStatus.COMPLETED;
        String title = success
            ? "매매 분석 완료 (" + symbol + ")"
            : "매매 분석 실패 (" + symbol + ")";
        String content = completedAt + " - " + targetDate + " " + symbol + " 매매 분석이 " + (success ? "완료" : "실패") + "되었습니다.";
        String metadata = String.format(
            "{\"tradeId\":%d,\"targetDate\":\"%s\",\"symbol\":\"%s\",\"success\":%s}",
            tradeId, targetDate, symbol.replace("\\", "\\\\").replace("\"", "\\\""), success
        );
        try {
            notificationService.createNotification(userId, NotificationType.AI_SYSTEM, title, content, metadata);
        } catch (Exception e) {
            log.warn("매매 분석 알림 DB 저장 실패: userId={}, tradeId={}", userId, tradeId, e);
        }
    }

    private void sendTradeEvaluationSse(UUID userId, Integer tradeId, LocalDate targetDate, Integer coinId, TradeEvaluationJobStatus status) {
        String symbol = coinRepository.findById(coinId).map(c -> c.getSymbol()).orElse("?");
        TradeEvaluationEventPayload payload = TradeEvaluationEventPayload.builder()
            .success(status == TradeEvaluationJobStatus.COMPLETED)
            .status(status)
            .tradeId(tradeId)
            .targetDate(targetDate)
            .completedAt(LocalDateTime.now())
            .symbol(symbol)
            .build();
        notificationSseService.sendTradeEvaluationEvent(userId, payload);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCompleted(UUID userId, Integer tradeId) {
        return statusRepository.findByUserIdAndTradeId(userId, tradeId)
            .map(s -> s.getStatus() == TradeEvaluationJobStatus.COMPLETED)
            .orElse(false);
    }
}
