package com.bitreiver.app_server.domain.feargreed.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공포/탐욕 지수 응답")
public class FearGreedResponse {
    @Schema(description = "공포/탐욕 지수 ID", example = "1")
    private Integer id;
    
    @Schema(description = "날짜", example = "2025-12-09")
    private LocalDate date;
    
    @Schema(description = "공포/탐욕 지수 값 (0-100)", example = "22")
    private Integer value;
}

