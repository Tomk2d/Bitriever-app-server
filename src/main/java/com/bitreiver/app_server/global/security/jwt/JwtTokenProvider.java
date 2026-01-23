package com.bitreiver.app_server.global.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;
    
    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;
    
    public String generateAccessToken(UUID userId) {
        return generateToken(userId, accessTokenValidity);
    }
    
    public String generateRefreshToken(UUID userId) {
        return generateToken(userId, refreshTokenValidity);
    }
    
    private String generateToken(UUID userId, long validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity);
        
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact();
    }
    
    public UUID getUserIdFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        
        String userIdStr = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
            
        return UUID.fromString(userIdStr);
    }
    
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isTokenExpired(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 토큰의 만료 시간을 반환
     * @param token JWT 토큰
     * @return 만료 시간 (Date), 토큰이 유효하지 않으면 null
     */
    public Date getTokenExpiration(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 토큰의 남은 유효기간을 밀리초로 반환
     * @param token JWT 토큰
     * @return 남은 유효기간 (밀리초), 토큰이 만료되었거나 유효하지 않으면 0
     */
    public long getRemainingTime(String token) {
        try {
            Date expiration = getTokenExpiration(token);
            if (expiration == null) {
                return 0;
            }
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }
}

