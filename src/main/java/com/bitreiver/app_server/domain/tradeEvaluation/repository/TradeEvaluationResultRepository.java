package com.bitreiver.app_server.domain.tradeEvaluation.repository;

import com.bitreiver.app_server.domain.tradeEvaluation.entity.TradeEvaluationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TradeEvaluationResultRepository extends JpaRepository<TradeEvaluationResult, Long> {

    Optional<TradeEvaluationResult> findTopByTradeIdOrderByCreatedAtDesc(Integer tradeId);
}
