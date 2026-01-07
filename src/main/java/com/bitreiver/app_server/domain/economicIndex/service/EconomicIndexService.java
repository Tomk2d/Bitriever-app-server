package com.bitreiver.app_server.domain.economicIndex.service;

import com.bitreiver.app_server.domain.economicIndex.dto.EconomicIndexResponse;
import com.bitreiver.app_server.domain.economicIndex.enums.EconomicIndexType;

import java.time.LocalDate;
import java.util.List;

public interface EconomicIndexService {
    
    List<EconomicIndexResponse> getByIndexType(EconomicIndexType indexType);
    
    List<EconomicIndexResponse> getByIndexTypeAndDateRange(
        EconomicIndexType indexType,
        LocalDate startDate,
        LocalDate endDate
    );

    EconomicIndexResponse getByIndexTypeAndDate(
        EconomicIndexType indexType,
        LocalDate date
    );
}