package com.bitreiver.app_server.domain.assetAnalysis.service;

import com.bitreiver.app_server.domain.assetAnalysis.dto.AssetAnalysisResponse;

import java.util.UUID;

public interface AssetAnalysisService {
    AssetAnalysisResponse getAssetAnalysis(UUID userId);
}
