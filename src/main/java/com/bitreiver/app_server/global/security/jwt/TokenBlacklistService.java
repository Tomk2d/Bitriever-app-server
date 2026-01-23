package com.bitreiver.app_server.global.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    
    private static final String BLACKLIST_PREFIX_ACCESS = "blacklist:access:";
    private static final String BLACKLIST_PREFIX_REFRESH = "blacklist:refresh:";
    
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    
    /**
     * Access token을 블랙리스트에 추가
     * @param token 블랙리스트에 추가할 access token
     */
    public void addAccessTokenToBlacklist(String token) {
        try {
            long remainingTime = jwtTokenProvider.getRemainingTime(token);
            if (remainingTime > 0) {
                String key = BLACKLIST_PREFIX_ACCESS + token;
                stringRedisTemplate.opsForValue().set(key, "blacklisted", Duration.ofMillis(remainingTime));
                log.debug("Access token added to blacklist, remaining time: {}ms", remainingTime);
            }
        } catch (Exception e) {
            log.warn("Failed to add access token to blacklist: {}", e.getMessage());
        }
    }
    
    /**
     * Refresh token을 블랙리스트에 추가
     * @param token 블랙리스트에 추가할 refresh token
     */
    public void addRefreshTokenToBlacklist(String token) {
        try {
            long remainingTime = jwtTokenProvider.getRemainingTime(token);
            if (remainingTime > 0) {
                String key = BLACKLIST_PREFIX_REFRESH + token;
                stringRedisTemplate.opsForValue().set(key, "blacklisted", Duration.ofMillis(remainingTime));
                log.debug("Refresh token added to blacklist, remaining time: {}ms", remainingTime);
            }
        } catch (Exception e) {
            log.warn("Failed to add refresh token to blacklist: {}", e.getMessage());
        }
    }
    
    /**
     * Access token이 블랙리스트에 있는지 확인
     * @param token 확인할 access token
     * @return 블랙리스트에 있으면 true
     */
    public boolean isAccessTokenBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX_ACCESS + token;
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Failed to check access token blacklist: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Refresh token이 블랙리스트에 있는지 확인
     * @param token 확인할 refresh token
     * @return 블랙리스트에 있으면 true
     */
    public boolean isRefreshTokenBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX_REFRESH + token;
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Failed to check refresh token blacklist: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Access token과 Refresh token을 모두 블랙리스트에 추가
     * @param accessToken 블랙리스트에 추가할 access token
     * @param refreshToken 블랙리스트에 추가할 refresh token
     */
    public void addTokensToBlacklist(String accessToken, String refreshToken) {
        if (accessToken != null) {
            addAccessTokenToBlacklist(accessToken);
        }
        if (refreshToken != null) {
            addRefreshTokenToBlacklist(refreshToken);
        }
    }
}
