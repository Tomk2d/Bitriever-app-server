package com.bitreiver.app_server.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * OAuth2 일회성 code 교환 시 Redis에 저장하는 페이로드.
 * code로 조회 후 refresh_token은 쿠키로, 나머지는 body로 반환.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2CodePayload {
    private String accessToken;
    private String refreshToken;
    private UUID userId;
    private String email;
    private String nickname;
    private String profileUrl;
    private Boolean requiresNickname;

    public static OAuth2CodePayload from(AuthResult result) {
        AuthResponse auth = result.getAuthResponse();
        return OAuth2CodePayload.builder()
            .accessToken(auth.getAccessToken())
            .refreshToken(result.getRefreshToken())
            .userId(auth.getUserId())
            .email(auth.getEmail())
            .nickname(auth.getNickname())
            .profileUrl(auth.getProfileUrl())
            .requiresNickname(auth.getRequiresNickname() != null ? auth.getRequiresNickname() : false)
            .build();
    }
}
