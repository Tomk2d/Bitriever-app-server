package com.bitreiver.app_server.domain.tradeEvaluation.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * ai-server의 trade_evaluation_results 테이블과 동일.
 * app-server가 같은 DB에서 조회할 때 사용 (읽기 전용).
 */
@Entity
@Table(name = "trade_evaluation_results", indexes = {
    @Index(name = "idx_trade_eval_results_trade_id", columnList = "trade_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TradeEvaluationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "trade_id", nullable = false)
    private Integer tradeId;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "coin_id", nullable = false)
    private Integer coinId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Type(JsonType.class)
    @Column(name = "result", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> result;
}
