package com.bitreiver.app_server.global.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionUtil {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    
    private static SecretKey getSecretKey() {
        String secretKey = System.getenv("ENCRYPTION_KEY");
        if (secretKey == null || secretKey.isEmpty()) {
            secretKey = "dev-encryption-key-for-testing-only-32chars";
        }
        // AES-256을 위해서는 키가 32 bytes여야 함
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 32) {
            // 키를 32 bytes로 패딩하거나 자름
            byte[] fixedKey = new byte[32];
            System.arraycopy(keyBytes, 0, fixedKey, 0, Math.min(keyBytes.length, 32));
            return new SecretKeySpec(fixedKey, ALGORITHM);
        }
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
    
    public static String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("암호화 중 오류 발생", e);
        }
    }
    
    public static String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("복호화 중 오류 발생", e);
        }
    }
}

