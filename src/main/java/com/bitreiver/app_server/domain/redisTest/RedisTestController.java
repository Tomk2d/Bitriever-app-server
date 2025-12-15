package com.bitreiver.app_server.domain.redisTest;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/redis-test")
@RequiredArgsConstructor
public class RedisTestController {
    
    // Master (쓰기용) - @Primary로 기본값
    private final StringRedisTemplate stringRedisTemplate;
    
    // Replica (읽기용)
    @Qualifier("stringRedisReadTemplate")
    private final StringRedisTemplate stringRedisReadTemplate;

    @PostMapping("/set")
    public String setValue(@RequestParam("key") String key, @RequestParam("value") String value) {
        // Master에 쓰기
        stringRedisTemplate.opsForValue().set(key, value);
        return "OK (Master): " + key + " = " + value;
    }

    @GetMapping("/get")
    public String getValue(@RequestParam("key") String key) {
        // Replica에서 읽기
        String value = stringRedisReadTemplate.opsForValue().get(key);
        return value != null ? "Value (Replica): " + value : "Key not found";
    }
    
    @GetMapping("/get-from-master")
    public String getValueFromMaster(@RequestParam("key") String key) {
        // Master에서 읽기 (비교용)
        String value = stringRedisTemplate.opsForValue().get(key);
        return value != null ? "Value (Master): " + value : "Key not found";
    }
}
