package com.bitreiver.app_server.global.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 시간 관련 유틸리티.
 * 알림 등 사용자에게 표시되는 시간은 한국 시간(KST) 기준으로 통일합니다.
 */
public final class TimeUtil {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    private TimeUtil() {
    }

    /**
     * 현재 시각을 한국 시간(KST) 기준 LocalDateTime으로 반환.
     * DB 저장 및 알림 표시용으로 사용합니다.
     */
    public static LocalDateTime nowKorea() {
        return LocalDateTime.now(KOREA_ZONE);
    }
}
