package com.bitreiver.app_server.global.security.oauth2;

import java.util.Map;

public interface OAuth2UserInfo {
    String getProviderId();
    String getEmail();
    String getNickname();
    Map<String, Object> getAttributes();
}
