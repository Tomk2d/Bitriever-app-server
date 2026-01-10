package com.bitreiver.app_server.domain.economicEvent.repository;

import com.bitreiver.app_server.domain.economicEvent.entity.EconomicEvent;
import java.time.LocalDate;
import java.util.List;

public interface EconomicEventRepositoryEntityManager {
    List<EconomicEvent> findUpcomingEvents(LocalDate today, int limit);
}
