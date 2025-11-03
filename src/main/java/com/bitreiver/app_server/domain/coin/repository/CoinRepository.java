package com.bitreiver.app_server.domain.coin.repository;

import com.bitreiver.app_server.domain.coin.entity.Coin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CoinRepository extends JpaRepository<Coin, Integer> {
    Optional<Coin> findByMarketCode(String marketCode);
    List<Coin> findByExchangeAndIsActive(String exchange, Boolean isActive);
    List<Coin> findAllByIsActive(Boolean isActive);
}

