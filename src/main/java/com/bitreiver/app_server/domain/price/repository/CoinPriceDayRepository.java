package com.bitreiver.app_server.domain.price.repository;

import com.bitreiver.app_server.domain.price.entity.CoinPriceDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface CoinPriceDayRepository extends JpaRepository<CoinPriceDay, Integer> {
    Optional<CoinPriceDay> findById(Integer id);
    List<CoinPriceDay> findAllByCoinIdOrderByCandleDateTimeUtcDesc(Integer id);
    
    @Query("SELECT c FROM CoinPriceDay c WHERE c.coinId = :coinId " +
        "AND c.candleDateTimeUtc >= :startDate AND c.candleDateTimeUtc < :endDate " +
        "ORDER BY c.candleDateTimeUtc DESC")
    List<CoinPriceDay> findByCoinIdAndUtcDateRange(
        @Param("coinId") Integer coinId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}