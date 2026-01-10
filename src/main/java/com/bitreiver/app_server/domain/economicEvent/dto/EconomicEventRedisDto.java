package com.bitreiver.app_server.domain.economicEvent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EconomicEventRedisDto {
    private Long id;
    private String uniqueName;
    private LocalDate eventDate;
    private String title;
    private String subtitleText;
    private String countryType;
    private Boolean excludeFromAll;
    private EconomicEventValueRedisDto economicEventValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EconomicEventValueRedisDto {
        private Long id;
        private String ric;
        private String unit;
        private String unitPrefix;
        private BigDecimal actual;
        private BigDecimal forecast;
        private BigDecimal actualForecastDiff;
        private BigDecimal historical;
        private LocalTime time;
        private String preAnnouncementWording;
    }
}