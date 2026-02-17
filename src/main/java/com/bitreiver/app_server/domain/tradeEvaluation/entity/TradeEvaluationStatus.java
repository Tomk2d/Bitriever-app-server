package com.bitreiver.app_server.domain.tradeEvaluation.entity;

import com.bitreiver.app_server.domain.tradeEvaluation.enums.TradeEvaluationJobStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trade_evaluation_status", indexes = {
    @Index(name = "idx_trade_evaluation_status_lookup", columnList = "user_id, trade_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_trade_evaluation_status_user_trade", columnNames = {"user_id", "trade_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeEvaluationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "trade_id", nullable = false)
    private Integer tradeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TradeEvaluationJobStatus status;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "coin_id", nullable = false)
    private Integer coinId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
