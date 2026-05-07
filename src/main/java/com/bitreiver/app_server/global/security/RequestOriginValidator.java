package com.bitreiver.app_server.global.security;

import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RequestOriginValidator {
    private final Set<String> allowedOrigins;

    public RequestOriginValidator(@Value("${security.cors.allowed-origins:http://localhost:3000}") String allowedOriginsProperty) {
        this.allowedOrigins = Arrays.stream(allowedOriginsProperty.split(","))
            .map(String::trim)
            .filter(StringUtils::hasText)
            .collect(Collectors.toSet());
    }

    public void validateOrThrow(HttpServletRequest request) {
        if (allowedOrigins.contains("*")) return;
        String sourceOrigin = extractOrigin(request.getHeader("Origin"));
        if (!StringUtils.hasText(sourceOrigin)) {
            sourceOrigin = extractOrigin(request.getHeader("Referer"));
        }
        if (!StringUtils.hasText(sourceOrigin) || !allowedOrigins.contains(sourceOrigin)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
    }

    private String extractOrigin(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            URI uri = URI.create(value);
            StringBuilder origin = new StringBuilder(uri.getScheme())
                .append("://")
                .append(uri.getHost());
            if (uri.getPort() != -1) {
                origin.append(":").append(uri.getPort());
            }
            return origin.toString();
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
