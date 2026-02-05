package com.bitreiver.app_server.domain.exchange.service;

import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialRequest;
import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExchangeCredentialService {
    /**
     * 거래소별 자격증명 저장 또는 수정 (동일 거래소가 있으면 수정, 없으면 생성)
     */
    ExchangeCredentialResponse saveCredential(UUID userId, ExchangeCredentialRequest request);

    Optional<ExchangeCredentialResponse> getCredential(UUID userId, Short exchangeProvider);

    List<ExchangeCredentialResponse> getAllCredentials(UUID userId);

    /**
     * 특정 거래소 자격증명 수정
     */
    ExchangeCredentialResponse updateCredential(UUID userId, ExchangeCredentialRequest request);

    /**
     * 특정 거래소 자격증명 삭제
     */
    void deleteCredential(UUID userId, Short exchangeProvider);
}
