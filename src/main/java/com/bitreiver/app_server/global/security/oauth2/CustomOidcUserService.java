package com.bitreiver.app_server.global.security.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

/**
 * 구글 OIDC 사용자 정보를 처리하는 커스텀 서비스
 * 구글은 OpenID Connect를 사용하므로 OidcUserService를 사용해야 함
 * OidcUser는 그대로 반환하고, OAuth2AuthenticationSuccessHandler에서 처리
 */
@Slf4j
@Service
public class CustomOidcUserService extends OidcUserService {
    
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 OidcUserService의 loadUser를 호출하여 OidcUser 반환
        // OAuth2AuthenticationSuccessHandler에서 OidcUser를 처리하도록 함
        return super.loadUser(userRequest);
    }
}
