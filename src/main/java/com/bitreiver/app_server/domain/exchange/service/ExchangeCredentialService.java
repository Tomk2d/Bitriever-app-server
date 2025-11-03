package com.bitreiver.app_server.domain.exchange.service;

import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialRequest;
import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialResponse;

import java.util.UUID;

public interface ExchangeCredentialService {
    ExchangeCredentialResponse createCredential(UUID userId, ExchangeCredentialRequest request);
    ExchangeCredentialResponse getCredential(UUID userId);
    ExchangeCredentialResponse updateCredential(UUID userId, ExchangeCredentialRequest request);
    void deleteCredential(UUID userId);
}

