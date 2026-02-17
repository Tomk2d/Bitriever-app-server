package com.bitreiver.app_server.domain.tradeEvaluation.dto;

import com.bitreiver.app_server.domain.tradeEvaluation.enums.TradeEvaluationJobStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매매 분석 상태/결과 응답")
public class TradeEvaluationStatusResponse {

    @Schema(description = "작업 상태")
    private TradeEvaluationJobStatus status;

    @Schema(description = "완료 시 분석 결과 (COMPLETED일 때만 존재)")
    private Map<String, Object> result;
}
