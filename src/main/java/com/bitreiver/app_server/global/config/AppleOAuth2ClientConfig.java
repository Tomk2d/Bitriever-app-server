package com.bitreiver.app_server.global.config;

import com.bitreiver.app_server.global.security.oauth2.AppleClientSecretGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;

import java.util.ArrayList;
import java.util.List;

/**
 * 애플 OAuth2 클라이언트 설정
 * 애플은 JWT 기반 클라이언트 시크릿을 사용하므로 동적으로 생성해야 함
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.security.oauth2.client.registration.apple.client-id")
public class AppleOAuth2ClientConfig {
    
    private final AppleClientSecretGenerator appleClientSecretGenerator;
    
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            ClientRegistrationRepository defaultRepository) {
        
        List<ClientRegistration> registrations = new ArrayList<>();
        
        // 기존 등록 정보 가져오기
        if (defaultRepository instanceof InMemoryClientRegistrationRepository) {
            InMemoryClientRegistrationRepository inMemoryRepo = 
                (InMemoryClientRegistrationRepository) defaultRepository;
            inMemoryRepo.forEach(registrations::add);
        }
        
        // 애플 클라이언트 시크릿 동적 생성
        String appleClientSecret = appleClientSecretGenerator.generateClientSecret();
        
        // application.properties에서 애플 클라이언트 ID 가져오기
        String appleClientId = System.getProperty("apple.client-id");
        if (appleClientId == null || appleClientId.isEmpty()) {
            appleClientId = System.getenv("APPLE_CLIENT_ID");
        }
        
        // 애플 클라이언트 ID가 없으면 기존 등록만 반환
        if (appleClientId == null || appleClientId.isEmpty()) {
            log.warn("애플 클라이언트 ID가 설정되지 않았습니다. 애플 로그인을 사용할 수 없습니다.");
            return defaultRepository;
        }
        
        // 애플 클라이언트 등록 추가/업데이트
        ClientRegistration appleRegistration = ClientRegistration
            .withRegistrationId("apple")
            .clientId(appleClientId)
            .clientSecret(appleClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.PRIVATE_KEY_JWT)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "email", "name")
            .authorizationUri("https://appleid.apple.com/auth/authorize")
            .tokenUri("https://appleid.apple.com/auth/token")
            .userInfoUri("https://appleid.apple.com/auth/userinfo")
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .clientName("Apple")
            .build();
        
        // 기존 애플 등록이 있으면 제거
        registrations.removeIf(reg -> "apple".equals(reg.getRegistrationId()));
        registrations.add(appleRegistration);
        
        return new InMemoryClientRegistrationRepository(registrations);
    }
}
