package com.bitreiver.app_server.domain.asset.repository;

import com.bitreiver.app_server.domain.asset.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {
    List<Asset> findByUserId(UUID userId);
    
    List<Asset> findByUserIdAndExchangeCode(UUID userId, Short exchangeCode);
}

