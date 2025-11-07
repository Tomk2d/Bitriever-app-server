package com.bitreiver.app_server.domain.asset.service;

import com.bitreiver.app_server.domain.asset.dto.AssetResponse;

import java.util.List;
import java.util.UUID;

public interface AssetService {
    List<AssetResponse> getUserAssets(UUID userId);
    
    List<AssetResponse> getUserAssetsByExchange(UUID userId, Short exchangeCode);
    
    void syncAssets(UUID userId);
}

