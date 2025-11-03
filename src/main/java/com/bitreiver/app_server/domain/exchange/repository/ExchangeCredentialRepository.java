package com.bitreiver.app_server.domain.exchange.repository;

import com.bitreiver.app_server.domain.exchange.entity.ExchangeCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExchangeCredentialRepository extends JpaRepository<ExchangeCredential, UUID> {
    Optional<ExchangeCredential> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}

