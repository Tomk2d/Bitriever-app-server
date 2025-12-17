package com.bitreiver.app_server.global.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Slf4j
@Service
public class RedisCacheZSetService extends RedisCacheBaseService {
    
    @Value("${cache.zset.day.ttl:86400}")
    private long dayTtl;

    public RedisCacheZSetService(
                ObjectMapper objectMapper, 
                StringRedisTemplate stringRedisTemplate, 
                StringRedisTemplate stringRedisReadTemplate, 
                RedisTemplate<String, Object> redisTemplate, 
                RedisTemplate<String, Object> redisReadTemplate){
        super(objectMapper, stringRedisTemplate, stringRedisReadTemplate, redisTemplate, redisReadTemplate);
    }

    /**
     * ZSet 에 단일 멤버 추가
    */
    public void zAdd(String key, Object value, double score) {
        try{
            String jsonValue = serialize(value);
            redisTemplate.opsForZSet().add(key, jsonValue, score);
        } catch (Exception e) {
            log.warn("redis ZSet 멤버 추가 중 오류 발생 - key: {}, error: {}", key, e.getMessage());
        }
    }

    /**
     * ZSet 에 다수 멤버 추가
    */
    public <T> void zAddAll(String key, List<T> values, Function<T, Double> scoreExtractor, long ttlSeconds) {
        if (values == null || values.isEmpty()) {
            log.debug("ZSET 일괄 추가할 데이터가 없습니다 - key: {}", key);
            return;
        }
        
        try {
            // 기존 데이터 삭제
            redisTemplate.delete(key);
            
            int count = 0;
            for (T value : values) {
                try {
                    String jsonValue = serialize(value);
                    double score = scoreExtractor.apply(value);
                    redisTemplate.opsForZSet().add(key, jsonValue, score);
                    count++;
                } catch (Exception e) {
                    log.warn("ZSET 항목 추가 중 오류 - key: {}, error: {}", key, e.getMessage());
                }
            }
            
            // TTL 설정
            if (ttlSeconds > 0) {
                setTtl(key, ttlSeconds);
            }
            
            log.debug("ZSET 일괄 추가 완료 - key: {}, count: {}", key, count);
        } catch (Exception e) {
            log.error("ZSET 일괄 추가 실패 - key: {}, error: {}", key, e.getMessage(), e);
        }
    }

    /**
     * ZSET 일괄 추가 (기본 TTL 사용)
     */
    public <T> void zAddAll(String key, List<T> values, Function<T, Double> scoreExtractor) {
        zAddAll(key, values, scoreExtractor, dayTtl);
    }

    /**
     * ZSet 에 범위 조회
    */
    public <T> List<T> zRangeByScore(String key, double minScore, double maxScore, Class<T> clazz) {
        try {
            Set<Object> values = redisReadTemplate.opsForZSet()
                .rangeByScore(key, minScore, maxScore);
            
            if (values == null || values.isEmpty()) {
                log.debug("ZSET 범위 조회 결과 없음 - key: {}, min: {}, max: {}", key, minScore, maxScore);
                return Collections.emptyList();
            }
            
            List<T> result = convertZSetMembersToObjects(values, clazz);
            return result;
        } catch (Exception e) {
            log.warn("ZSET 범위 조회 중 오류 발생 - key: {}, error: {}", key, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * ZSET 전체 조회
     */
    public <T> List<T> zRangeAll(String key, Class<T> clazz) {
        try {
            Set<Object> values = redisReadTemplate.opsForZSet().range(key, 0, -1);
            
            if (values == null || values.isEmpty()) {
                log.debug("ZSET 전체 조회 결과 없음 - key: {}", key);
                return Collections.emptyList();
            }
            
            List<T> result = convertZSetMembersToObjects(values, clazz);
            log.debug("ZSET 전체 조회 완료 - key: {}, count: {}", key, result.size());
            return result;
        } catch (Exception e) {
            log.warn("ZSET 전체 조회 중 오류 발생 - key: {}, error: {}", key, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * ZSET 개수 조회
     */
    public Long zCard(String key) {
        try {
            Long count = redisReadTemplate.opsForZSet().zCard(key);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.warn("ZSET 개수 조회 중 오류 발생 - key: {}, error: {}", key, e.getMessage());
            return 0L;
        }
    }
    
    /**
     * ZSET 존재 여부 확인
     */
    public boolean zExists(String key) {
        try {
            Long count = redisReadTemplate.opsForZSet().zCard(key);
            return count != null && count > 0;
        } catch (Exception e) {
            log.warn("ZSET 존재 확인 중 오류 발생 - key: {}, error: {}", key, e.getMessage());
            return false;
        }
    }
    
    /**
     * ZSET에서 특정 score의 항목 조회
     */
    public <T> List<T> zRangeByScore(String key, double score, Class<T> clazz) {
        return zRangeByScore(key, score, score, clazz);
    }
}
