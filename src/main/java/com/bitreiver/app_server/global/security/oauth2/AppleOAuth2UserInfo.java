package com.bitreiver.app_server.global.security.oauth2;

import java.util.Map;

public class AppleOAuth2UserInfo implements OAuth2UserInfo {
    
    private final Map<String, Object> attributes;
    
    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }
    
    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
    
    @Override
    public String getNickname() {
        // Apple은 닉네임을 제공하지 않으므로 이메일 기반으로 생성
        String email = getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "Apple User";
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
