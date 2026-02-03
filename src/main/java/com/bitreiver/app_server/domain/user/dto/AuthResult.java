package com.bitreiver.app_server.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인/토큰 갱신 시 서비스가 컨트롤러에 전달하는 내부 결과.
 * body에는 AuthResponse(refreshToken 제외)를 내려주고, refreshToken은 쿠키로만 설정하기 위함.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResult {
    private AuthResponse authResponse;
    private String refreshToken;
}
