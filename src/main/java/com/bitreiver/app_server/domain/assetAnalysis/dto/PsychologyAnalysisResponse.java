package com.bitreiver.app_server.domain.assetAnalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "심리 분석 응답")
public class PsychologyAnalysisResponse {
    
    @Schema(description = "심리 상태별 분포")
    private Map<String, Long> mindDistribution;
    
    @Schema(description = "심리 상태별 평균 수익률")
    private Map<String, BigDecimal> mindAverageProfitRate;
    
    @Schema(description = "태그 빈도 (상위 N개)")
    private List<TagFrequency> topTags;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "태그 빈도")
    public static class TagFrequency {
        @Schema(description = "태그 이름", example = "스캘핑")
        private String tag;
        
        @Schema(description = "사용 횟수", example = "15")
        private Long count;
    }
}
