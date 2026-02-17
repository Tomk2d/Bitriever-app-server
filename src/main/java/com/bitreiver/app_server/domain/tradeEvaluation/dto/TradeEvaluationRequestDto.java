package com.bitreiver.app_server.domain.tradeEvaluation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매매 분석 요청")
public class TradeEvaluationRequestDto {

    @NotNull
    @Schema(description = "분석할 매매 내역 ID (trading_histories.id)", example = "123", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer tradeId;

    @NotNull
    @Schema(description = "선택된 날짜 (YYYY-MM-DD)", example = "2022-01-14", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate targetDate;

    @NotNull
    @Schema(description = "코인 ID (coins.id)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer coinId;
}
