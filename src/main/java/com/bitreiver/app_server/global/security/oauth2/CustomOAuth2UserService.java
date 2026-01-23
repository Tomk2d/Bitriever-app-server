package com.bitreiver.app_server.global.security.oauth2;

import com.bitreiver.app_server.domain.user.enums.SnsProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, attributes);
        
        return new CustomOAuth2User(oAuth2User, oAuth2UserInfo, registrationId);
    }
    
    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        SnsProvider provider = SnsProvider.fromRegistrationId(registrationId);
        
        return switch (provider) {
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            case NAVER -> new NaverOAuth2UserInfo(attributes);
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case APPLE -> new AppleOAuth2UserInfo(attributes);
        };
    }
}
