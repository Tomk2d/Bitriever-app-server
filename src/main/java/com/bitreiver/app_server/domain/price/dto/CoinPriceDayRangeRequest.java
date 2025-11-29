package com.bitreiver.app_server.domain.price.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Schema(description = "기간별 매매 내역 조회 요청", example = "{\"coinId\":1,\"startDate\":\"2025-07-01T00:00:00\",\"endDate\":\"2025-08-01T00:00:00\"}")
public class CoinPriceDayRangeRequest {

    @Schema(description = "코인 ID", example = "1", required = true)
    @NotNull(message = "코인 ID는 필수입니다.")
    private Integer coinId;
    
    @Schema(description = "시작 날짜 및 시간 (ISO 8601 형식)", example = "2025-07-01T00:00:00", required = true)
    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDateTime startDate;
    
    @Schema(description = "종료 날짜 및 시간 (ISO 8601 형식, 미포함)", example = "2025-08-01T00:00:00", required = true)
    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDateTime endDate;
}