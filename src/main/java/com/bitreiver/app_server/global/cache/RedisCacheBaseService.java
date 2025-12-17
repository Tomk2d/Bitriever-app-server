package com.bitreiver.app_server.global.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.Collections;
import java.util.stream.Collectors;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheBaseService {
    protected final ObjectMapper objectMapper;
    
    @Qualifier("stringRedisTemplate")
    protected final StringRedisTemplate stringRedisTemplate;

    @Qualifier("stringRedisReadTemplate")
    protected final StringRedisTemplate stringRedisReadTemplate;

    @Qualifier("redisTemplate")
    protected final RedisTemplate<String, Object> redisTemplate;

    @Qualifier("redisReadTemplate")
    protected final RedisTemplate<String, Object> redisReadTemplate;
    
    @Value("${cache.coin.ttl:86400}")
    protected long defaultTtl;

    /**
    * Redis 공통 직렬화, 역직렬화 메서드
    */
    protected String serialize(Object value) {
        try{
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("redis 직렬화 오류 발생 - error : {}, value : {}", e.getMessage(), value);
            throw new RuntimeException("redis 직렬화 오류 발생", e);
        }
    }

    protected <T> T deserialize(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.warn("redis 역직렬화 오류 발생 - error: {}", e.getMessage());
            return null;
        }
    }
    
    protected <T> T deserialize(String json, com.fasterxml.jackson.core.type.TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.warn("redis 역직렬화 오류 발생 - error: {}", e.getMessage());
            return null;
        }
    }


    /**
     *  ZSET 멤버를 객체 리스트로 반환
     */
    protected <T> List<T> convertZSetMembersToObjects(Set<Object> members, Class<T> clazz) {
        if(members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        return members.stream()
            .map(value -> {
                try{
                    String jsonValue = (String) value;
                    return objectMapper.readValue(jsonValue, clazz);
                } catch (Exception e) {
                    log.warn("redis ZSet 멤버 역직렬화 오류 발생 - error : {}, value : {}", e.getMessage(), value);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    public void delete(String key){
        try{
            redisTemplate.delete(key);
            log.debug("redis 키 삭제 완료 - key: {}", key);
        } catch (Exception e) {
            log.warn("redis 키 삭제 중 오류 발생 - key: {}, error: {}", key, e.getMessage());
        }
    }

    protected void setTtl(String key, long ttlSeconds){
        try{
            redisTemplate.expire(key, Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            log.warn("redis 키 TTL 설정 중 오류 발생 - key: {}, error: {}", key, e.getMessage());
        }
    }
}
