package com.bitreiver.app_server.domain.longshort.service;

import java.util.List;

import com.bitreiver.app_server.domain.longshort.dto.LongShortResponse;

public interface LongShortService {
    List<LongShortResponse> getLongShortRatio(String symbol, String period);
    
}
