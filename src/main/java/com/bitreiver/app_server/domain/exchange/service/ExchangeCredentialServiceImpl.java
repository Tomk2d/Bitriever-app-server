package com.bitreiver.app_server.domain.exchange.service;

import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialRequest;
import com.bitreiver.app_server.domain.exchange.dto.ExchangeCredentialResponse;
import com.bitreiver.app_server.domain.exchange.dto.RegisterAndSyncStartResponse;
import com.bitreiver.app_server.domain.exchange.dto.RegisterAndSyncStatusResponse;
import com.bitreiver.app_server.domain.exchange.entity.ExchangeCredential;
import com.bitreiver.app_server.domain.exchange.enums.ExchangeType;
import com.bitreiver.app_server.domain.exchange.repository.ExchangeCredentialRepository;
import com.bitreiver.app_server.domain.user.entity.User;
import com.bitreiver.app_server.domain.user.repository.UserRepository;
import com.bitreiver.app_server.global.common.exception.CustomException;
import com.bitreiver.app_server.global.common.exception.ErrorCode;
import com.bitreiver.app_server.global.util.EncryptionUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeCredentialServiceImpl implements ExchangeCredentialService {

    private final ExchangeCredentialRepository exchangeCredentialRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${external.fetch.server.url}")
    private String fetchServerUrl;

    @Override
    public RegisterAndSyncStartResponse startRegisterAndSync(UUID userId, ExchangeCredentialRequest request) {
        String url = fetchServerUrl + "/api/exchange-credentials/register-and-sync/async";
        Map<String, Object> body = Map.of(
            "user_id", userId.toString(),
            "exchange_provider", request.getExchangeProvider(),
            "access_key", request.getAccessKey(),
            "secret_key", request.getSecretKey()
        );
        ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !Boolean.TRUE.equals(responseBody.get("success"))) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "거래소 등록 요청에 실패했습니다.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        if (data == null || !data.containsKey("job_id")) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "job_id를 받지 못했습니다.");
        }
        String jobId = String.valueOf(data.get("job_id"));
        return RegisterAndSyncStartResponse.builder().jobId(jobId).build();
    }

    @Override
    public RegisterAndSyncStatusResponse getRegisterStatus(String jobId) {
        try {
            String url = fetchServerUrl + "/api/exchange-credentials/register-status?job_id=" + jobId;
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, null, Map.class);
            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !Boolean.TRUE.equals(responseBody.get("success"))) {
                throw new CustomException(ErrorCode.NOT_FOUND, "작업을 찾을 수 없거나 만료되었습니다.");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            if (data == null) {
                throw new CustomException(ErrorCode.NOT_FOUND, "작업을 찾을 수 없습니다.");
            }
            return RegisterAndSyncStatusResponse.builder()
                .status((String) data.get("status"))
                .userId((String) data.get("userId"))
                .exchangeProvider(data.get("exchangeProvider") != null ? ((Number) data.get("exchangeProvider")).shortValue() : null)
                .exchangeName((String) data.get("exchangeName"))
                .result((Map<String, Object>) data.get("result"))
                .error((String) data.get("error"))
                .errorCode((String) data.get("errorCode"))
                .message((String) data.get("message"))
                .build();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 404) {
                throw new CustomException(ErrorCode.NOT_FOUND, "작업을 찾을 수 없거나 만료되었습니다.");
            }
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "상태 조회에 실패했습니다: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ExchangeCredentialResponse saveCredential(UUID userId, ExchangeCredentialRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다."));

        String encryptedAccessKey = EncryptionUtil.encrypt(request.getAccessKey());
        String encryptedSecretKey = EncryptionUtil.encrypt(request.getSecretKey());

        Optional<ExchangeCredential> existing = exchangeCredentialRepository
            .findByUserIdAndExchangeProvider(userId, request.getExchangeProvider());

        ExchangeCredential credential;
        if (existing.isPresent()) {
            credential = existing.get();
            credential.setEncryptedAccessKey(encryptedAccessKey);
            credential.setEncryptedSecretKey(encryptedSecretKey);
            credential.updateTimestamp();
        } else {
            credential = ExchangeCredential.builder()
                .userId(userId)
                .exchangeProvider(request.getExchangeProvider())
                .encryptedAccessKey(encryptedAccessKey)
                .encryptedSecretKey(encryptedSecretKey)
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .build();
        }

        ExchangeCredential saved = exchangeCredentialRepository.save(credential);

        ExchangeType exchangeType = ExchangeType.fromCode(request.getExchangeProvider());
        String providerName = exchangeType.getName();
        List<String> currentExchanges = parseConnectedExchanges(user.getConnectedExchanges());
        if (!currentExchanges.contains(providerName)) {
            currentExchanges.add(providerName);
        }
        user.setIsConnectExchange(true);
        user.setConnectedExchanges(serializeConnectedExchanges(currentExchanges));
        userRepository.save(user);

        log.info("saveCredential - 사용자 {}의 {} 자격증명 저장/수정 완료", userId, providerName);
        return ExchangeCredentialResponse.from(saved);
    }

    @Override
    public Optional<ExchangeCredentialResponse> getCredential(UUID userId, Short exchangeProvider) {
        return exchangeCredentialRepository.findByUserIdAndExchangeProvider(userId, exchangeProvider)
            .map(ExchangeCredentialResponse::from);
    }

    @Override
    public List<ExchangeCredentialResponse> getAllCredentials(UUID userId) {
        List<ExchangeCredential> list = exchangeCredentialRepository.findByUserId(userId);
        List<ExchangeCredentialResponse> result = new ArrayList<>();
        for (ExchangeCredential c : list) {
            result.add(ExchangeCredentialResponse.from(c));
        }
        return result;
    }

    @Override
    @Transactional
    public ExchangeCredentialResponse updateCredential(UUID userId, ExchangeCredentialRequest request) {
        ExchangeCredential credential = exchangeCredentialRepository
            .findByUserIdAndExchangeProvider(userId, request.getExchangeProvider())
            .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_CREDENTIAL_NOT_FOUND, "해당 거래소 자격증명을 찾을 수 없습니다."));

        String encryptedAccessKey = EncryptionUtil.encrypt(request.getAccessKey());
        String encryptedSecretKey = EncryptionUtil.encrypt(request.getSecretKey());
        credential.setEncryptedAccessKey(encryptedAccessKey);
        credential.setEncryptedSecretKey(encryptedSecretKey);
        credential.updateTimestamp();
        exchangeCredentialRepository.save(credential);

        log.info("updateCredential - 사용자 {}의 거래소 {} 자격증명 수정 완료", userId, request.getExchangeProvider());
        return ExchangeCredentialResponse.from(credential);
    }

    @Override
    @Transactional
    public void deleteCredential(UUID userId, Short exchangeProvider) {
        Optional<ExchangeCredential> credentialOpt = exchangeCredentialRepository
            .findByUserIdAndExchangeProvider(userId, exchangeProvider);
        if (credentialOpt.isEmpty()) {
            throw new CustomException(ErrorCode.EXCHANGE_CREDENTIAL_NOT_FOUND, "해당 거래소 자격증명을 찾을 수 없습니다.");
        }
        exchangeCredentialRepository.delete(credentialOpt.get());

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            ExchangeType exchangeType = ExchangeType.fromCode(exchangeProvider);
            String providerName = exchangeType.getName();
            List<String> currentExchanges = parseConnectedExchanges(user.getConnectedExchanges());
            currentExchanges.remove(providerName);
            if (currentExchanges.isEmpty()) {
                user.setIsConnectExchange(false);
            }
            user.setConnectedExchanges(serializeConnectedExchanges(currentExchanges));
            userRepository.save(user);
        }
        log.info("deleteCredential - 사용자 {}의 거래소 {} 자격증명 삭제 완료", userId, exchangeProvider);
    }

    private List<String> parseConnectedExchanges(String connectedExchangesJson) {
        if (connectedExchangesJson == null || connectedExchangesJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(connectedExchangesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("parseConnectedExchanges 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String serializeConnectedExchanges(List<String> exchanges) {
        try {
            return objectMapper.writeValueAsString(exchanges);
        } catch (Exception e) {
            log.warn("serializeConnectedExchanges 실패: {}", e.getMessage());
            return "[]";
        }
    }
}
