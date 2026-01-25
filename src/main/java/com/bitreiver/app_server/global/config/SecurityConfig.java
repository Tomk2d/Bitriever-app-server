package com.bitreiver.app_server.global.config;

import com.bitreiver.app_server.global.security.jwt.JwtAuthenticationFilter;
import com.bitreiver.app_server.global.security.oauth2.CustomOAuth2AuthorizationRequestResolver;
import com.bitreiver.app_server.global.security.oauth2.CustomOAuth2UserService;
import com.bitreiver.app_server.global.security.oauth2.CustomOidcUserService;
import com.bitreiver.app_server.global.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.bitreiver.app_server.global.security.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomOAuth2UserService customOAuth2UserService,
            CustomOidcUserService customOidcUserService,
            @Lazy OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
            OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customOAuth2UserService = customOAuth2UserService;
        this.customOidcUserService = customOidcUserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/oauth2/**",
                    "/login/oauth2/code/**",
                    "/api/coins/**",
                    "/api/coin-prices/day/**",
                    "/api/coin-prices/ticker/**",  // 코인 현재가 조회 API 
                    "/api/fear-greed/**",  // 공포/탐욕 지수 API  - /today, /history, /{date} 포함
                    "/api/redis-test/**",  // Redis 테스트용 
                    "/api/longshort/**",  // 롱숏 비율 조회 API 
                    "/api/articles/**",  // 기사 조회 API 
                    "/api/economic-indices/**",  // 경제 지표 조회 API 
                    "/api/economic-events/**",  // 경제 지표 이벤트 조회 API 
                    "/api/callback/**",  // fetch-server 콜백 API (내부용)
                    "/api/sse/**",  // SSE 엔드포인트 (인증 필요)
                    "/api/notifications/**",  // 알림 API (인증 필요)
                    "/ws/**",       // WebSocket 인증 없이 공개 (주가 데이터)
                    "/health", 
                    "/",
                    "/docs",
                    "/docs/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                // 커뮤니티 API - GET 요청만 permitAll, 나머지는 authenticated
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/communities/**").permitAll()
                .requestMatchers("/api/communities/**").authenticated()
                // 커뮤니티 댓글 API - GET 요청만 permitAll, 나머지는 authenticated
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/communities/*/comments/**").permitAll()
                .requestMatchers("/api/communities/*/comments/**").authenticated()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(oAuth2AuthorizationRequestResolver(clientRegistrationRepository))
                )
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                    .oidcUserService(customOidcUserService)
                )
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // setAllowedOriginPatterns를 사용하면 와일드카드와 credentials를 함께 사용 가능
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        // SockJS가 credentials를 포함할 수 있으므로 명시적으로 허용
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        // 기본 리졸버 생성
        OAuth2AuthorizationRequestResolver defaultResolver = 
            new org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
            );
        
        // 커스텀 리졸버로 래핑하여 prompt=select_account 추가
        return new CustomOAuth2AuthorizationRequestResolver(defaultResolver);
    }
}

