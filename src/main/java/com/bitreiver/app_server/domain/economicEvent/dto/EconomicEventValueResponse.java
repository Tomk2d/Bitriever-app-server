package com.bitreiver.app_server.domain.economicEvent.dto;

import com.bitreiver.app_server.domain.economicEvent.entity.EconomicEventValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EconomicEventValueResponse {
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
    
    public static EconomicEventValueResponse from(EconomicEventValue value) {
        if (value == null) {
            return null;
        }
        
        return EconomicEventValueResponse.builder()
            .id(value.getId())
            .ric(value.getRic())
            .unit(value.getUnit())
            .unitPrefix(value.getUnitPrefix())
            .actual(value.getActual())
            .forecast(value.getForecast())
            .actualForecastDiff(value.getActualForecastDiff())
            .historical(value.getHistorical())
            .time(value.getTime())
            .preAnnouncementWording(value.getPreAnnouncementWording())
            .build();
    }
}
