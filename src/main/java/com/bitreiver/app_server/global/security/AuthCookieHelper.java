package com.bitreiver.app_server.global.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Refresh token 쿠키 생성/제거 유틸.
 * HttpOnly, Secure(HTTPS 시), SameSite=Lax, Path=/api/auth 적용.
 */
@Component
public class AuthCookieHelper {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final String COOKIE_PATH = "/api/auth";

    @Value("${jwt.refresh-token-validity:604800000}") // default 7 days in ms
    private long refreshTokenValidityMs;

    /**
     * refresh_token 쿠키 생성 (Set-Cookie용).
     * Secure는 request.isSecure()가 true일 때만 적용 (로컬 HTTP에서는 false).
     */
    public ResponseCookie createRefreshTokenCookie(String refreshToken, HttpServletRequest request) {
        // Max-Age: 초 단위 (JWT validity는 ms)
        long maxAgeSeconds = refreshTokenValidityMs > 0 ? refreshTokenValidityMs / 1000 : 604800; // 7 days default
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
            .httpOnly(true)
            .secure(request != null && request.isSecure())
            .sameSite("Lax")
            .path(COOKIE_PATH)
            .maxAge(maxAgeSeconds)
            .build();
    }

    /**
     * refresh_token 쿠키 제거용 (로그아웃 시).
     */
    public ResponseCookie clearRefreshTokenCookie(HttpServletRequest request) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(request != null && request.isSecure())
            .sameSite("Lax")
            .path(COOKIE_PATH)
            .maxAge(0)
            .build();
    }
}
