package com.bitreiver.app_server.domain.economicEvent.service;

import com.bitreiver.app_server.domain.economicEvent.dto.EconomicEventResponse;

import java.util.List;

public interface EconomicEventService {
    List<EconomicEventResponse> getEventsByYearMonth(String yearMonth);
}
