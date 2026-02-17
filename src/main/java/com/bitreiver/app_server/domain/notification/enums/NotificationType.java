package com.bitreiver.app_server.domain.notification.enums;

public enum NotificationType {
    USER_ACTIVITY,      // 사용자 활동 (댓글, 좋아요 등)
    SYSTEM,             // 시스템 알림 (공지사항, 업데이트 등)
    USER_UPDATE,        // 사용자 정보 업데이트 (자산 연동, 자산 분석 등)
    TRADING,            // 매매 관련
    AI_SYSTEM,          // AI 관련 (매매 분석 완료/실패 등)
}
