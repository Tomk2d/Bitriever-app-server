package com.bitreiver.app_server.global.security.oauth2;

import com.bitreiver.app_server.domain.user.dto.AuthResult;
import com.bitreiver.app_server.domain.user.dto.OAuth2CodePayload;
import com.bitreiver.app_server.domain.user.service.OAuth2CodeService;
import com.bitreiver.app_server.domain.user.service.UserService;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final UserService userService;
    private final OAuth2CodeService oAuth2CodeService;
    
    @Autowired
    public OAuth2AuthenticationSuccessHandler(@Lazy UserService userService,
                                              @Lazy OAuth2CodeService oAuth2CodeService) {
        this.userService = userService;
        this.oAuth2CodeService = oAuth2CodeService;
    }
    
    @Value("${oauth2.redirect.uri:http://localhost:3000/api/auth/callback}")
    private String redirectUri;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);
        
        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }
        
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        try {
            Object principal = authentication.getPrincipal();
            OAuth2UserInfo userInfo;
            String registrationId;
            
            // 구글과 애플은 OIDC를 사용하므로 OidcUser일 수 있음
            if (principal instanceof OidcUser) {
                OidcUser oidcUser = (OidcUser) principal;
                // OAuth2AuthenticationToken에서 registrationId 추출
                if (authentication instanceof OAuth2AuthenticationToken) {
                    OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                    registrationId = oauth2Token.getAuthorizedClientRegistrationId();
                } else {
                    registrationId = "google"; // 기본값
                }
                // registrationId에 따라 적절한 UserInfo 생성
                if ("apple".equals(registrationId)) {
                    userInfo = new AppleOAuth2UserInfo(oidcUser.getAttributes());
                } else {
                    userInfo = new GoogleOAuth2UserInfo(oidcUser.getAttributes());
                }
            } else if (principal instanceof CustomOAuth2User) {
                CustomOAuth2User customOAuth2User = (CustomOAuth2User) principal;
                userInfo = customOAuth2User.getOAuth2UserInfo();
                registrationId = customOAuth2User.getRegistrationId();
            } else if (principal instanceof OAuth2User) {
                // 일반 OAuth2User인 경우 (구글 OIDC가 아닌 경우)
                OAuth2User oAuth2User = (OAuth2User) principal;
                // OAuth2AuthenticationToken에서 registrationId 추출
                if (authentication instanceof OAuth2AuthenticationToken) {
                    OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
                    registrationId = oauth2Token.getAuthorizedClientRegistrationId();
                } else {
                    registrationId = extractRegistrationId(authentication);
                }
                userInfo = getOAuth2UserInfoFromAttributes(registrationId, oAuth2User.getAttributes());
            } else {
                throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getName());
            }
            
            String providerId = userInfo.getProviderId();
            String email = userInfo.getEmail();
            String nickname = userInfo.getNickname();
            
            // OAuth2 사용자 처리 및 JWT 토큰 발급
            AuthResult result = userService.processOAuth2User(registrationId, providerId, email, nickname);
            OAuth2CodePayload payload = OAuth2CodePayload.from(result);
            String code = oAuth2CodeService.save(payload);
            
            // 리다이렉트 URL 생성 (code와 사용자 정보만 전달, 토큰은 URL에 넣지 않음)
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUri + "/" + registrationId)
                .queryParam("code", code)
                .queryParam("userId", result.getAuthResponse().getUserId().toString())
                .queryParam("email", URLEncoder.encode(result.getAuthResponse().getEmail() != null ? result.getAuthResponse().getEmail() : "", StandardCharsets.UTF_8));
            
            if (result.getAuthResponse().getNickname() != null) {
                uriBuilder.queryParam("nickname", URLEncoder.encode(result.getAuthResponse().getNickname(), StandardCharsets.UTF_8));
            }
            if (result.getAuthResponse().getRequiresNickname() != null && result.getAuthResponse().getRequiresNickname()) {
                uriBuilder.queryParam("requiresNickname", "true");
            }
            
            String targetUrl = uriBuilder.build().toUriString();
            return targetUrl;
        } catch (Exception e) {
            log.error("OAuth2 user processing failed", e);
            
            // CustomException인 경우 에러 코드에 따라 처리
            String errorCode = "oauth2_authentication_failed";
            String errorMessage = e.getMessage();
            
            if (e instanceof CustomException) {
                CustomException customException = (CustomException) e;
                if (customException.getErrorCode() == ErrorCode.DUPLICATE_EMAIL) {
                    errorCode = "duplicate_email";
                    errorMessage = "이미 존재하는 이메일입니다.";
                } else {
                    errorCode = customException.getErrorCode().getCode().toLowerCase();
                    errorMessage = customException.getErrorCode().getMessage();
                }
            }
            
            // 에러 발생 시 로그인 페이지로 리다이렉트
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri.replace("/api/auth/callback", "/login"))
                .queryParam("error", errorCode)
                .queryParam("message", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
                .build().toUriString();
            return errorUrl;
        }
    }
    
    private String extractRegistrationId(Authentication authentication) {
        // Authentication의 details나 name에서 registrationId 추출
        String name = authentication.getName();
        if (name.contains("google")) {
            return "google";
        } else if (name.contains("kakao")) {
            return "kakao";
        } else if (name.contains("naver")) {
            return "naver";
        } else if (name.contains("apple")) {
            return "apple";
        }
        // 기본값으로 authentication name에서 추출 시도
        return name;
    }
    
    private OAuth2UserInfo getOAuth2UserInfoFromAttributes(String registrationId, java.util.Map<String, Object> attributes) {
        com.bitreiver.app_server.domain.user.enums.SnsProvider provider = 
            com.bitreiver.app_server.domain.user.enums.SnsProvider.fromRegistrationId(registrationId);
        
        return switch (provider) {
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            case NAVER -> new NaverOAuth2UserInfo(attributes);
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case APPLE -> new AppleOAuth2UserInfo(attributes);
        };
    }
}
