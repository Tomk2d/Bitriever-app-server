package com.bitreiver.app_server.global.security.oauth2;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {
    
    private final OAuth2User oAuth2User;
    private final OAuth2UserInfo oAuth2UserInfo;
    private final String registrationId;
    
    public CustomOAuth2User(OAuth2User oAuth2User, OAuth2UserInfo oAuth2UserInfo, String registrationId) {
        this.oAuth2User = oAuth2User;
        this.oAuth2UserInfo = oAuth2UserInfo;
        this.registrationId = registrationId;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2User.getAttributes();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return oAuth2User.getAuthorities();
    }
    
    @Override
    public String getName() {
        return oAuth2UserInfo.getProviderId();
    }
}
