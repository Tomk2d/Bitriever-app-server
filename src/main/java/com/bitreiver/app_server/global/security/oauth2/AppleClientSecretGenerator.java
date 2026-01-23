package com.bitreiver.app_server.global.security.oauth2;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

/**
 * 애플 OAuth2 클라이언트 시크릿 생성기
 * 애플은 JWT 기반 클라이언트 인증을 사용하므로, private key로 JWT를 생성해야 함
 */
@Slf4j
@Component
public class AppleClientSecretGenerator {
    
    @Value("${apple.client-id:}")
    private String clientId;
    
    @Value("${apple.team-id:}")
    private String teamId;
    
    @Value("${apple.key-id:}")
    private String keyId;
    
    @Value("${apple.private-key:}")
    private String privateKey;
    
    /**
     * Private Key가 올바르게 설정되었는지 확인
     */
    public boolean isConfigured() {
        return clientId != null && !clientId.isEmpty() &&
               teamId != null && !teamId.isEmpty() &&
               keyId != null && !keyId.isEmpty() &&
               privateKey != null && !privateKey.isEmpty();
    }
    
    /**
     * 애플 클라이언트 시크릿 JWT 생성
     * 이 JWT는 6개월마다 갱신해야 함
     */
    public String generateClientSecret() {
        if (!isConfigured()) {
            throw new IllegalStateException("애플 OAuth2 설정이 완료되지 않았습니다. client-id, team-id, key-id, private-key를 확인하세요.");
        }
        
        try {
            // Private Key 파싱
            String privateKeyContent = privateKey
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            ECPrivateKey ecPrivateKey = (ECPrivateKey) keyFactory.generatePrivate(keySpec);
            
            // JWT 헤더 생성
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                .keyID(keyId)
                .type(JOSEObjectType.JWT)
                .build();
            
            // JWT 클레임 생성
            Instant now = Instant.now();
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(teamId)
                .subject(clientId)
                .audience("https://appleid.apple.com")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(15778463))) // 6개월 (182.5일)
                .build();
            
            // JWT 서명
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            JWSSigner signer = new ECDSASigner(ecPrivateKey);
            signedJWT.sign(signer);
            
            return signedJWT.serialize();
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | JOSEException e) {
            log.error("애플 클라이언트 시크릿 생성 실패", e);
            throw new RuntimeException("애플 클라이언트 시크릿 생성 실패", e);
        }
    }
}
