package com.bitreiver.app_server.global.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import java.time.Duration;

import java.util.Optional;

@Slf4j
@Service
public class RedisCacheStringService extends RedisCacheBaseService {
    
    @Value("${cache.string.day.ttl:86400}")
    private long dayTtl;

    public RedisCacheStringService(
                ObjectMapper objectMapper, 
                StringRedisTemplate stringRedisTemplate, 
                StringRedisTemplate stringRedisReadTemplate, 
                RedisTemplate<String, Object> redisTemplate, 
                RedisTemplate<String, Object> redisReadTemplate) {
        super(objectMapper, stringRedisTemplate, stringRedisReadTemplate, redisTemplate, redisReadTemplate);

    }

     /**
     * String 값 조회
     */
    public <T> Optional<T> get(String key, Class<T> clazz) {
        try{
            String cachedValue = stringRedisReadTemplate.opsForValue().get(key);
            if(cachedValue != null) {
                T value = deserialize(cachedValue, clazz);
                return Optional.ofNullable(value);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.warn("redis 캐시 조회 중 오류 발생 - key: {}, error: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * String 값 조회 (TypeReference)
     */
    public <T> Optional<T> get(String key, TypeReference<T> typeReference) {
        try {
            String cachedValue = stringRedisReadTemplate.opsForValue().get(key);
            if (cachedValue != null) {
                T value = deserialize(cachedValue, typeReference);
                return Optional.ofNullable(value);
            }
            log.debug("캐시 미스 - key: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.warn("캐시 조회 중 오류 발생 - key: {}, error: {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    public void set(String key, Object value) {
        set(key, value, dayTtl);
    }

    /**
     * String 값 저장 (TTL 지정)
     */
    public void set(String key, Object value, long ttlSeconds) {
        try {
            String jsonValue = serialize(value);
            stringRedisTemplate.opsForValue().set(key, jsonValue, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("캐시 저장 중 오류 발생 - key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * 키 존재 여부 확인
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("캐시 존재 확인 중 오류 발생 - key: {}, error: {}", key, e.getMessage());
            return false;
        }
    }

}
