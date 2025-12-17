package com.bitreiver.app_server.global.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Redis 캐시 서비스 통합 래퍼
 * 기존 코드 호환성을 위해 유지
 * 내부적으로 RedisStringCacheService와 RedisZSetCacheService를 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {
    
    private final RedisCacheStringService stringCacheService;
    private final RedisCacheZSetService zSetCacheService;
    
    // ========== String 메서드 (기존 코드 호환) ==========
    
    public <T> Optional<T> get(String key, Class<T> clazz) {
        return stringCacheService.get(key, clazz);
    }
    
    public <T> Optional<T> get(String key, TypeReference<T> typeReference) {
        return stringCacheService.get(key, typeReference);
    }
    
    public void set(String key, Object value) {
        stringCacheService.set(key, value);
    }
    
    public void set(String key, Object value, long ttlSeconds) {
        stringCacheService.set(key, value, ttlSeconds);
    }
    
    public void delete(String key) {
        stringCacheService.delete(key);
    }
    
    public boolean exists(String key) {
        return stringCacheService.exists(key);
    }
    
    // ========== ZSET 메서드 (새로 추가) ==========
    
    public void zAdd(String key, Object value, double score) {
        zSetCacheService.zAdd(key, value, score);
    }
    
    public <T> void zAddAll(String key, List<T> values, Function<T, Double> scoreExtractor, long ttlSeconds) {
        zSetCacheService.zAddAll(key, values, scoreExtractor, ttlSeconds);
    }
    
    public <T> void zAddAll(String key, List<T> values, Function<T, Double> scoreExtractor) {
        zSetCacheService.zAddAll(key, values, scoreExtractor);
    }
    
    public <T> List<T> zRangeByScore(String key, double minScore, double maxScore, Class<T> clazz) {
        return zSetCacheService.zRangeByScore(key, minScore, maxScore, clazz);
    }
    
    public <T> List<T> zRangeAll(String key, Class<T> clazz) {
        return zSetCacheService.zRangeAll(key, clazz);
    }
    
    public Long zCard(String key) {
        return zSetCacheService.zCard(key);
    }
    
    public boolean zExists(String key) {
        return zSetCacheService.zExists(key);
    }
}