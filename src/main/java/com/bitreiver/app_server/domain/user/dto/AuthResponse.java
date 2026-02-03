package com.bitreiver.app_server.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private UUID userId;
    private String email;
    private String nickname;
    private String profileUrl;
    private String accessToken;
    @Builder.Default
    private Boolean requiresNickname = false; // SNS 회원가입 시 닉네임 설정 필요 여부
}

