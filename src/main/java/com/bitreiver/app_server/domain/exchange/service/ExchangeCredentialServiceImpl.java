package com.bitreiver.app_server.domain.exchange.service;

import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialRequest;
import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialResponse;
import com.bitreiver.app_server.domain.exchange.entity.ExchangeCredential;
import com.bitreiver.app_server.domain.exchange.repository.ExchangeCredentialRepository;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import com.bitreiver.app_server.global.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExchangeCredentialServiceImpl implements ExchangeCredentialService {
    
    private final ExchangeCredentialRepository exchangeCredentialRepository;
    
    @Override
    @Transactional
    public ExchangeCredentialResponse createCredential(UUID userId, ExchangeCredentialRequest request) {
        if (exchangeCredentialRepository.existsByUserId(userId)) {
            throw new CustomException(ErrorCode.EXCHANGE_CREDENTIAL_NOT_FOUND, "userId");
        }
        
        String encryptedAccessKey = EncryptionUtil.encrypt(request.getAccessKey());
        String encryptedSecretKey = EncryptionUtil.encrypt(request.getSecretKey());
        
        ExchangeCredential credential = ExchangeCredential.builder()
            .userId(userId)
            .exchangeProvider(request.getExchangeProvider())
            .encryptedAccessKey(encryptedAccessKey)
            .encryptedSecretKey(encryptedSecretKey)
            .createdAt(LocalDateTime.now())
            .lastUpdatedAt(LocalDateTime.now())
            .build();
        
        exchangeCredentialRepository.save(credential);
        
        return ExchangeCredentialResponse.from(credential);
    }
    
    @Override
    public ExchangeCredentialResponse getCredential(UUID userId) {
        ExchangeCredential credential = exchangeCredentialRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_CREDENTIAL_NOT_FOUND));
        
        return ExchangeCredentialResponse.from(credential);
    }
    
    @Override
    @Transactional
    public ExchangeCredentialResponse updateCredential(UUID userId, ExchangeCredentialRequest request) {
        ExchangeCredential credential = exchangeCredentialRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_CREDENTIAL_NOT_FOUND));
        
        String encryptedAccessKey = EncryptionUtil.encrypt(request.getAccessKey());
        String encryptedSecretKey = EncryptionUtil.encrypt(request.getSecretKey());
        
        credential.setEncryptedAccessKey(encryptedAccessKey);
        credential.setEncryptedSecretKey(encryptedSecretKey);
        credential.updateTimestamp();
        
        exchangeCredentialRepository.save(credential);
        
        return ExchangeCredentialResponse.from(credential);
    }
    
    @Override
    @Transactional
    public void deleteCredential(UUID userId) {
        ExchangeCredential credential = exchangeCredentialRepository.findByUserId(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_CREDENTIAL_NOT_FOUND));
        
        exchangeCredentialRepository.delete(credential);
    }
}

