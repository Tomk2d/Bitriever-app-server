package com.bitreiver.app_server.domain.economicEvent.dto;

import com.bitreiver.app_server.domain.economicEvent.entity.EconomicEvent;
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
public class EconomicEventResponse {
    private Long id;
    private String uniqueName;
    private LocalDate eventDate;
    private String title;
    private String subtitleText;
    private String countryType;
    private Boolean excludeFromAll;
    private EconomicEventValueResponse economicEventValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static EconomicEventResponse from(EconomicEvent event) {
        return EconomicEventResponse.builder()
            .id(event.getId())
            .uniqueName(event.getUniqueName())
            .eventDate(event.getEventDate())
            .title(event.getTitle())
            .subtitleText(event.getSubtitleText())
            .countryType(event.getCountryType())
            .excludeFromAll(event.getExcludeFromAll())
            .economicEventValue(EconomicEventValueResponse.from(event.getEconomicEventValue()))
            .createdAt(event.getCreatedAt())
            .updatedAt(event.getUpdatedAt())
            .build();
    }
    
    public static EconomicEventResponse from(EconomicEventRedisDto dto) {
        return EconomicEventResponse.builder()
            .id(dto.getId())
            .uniqueName(dto.getUniqueName())
            .eventDate(dto.getEventDate())
            .title(dto.getTitle())
            .subtitleText(dto.getSubtitleText())
            .countryType(dto.getCountryType())
            .excludeFromAll(dto.getExcludeFromAll())
            .economicEventValue(EconomicEventValueResponse.from(dto.getEconomicEventValue()))
            .createdAt(dto.getCreatedAt())
            .updatedAt(dto.getUpdatedAt())
            .build();
    }
}
