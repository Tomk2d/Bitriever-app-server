package com.bitreiver.app_server.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 인증 요청에 prompt 파라미터를 추가하여
 * 매번 계정 선택 화면을 표시하도록 하는 커스텀 리졸버
 * 
 * - 카카오, 네이버, 구글: prompt=select_account
 */
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
    
    private final OAuth2AuthorizationRequestResolver defaultResolver;
    
    public CustomOAuth2AuthorizationRequestResolver(OAuth2AuthorizationRequestResolver defaultResolver) {
        this.defaultResolver = defaultResolver;
    }
    
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(authorizationRequest);
    }
    
    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(authorizationRequest, clientRegistrationId);
    }
    
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        return customizeAuthorizationRequest(authorizationRequest, null);
    }
    
    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest, 
            String clientRegistrationId) {
        if (authorizationRequest == null) {
            return null;
        }
        
        // 기존 추가 파라미터를 가져오거나 새로 생성
        Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());
        
        // 카카오만 계정 선택 강제 파라미터 추가
        if (clientRegistrationId != null && "kakao".equalsIgnoreCase(clientRegistrationId)) {
            // 카카오: prompt=select_account 지원 (매번 계정 선택 화면 표시)
            additionalParameters.put("prompt", "select_account");
        }
        
        // 커스터마이징된 OAuth2AuthorizationRequest 반환
        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }
}
