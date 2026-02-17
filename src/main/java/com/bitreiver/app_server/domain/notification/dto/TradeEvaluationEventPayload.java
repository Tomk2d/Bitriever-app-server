package com.bitreiver.app_server.domain.notification.dto;

import com.bitreiver.app_server.domain.tradeEvaluation.enums.TradeEvaluationJobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매매 분석 완료 SSE 이벤트 페이로드")
public class TradeEvaluationEventPayload {

    @Schema(description = "성공 여부")
    private boolean success;

    @Schema(description = "작업 상태 (COMPLETED / FAILED)")
    private TradeEvaluationJobStatus status;

    @Schema(description = "매매 ID")
    private Integer tradeId;

    @Schema(description = "분석 대상 일자")
    private LocalDate targetDate;

    @Schema(description = "완료 시각 (날짜+시간)")
    private LocalDateTime completedAt;

    @Schema(description = "코인 심볼명")
    private String symbol;
}
