package com.bitreiver.app_server.domain.tradeEvaluation.repository;

import com.bitreiver.app_server.domain.tradeEvaluation.entity.TradeEvaluationStatus;
import com.bitreiver.app_server.domain.tradeEvaluation.enums.TradeEvaluationJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TradeEvaluationStatusRepository extends JpaRepository<TradeEvaluationStatus, Long> {

    Optional<TradeEvaluationStatus> findByUserIdAndTradeId(UUID userId, Integer tradeId);

    boolean existsByUserIdAndTradeIdAndStatus(UUID userId, Integer tradeId, TradeEvaluationJobStatus status);
}
