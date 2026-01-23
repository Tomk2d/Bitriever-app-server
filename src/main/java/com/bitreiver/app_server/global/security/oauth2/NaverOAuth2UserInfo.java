package com.bitreiver.app_server.global.security.oauth2;

import java.util.Map;

public class NaverOAuth2UserInfo implements OAuth2UserInfo {
    
    private final Map<String, Object> attributes;
    
    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        // Naver는 response 객체로 감싸져 있음
        this.attributes = (Map<String, Object>) attributes.get("response");
    }
    
    @Override
    public String getProviderId() {
        if (attributes == null) {
            return null;
        }
        return (String) attributes.get("id");
    }
    
    @Override
    public String getEmail() {
        if (attributes == null) {
            return null;
        }
        return (String) attributes.get("email");
    }
    
    @Override
    public String getNickname() {
        if (attributes == null) {
            return null;
        }
        return (String) attributes.get("nickname");
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
