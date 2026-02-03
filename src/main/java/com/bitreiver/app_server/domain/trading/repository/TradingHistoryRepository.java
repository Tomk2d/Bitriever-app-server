package com.bitreiver.app_server.domain.trading.repository;

import com.bitreiver.app_server.domain.trading.entity.TradingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
    
    // 매도 거래만 필터링
    @Query("SELECT t FROM TradingHistory t WHERE t.userId = :userId " +
           "AND t.tradeType = 1 AND t.profitLossRate IS NOT NULL " +
           "ORDER BY t.tradeTime DESC")
    List<TradingHistory> findSellTradesByUserId(@Param("userId") UUID userId);
    
    // 월별 매수/매도 금액 집계
    @Query(value = "SELECT CAST(EXTRACT(YEAR FROM t.trade_time) AS INTEGER) as year, " +
           "CAST(EXTRACT(MONTH FROM t.trade_time) AS INTEGER) as month, " +
           "t.trade_type, SUM(t.total_price) as totalAmount " +
           "FROM trading_histories t WHERE t.user_id = :userId " +
           "GROUP BY EXTRACT(YEAR FROM t.trade_time), EXTRACT(MONTH FROM t.trade_time), t.trade_type " +
           "ORDER BY year, month", nativeQuery = true)
    List<Object[]> findMonthlyTradeAmountsByUserId(@Param("userId") UUID userId);
    
    // 시간대별 거래 빈도 집계
    @Query(value = "SELECT CAST(EXTRACT(HOUR FROM t.trade_time) AS INTEGER) as hour, COUNT(t) as count " +
           "FROM trading_histories t WHERE t.user_id = :userId " +
           "GROUP BY EXTRACT(HOUR FROM t.trade_time) " +
           "ORDER BY hour", nativeQuery = true)
    List<Object[]> findHourlyFrequencyByUserId(@Param("userId") UUID userId);
    
    // 요일별 거래 빈도 집계 (0=일요일, 6=토요일)
    // PostgreSQL: EXTRACT(DOW FROM date) returns 0(Sunday) to 6(Saturday)
    @Query(value = "SELECT CAST(EXTRACT(DOW FROM t.trade_time) AS INTEGER) as dayOfWeek, COUNT(t) as count " +
           "FROM trading_histories t WHERE t.user_id = :userId " +
           "GROUP BY EXTRACT(DOW FROM t.trade_time) " +
           "ORDER BY dayOfWeek", nativeQuery = true)
    List<Object[]> findDayOfWeekFrequencyByUserId(@Param("userId") UUID userId);
    
    // 월별 거래 횟수 추이
    @Query(value = "SELECT CAST(EXTRACT(YEAR FROM t.trade_time) AS INTEGER) as year, " +
           "CAST(EXTRACT(MONTH FROM t.trade_time) AS INTEGER) as month, COUNT(t) as count " +
           "FROM trading_histories t WHERE t.user_id = :userId " +
           "GROUP BY EXTRACT(YEAR FROM t.trade_time), EXTRACT(MONTH FROM t.trade_time) " +
           "ORDER BY year, month", nativeQuery = true)
    List<Object[]> findMonthlyFrequencyByUserId(@Param("userId") UUID userId);
    
    // 코인별 누적 수익 금액 계산 (매도 거래만)
    @Query(value = "SELECT t.coin_id, " +
           "SUM((t.price - t.avg_buy_price) * t.quantity) as totalProfit, " +
           "AVG(t.profit_loss_rate) as avgProfitRate, COUNT(t) as sellCount " +
           "FROM trading_histories t WHERE t.user_id = :userId " +
           "AND t.trade_type = 1 AND t.profit_loss_rate IS NOT NULL AND t.avg_buy_price IS NOT NULL " +
           "GROUP BY t.coin_id " +
           "ORDER BY totalProfit DESC", nativeQuery = true)
    List<Object[]> findCoinProfitByUserId(@Param("userId") UUID userId);
    
    // 거래소별 통계 집계
    @Query(value = "SELECT t.exchange_code, COUNT(t) as tradeCount, SUM(t.total_price) as totalAmount, " +
           "AVG(t.total_price) as avgAmount, " +
           "SUM(CASE WHEN t.trade_type = 1 THEN 1 ELSE 0 END) as sellCount, " +
           "AVG(CASE WHEN t.trade_type = 1 AND t.profit_loss_rate IS NOT NULL THEN t.profit_loss_rate ELSE NULL END) as avgProfitRate " +
           "FROM trading_histories t WHERE t.user_id = :userId " +
           "GROUP BY t.exchange_code", nativeQuery = true)
    List<Object[]> findExchangeStatsByUserId(@Param("userId") UUID userId);
    
    // 거래소별 매도 거래 평균 수익률
    @Query(value = "SELECT t.exchange_code, AVG(t.profit_loss_rate) as avgProfitRate " +
           "FROM trading_histories t WHERE t.user_id = :userId " +
           "AND t.trade_type = 1 AND t.profit_loss_rate IS NOT NULL " +
           "GROUP BY t.exchange_code", nativeQuery = true)
    List<Object[]> findExchangeAverageProfitRateByUserId(@Param("userId") UUID userId);
    
    // 코인별 매도 거래 조회 (보유 기간 계산용)
    @Query("SELECT t FROM TradingHistory t WHERE t.userId = :userId " +
           "AND t.coinId = :coinId AND t.tradeType = 1 " +
           "ORDER BY t.tradeTime ASC")
    List<TradingHistory> findSellTradesByUserIdAndCoinId(
        @Param("userId") UUID userId,
        @Param("coinId") Integer coinId
    );
    
    // 코인별 매수 거래 조회 (보유 기간 계산용)
    @Query("SELECT t FROM TradingHistory t WHERE t.userId = :userId " +
           "AND t.coinId = :coinId AND t.tradeType = 0 " +
           "ORDER BY t.tradeTime ASC")
    List<TradingHistory> findBuyTradesByUserIdAndCoinId(
        @Param("userId") UUID userId,
        @Param("coinId") Integer coinId
    );
    
    // 전체 거래 통계
    @Query(value = "SELECT CAST(COUNT(t) AS BIGINT) as totalCount, " +
           "AVG(t.total_price) as avgAmount, " +
           "MIN(t.total_price) as minAmount, " +
           "MAX(t.total_price) as maxAmount " +
           "FROM trading_histories t WHERE t.user_id = :userId", nativeQuery = true)
    Object[] findTradeStatisticsByUserId(@Param("userId") UUID userId);
    
    // 매도 거래 통계 (수익률 분포용)
    @Query(value = "SELECT t.profit_loss_rate FROM trading_histories t " +
           "WHERE t.user_id = :userId AND t.trade_type = 1 AND t.profit_loss_rate IS NOT NULL " +
           "ORDER BY t.profit_loss_rate", nativeQuery = true)
    List<BigDecimal> findProfitLossRatesByUserId(@Param("userId") UUID userId);
}

