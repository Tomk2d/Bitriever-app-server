package com.bitreiver.app_server.domain.user.service;

import com.bitreiver.app_server.domain.user.dto.OAuth2CodePayload;
import com.bitreiver.app_server.global.cache.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * OAuth2 일회성 code 저장/조회 (Redis).
 * TTL 120초.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2CodeService {

    private static final String KEY_PREFIX = "oauth2:code:";
    private static final long TTL_SECONDS = 120;

    private final RedisCacheService redisCacheService;

    public String save(OAuth2CodePayload payload) {
        String code = UUID.randomUUID().toString();
        String key = KEY_PREFIX + code;
        redisCacheService.set(key, payload, TTL_SECONDS);
        return code;
    }

    public Optional<OAuth2CodePayload> getAndDelete(String code) {
        if (code == null || code.isEmpty()) {
            return Optional.empty();
        }
        String key = KEY_PREFIX + code;
        Optional<OAuth2CodePayload> payload = redisCacheService.get(key, OAuth2CodePayload.class);
        redisCacheService.delete(key);
        return payload;
    }
}
