package com.bitreiver.app_server.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    // 기존 자산 동기화용 스레드 풀
    @Bean(name = "assetSyncExecutor")
    public Executor assetSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("asset-sync-");
        executor.initialize();
        return executor;
    }

    // 주가 폴링용 스레드 풀 (가벼운 작업이므로 작은 풀 크기)
    @Bean(name = "coinPriceExecutor")
    public Executor coinPriceExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);      // 기본 스레드 수
        executor.setMaxPoolSize(5);       // 최대 스레드 수
        executor.setQueueCapacity(50);    // 대기 큐 크기
        executor.setThreadNamePrefix("coin-price-");
        executor.initialize();
        return executor;
    }
}

