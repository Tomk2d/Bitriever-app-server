package com.bitreiver.app_server.domain.assetAnalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "거래 빈도 분석 응답")
public class TradingFrequencyResponse {
    
    @Schema(description = "시간대별 거래 빈도 (0-23시)", example = "{\"0\": 5, \"1\": 3, ...}")
    private Map<Integer, Long> hourlyFrequency;
    
    @Schema(description = "요일별 거래 빈도 (0=일요일, 6=토요일)", example = "{\"0\": 10, \"1\": 15, ...}")
    private Map<Integer, Long> dayOfWeekFrequency;
    
    @Schema(description = "월별 거래 횟수 추이")
    private List<MonthlyFrequency> monthlyFrequency;
    
    @Schema(description = "가장 활발한 거래 시간대", example = "14")
    private Integer mostActiveHour;
    
    @Schema(description = "가장 활발한 거래 요일", example = "1")
    private Integer mostActiveDayOfWeek;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "월별 거래 빈도")
    public static class MonthlyFrequency {
        @Schema(description = "년", example = "2024")
        private Integer year;
        
        @Schema(description = "월", example = "1")
        private Integer month;
        
        @Schema(description = "거래 횟수", example = "50")
        private Long count;
    }
}
