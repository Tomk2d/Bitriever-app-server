package com.bitreiver.app_server.domain.economicEvent.repository;

import com.bitreiver.app_server.domain.economicEvent.entity.EconomicEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface EconomicEventRepository extends JpaRepository<EconomicEvent, Long>, EconomicEventRepositoryEntityManager {
    @Query("SELECT DISTINCT e FROM EconomicEvent e " +
           "LEFT JOIN FETCH e.economicEventValue v " +
           "WHERE e.eventDate >= :startDate AND e.eventDate <= :endDate " +
           "ORDER BY e.eventDate ASC, e.countryType ASC")
    List<EconomicEvent> findByYearMonth(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(DISTINCT e) FROM EconomicEvent e " +
       "WHERE e.eventDate = :date")
    int countByEventDate(@Param("date") LocalDate date);
}
