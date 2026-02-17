package com.bitreiver.app_server.global.config;

import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {
    @Value("${external.upbit.api.url:https://api.upbit.com}")
    private String upbitApiUrl;
    
    @Value("${external.coinone.api.url:https://api.coinone.co.kr}")
    private String coinoneApiUrl;

    @Value("${external.ai-server.url:http://localhost:8000}")
    private String aiServerUrl;

    @Bean
    public WebClient aiServerWebClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("ai-server")
            .maxConnections(20)
            .maxIdleTime(Duration.ofSeconds(30))
            .maxLifeTime(Duration.ofSeconds(120))
            .build();
        HttpClient httpClient = HttpClient.create(connectionProvider)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .responseTimeout(Duration.ofSeconds(120))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(120, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS))
            );
        return WebClient.builder()
            .baseUrl(aiServerUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

    @Bean
    public WebClient upbitTickerWebClient() {
        // 연결 풀 설정
        ConnectionProvider connectionProvider = ConnectionProvider.builder("upbit-ticker")
            .maxConnections(50)
            .maxIdleTime(Duration.ofSeconds(10))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(10))
            .evictInBackground(Duration.ofSeconds(10))
            .build();

        // EventLoopGroup 명시적 설정 (DNS resolver executor 문제 해결)
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

        HttpClient httpClient = HttpClient.create(connectionProvider)
            .runOn(eventLoopGroup)  // EventLoopGroup 명시적 설정
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
            .responseTimeout(Duration.ofSeconds(5))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(2, TimeUnit.SECONDS))
            );
        
        return WebClient.builder()
            .baseUrl(upbitApiUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
    
    @Bean
    public WebClient coinoneTickerWebClient() {
        // 연결 풀 설정
        ConnectionProvider connectionProvider = ConnectionProvider.builder("coinone-ticker")
            .maxConnections(50)
            .maxIdleTime(Duration.ofSeconds(10))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(10))
            .evictInBackground(Duration.ofSeconds(10))
            .build();

        // EventLoopGroup 명시적 설정 (DNS resolver executor 문제 해결)
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

        HttpClient httpClient = HttpClient.create(connectionProvider)
            .runOn(eventLoopGroup)  // EventLoopGroup 명시적 설정
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
            .responseTimeout(Duration.ofSeconds(5))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(2, TimeUnit.SECONDS))
            );
        
        return WebClient.builder()
            .baseUrl(coinoneApiUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }

}
