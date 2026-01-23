package com.bitreiver.app_server.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SnsProvider {
    NAVER((short) 1, "naver"),
    KAKAO((short) 2, "kakao"),
    GOOGLE((short) 3, "google"),
    APPLE((short) 4, "apple");
    
    private final Short code;
    private final String registrationId;
    
    public static SnsProvider fromCode(Short code) {
        for (SnsProvider provider : values()) {
            if (provider.code.equals(code)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown SNS provider code: " + code);
    }
    
    public static SnsProvider fromRegistrationId(String registrationId) {
        for (SnsProvider provider : values()) {
            if (provider.registrationId.equals(registrationId)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown SNS provider registration ID: " + registrationId);
    }
}
