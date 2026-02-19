package com.bitreiver.app_server.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    
    @Value("${oauth2.redirect.uri:http://localhost:3000/api/auth/callback}")
    private String redirectUri;

    private static final String OAUTH2_REDIRECT_URI_REQUIRED_SENTINEL = "__OAUTH2_REDIRECT_URI_REQUIRED__";
    
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.error("OAuth2 authentication failed: {}", exception.getMessage());
        if (redirectUri == null || redirectUri.isBlank() || redirectUri.contains(OAUTH2_REDIRECT_URI_REQUIRED_SENTINEL)) {
            log.error("OAUTH2_REDIRECT_URI must be set in production. Cannot redirect after OAuth failure.");
            throw new IllegalStateException("OAUTH2_REDIRECT_URI must be set in production.");
        }
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri.replace("/api/auth/callback", "/login"))
            .queryParam("error", URLEncoder.encode("oauth2_authentication_failed", StandardCharsets.UTF_8))
            .queryParam("message", URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8))
            .build().toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
