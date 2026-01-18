package com.bitreiver.app_server.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsResponse {
    private Long unreadCount;
    
    public static NotificationStatsResponse of(Long unreadCount) {
        return NotificationStatsResponse.builder()
            .unreadCount(unreadCount)
            .build();
    }
}
