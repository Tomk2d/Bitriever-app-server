package com.bitreiver.app_server.domain.trading.repository;

import com.bitreiver.app_server.domain.trading.entity.TradingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TradingHistoryRepository extends JpaRepository<TradingHistory, Integer> {
    Page<TradingHistory> findByUserIdOrderByTradeTimeDesc(UUID userId, Pageable pageable);
    
    @Query("SELECT t FROM TradingHistory t WHERE t.userId = :userId " +
           "AND t.tradeTime >= :startDate AND t.tradeTime < :endDate " +
           "ORDER BY t.tradeTime DESC")
    List<TradingHistory> findByUserIdAndTradeTimeBetween(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    List<TradingHistory> findByUserId(UUID userId);
    long countByUserId(UUID userId);
}

