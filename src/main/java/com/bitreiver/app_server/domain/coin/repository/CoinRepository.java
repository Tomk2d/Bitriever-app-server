package com.bitreiver.app_server.domain.coin.repository;

import com.bitreiver.app_server.domain.coin.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoinRepository extends JpaRepository<Coin, Integer> {
    List<Coin> findByExchangeAndIsActive(String exchange, Boolean isActive);
    List<Coin> findAllByIsActive(Boolean isActive);
    
    // Postgresql 의 Partial Index로 quoteCurrency 와 isActive 조건으로 조회
    // id 순서로 정렬하여 일관된 순서 보장
    @Query("SELECT c FROM Coin c WHERE c.quoteCurrency = :quoteCurrency AND c.isActive = :isActive ORDER BY c.id ASC")
    List<Coin> findAllByQuoteCurrencyAndIsActive(@Param("quoteCurrency") String quoteCurrency, @Param("isActive") Boolean isActive);
}

