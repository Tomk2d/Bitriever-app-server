package com.bitreiver.app_server.global.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);  // 10초
        factory.setReadTimeout(120000);    // 120초 (2분) - 최초 동기화 시 오래 걸릴 수 있음
        
        return builder
            .requestFactory(() -> factory)
            .setConnectTimeout(Duration.ofSeconds(10))
            .setReadTimeout(Duration.ofSeconds(120))  // 최초 동기화 시 2017년부터 조회하면 오래 걸림
            .build();
    }
}

