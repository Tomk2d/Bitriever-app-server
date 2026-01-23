package com.bitreiver.app_server.global.security.oauth2;

import java.util.Map;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    
    private final Map<String, Object> attributes;
    
    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
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
        String name = (String) attributes.get("name");
        if (name != null && !name.isEmpty()) {
            return name;
        }
        // 이름이 없으면 이메일의 @ 앞부분 사용
        String email = getEmail();
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return null;
    }
    
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
