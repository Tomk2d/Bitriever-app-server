package com.bitreiver.app_server.domain.economicIndex.dto;

import com.bitreiver.app_server.domain.economicIndex.enums.EconomicIndexType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "경제 지표 응답")
public class EconomicIndexResponse {
    @Schema(description = "지표 타입", example = "KOSPI")
    private EconomicIndexType indexType;
    
    @Schema(description = "날짜 및 시간", example = "2024-01-01T09:00:00")
    private LocalDateTime dateTime;
    
    @Schema(description = "날짜 시간 문자열", example = "2024-01-01 09:00:00")
    private String dateTimeString;
    
    @Schema(description = "가격", example = "2500.50")
    private BigDecimal price;
    
    @Schema(description = "전일 종가", example = "2490.30")
    private BigDecimal previousClose;
    
    @Schema(description = "전일 대비 등락 금액", example = "10.20")
    private BigDecimal changeAmount;
    
    @Schema(description = "전일 대비 등락률(%)", example = "1.25")
    private BigDecimal changeRate;
    
    public static EconomicIndexResponse from(EconomicIndexRedisDto dto, EconomicIndexType indexType) {
        return EconomicIndexResponse.builder()
            .indexType(indexType)
            .dateTime(dto.getDateTime())
            .dateTimeString(dto.getDateTimeString())
            .price(dto.getPrice())
            .previousClose(dto.getPreviousClose())
            .changeAmount(dto.getChangeAmount())
            .changeRate(dto.getChangeRate())
            .build();
    }
}
