package com.bitreiver.app_server.domain.tradeEvaluation.service;

import com.bitreiver.app_server.domain.tradeEvaluation.dto.TradeEvaluationRequestDto;
import com.bitreiver.app_server.domain.tradeEvaluation.dto.TradeEvaluationStatusResponse;

import java.util.UUID;

public interface TradeEvaluationRequestService {

    /**
     * JWT로 확인된 userId와 요청으로 상태 확인 후:
     * - COMPLETED: trade_evaluation_results 조회해 결과 반환
     * - IN_PROGRESS: 202 응답용 상태 반환
     * - 없음/PENDING/FAILED: 상태를 IN_PROGRESS로 저장 후 ai-server 동기 호출, 성공 시 COMPLETED로 갱신 후 결과 반환
     */
    TradeEvaluationStatusResponse requestOrGetResult(UUID userId, TradeEvaluationRequestDto request);

    boolean isCompleted(UUID userId, Integer tradeId);
}
