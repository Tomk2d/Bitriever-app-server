package com.bitreiver.app_server.domain.exchange.service;

import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialRequest;
import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialResponse;
import com.bitreiver.app_server.domain.exchange.dto.RegisterAndSyncStartResponse;
import com.bitreiver.app_server.domain.exchange.dto.RegisterAndSyncStatusResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExchangeCredentialService {
    /**
     * 거래소 자격인증 등록 및 연동을 비동기로 시작. fetch-server를 호출하고 202 + job_id 반환.
     */
    RegisterAndSyncStartResponse startRegisterAndSync(UUID userId, ExchangeCredentialRequest request);

    /**
     * 등록·연동 작업 상태 조회 (폴링용). fetch-server에 프록시.
     */
    RegisterAndSyncStatusResponse getRegisterStatus(String jobId);

    /**
     * 거래소별 자격증명 저장 또는 수정 (동일 거래소가 있으면 수정, 없으면 생성)
     * @deprecated 자격인증 등록은 {@link #startRegisterAndSync} 사용. (동일 DB이므로 조회는 로컬 사용)
     */
    @Deprecated
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
