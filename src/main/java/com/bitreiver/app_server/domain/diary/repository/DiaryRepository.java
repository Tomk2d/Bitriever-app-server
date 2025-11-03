package com.bitreiver.app_server.domain.diary.repository;

import com.bitreiver.app_server.domain.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Integer> {
    
    @Query("SELECT d FROM Diary d JOIN TradingHistory t ON d.tradingHistoryId = t.id WHERE t.userId = :userId")
    List<Diary> findByUserId(@Param("userId") UUID userId);
    
    Optional<Diary> findByTradingHistoryId(Integer tradingHistoryId);
    
    @Query("SELECT d FROM Diary d JOIN TradingHistory t ON d.tradingHistoryId = t.id WHERE d.id = :id AND t.userId = :userId")
    Optional<Diary> findByIdAndUserId(@Param("id") Integer id, @Param("userId") UUID userId);
    
    boolean existsByTradingHistoryId(Integer tradingHistoryId);
    
    @Query("SELECT t, d FROM TradingHistory t LEFT JOIN Diary d ON d.tradingHistoryId = t.id " +
           "WHERE t.userId = :userId AND t.tradeTime >= :startDate AND t.tradeTime < :endDate " +
           "ORDER BY t.tradeTime DESC")
    List<Object[]> findTradingHistoriesWithDiariesByDateRange(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}

