package com.bitreiver.app_server.domain.assetAnalysis.service;

import com.bitreiver.app_server.domain.asset.entity.Asset;
import com.bitreiver.app_server.domain.asset.repository.AssetRepository;
import com.bitreiver.app_server.domain.assetAnalysis.dto.*;
import com.bitreiver.app_server.domain.coin.dto.CoinResponse;
import com.bitreiver.app_server.domain.coin.entity.Coin;
import com.bitreiver.app_server.domain.coin.repository.CoinRepository;
import com.bitreiver.app_server.domain.diary.entity.Diary;
import com.bitreiver.app_server.domain.diary.repository.DiaryRepository;
import com.bitreiver.app_server.domain.trading.entity.TradingHistory;
import com.bitreiver.app_server.domain.trading.repository.TradingHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssetAnalysisServiceImpl implements AssetAnalysisService {
    
    private final TradingHistoryRepository tradingHistoryRepository;
    private final AssetRepository assetRepository;
    private final CoinRepository coinRepository;
    private final DiaryRepository diaryRepository;
    
    @Override
    public AssetAnalysisResponse getAssetAnalysis(UUID userId) {
        log.info("자산 분석 조회 시작: userId={}", userId);
        
        // 주요 지표 계산
        AssetAnalysisResponse.SummaryMetrics summaryMetrics = calculateSummaryMetrics(userId);
        
        // 각종 분석 데이터 계산
        AssetValueTrendResponse assetValueTrend = calculateAssetValueTrend(userId);
        ProfitDistributionResponse profitDistribution = calculateProfitDistribution(userId);
        CoinHoldingResponse coinHoldings = calculateCoinHoldings(userId);
        TradingFrequencyResponse tradingFrequency = calculateTradingFrequency(userId);
        TradingStyleResponse tradingStyle = analyzeTradingStyle(userId);
        MonthlyInvestmentResponse monthlyInvestment = calculateMonthlyInvestment(userId);
        TopCoinResponse topCoins = getTopProfitLossCoins(userId);
        PsychologyAnalysisResponse psychologyAnalysis = analyzePsychology(userId);
        RiskAnalysisResponse riskAnalysis = analyzeRisk(userId);
        TradingTendencySummaryResponse tradingTendencySummary = summarizeTradingTendency(
            tradingStyle, tradingFrequency, riskAnalysis
        );
        
        return AssetAnalysisResponse.builder()
            .summaryMetrics(summaryMetrics)
            .assetValueTrend(assetValueTrend)
            .profitDistribution(profitDistribution)
            .coinHoldings(coinHoldings)
            .tradingFrequency(tradingFrequency)
            .tradingStyle(tradingStyle)
            .monthlyInvestment(monthlyInvestment)
            .topCoins(topCoins)
            .psychologyAnalysis(psychologyAnalysis)
            .riskAnalysis(riskAnalysis)
            .tradingTendencySummary(tradingTendencySummary)
            .build();
    }
    
    private AssetAnalysisResponse.SummaryMetrics calculateSummaryMetrics(UUID userId) {
        List<TradingHistory> allTrades = tradingHistoryRepository.findByUserId(userId);
        List<TradingHistory> sellTrades = tradingHistoryRepository.findSellTradesByUserId(userId);
        List<Asset> assets = assetRepository.findByUserId(userId);
        
        // 총 거래 횟수
        long totalTradeCount = allTrades.size();
        
        // 보유 코인 수
        int holdingCoinCount = (int) assets.stream()
            .filter(a -> a.getCoinId() != null)
            .map(Asset::getCoinId)
            .distinct()
            .count();
        
        // 총 투자 원금 (매수액 합계)
        BigDecimal totalPrincipal = allTrades.stream()
            .filter(t -> t.getTradeType() == 0) // 매수
            .map(TradingHistory::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 총 매도액
        BigDecimal totalSellAmount = sellTrades.stream()
            .map(TradingHistory::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 총 수익 금액 (매도 시점의 수익)
        BigDecimal totalProfit = sellTrades.stream()
            .filter(t -> t.getProfitLossRate() != null && t.getAvgBuyPrice() != null)
            .map(t -> {
                BigDecimal profitRate = t.getProfitLossRate().divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
                BigDecimal buyAmount = t.getAvgBuyPrice().multiply(t.getQuantity());
                return buyAmount.multiply(profitRate);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 총 자산 가치 (현재 보유 자산 + 실현 수익)
        BigDecimal totalAssetValue = totalPrincipal.subtract(totalSellAmount).add(totalProfit);
        // TODO: 현재 보유 자산의 현재 가치를 추가로 계산 필요
        
        // 승률
        long profitCount = sellTrades.stream()
            .filter(t -> t.getProfitLossRate() != null && t.getProfitLossRate().compareTo(BigDecimal.ZERO) > 0)
            .count();
        BigDecimal winRate = sellTrades.isEmpty() ? BigDecimal.ZERO :
            BigDecimal.valueOf(profitCount)
                .divide(BigDecimal.valueOf(sellTrades.size()), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        
        return AssetAnalysisResponse.SummaryMetrics.builder()
            .totalAssetValue(totalAssetValue)
            .totalPrincipal(totalPrincipal)
            .totalProfit(totalProfit)
            .winRate(winRate)
            .totalTradeCount(totalTradeCount)
            .holdingCoinCount(holdingCoinCount)
            .build();
    }
    
    private BigDecimal calculateAverageHoldingPeriod(UUID userId) {
        List<TradingHistory> allTrades = tradingHistoryRepository.findByUserId(userId);
        
        // 코인별로 매수-매도 매칭하여 보유 기간 계산
        Map<Integer, List<TradingHistory>> tradesByCoin = allTrades.stream()
            .collect(Collectors.groupingBy(TradingHistory::getCoinId));
        
        List<Long> holdingPeriods = new ArrayList<>();
        
        for (Map.Entry<Integer, List<TradingHistory>> entry : tradesByCoin.entrySet()) {
            List<TradingHistory> coinTrades = entry.getValue().stream()
                .sorted(Comparator.comparing(TradingHistory::getTradeTime))
                .collect(Collectors.toList());
            
            BigDecimal remainingQuantity = BigDecimal.ZERO;
            LocalDateTime lastBuyTime = null;
            
            for (TradingHistory trade : coinTrades) {
                if (trade.getTradeType() == 0) { // 매수
                    remainingQuantity = remainingQuantity.add(trade.getQuantity());
                    if (lastBuyTime == null) {
                        lastBuyTime = trade.getTradeTime();
                    }
                } else if (trade.getTradeType() == 1) { // 매도
                    if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0 && lastBuyTime != null) {
                        long days = Duration.between(lastBuyTime, trade.getTradeTime()).toDays();
                        holdingPeriods.add(days);
                        
                        remainingQuantity = remainingQuantity.subtract(trade.getQuantity());
                        if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                            lastBuyTime = null;
                        }
                    }
                }
            }
        }
        
        if (holdingPeriods.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        double avgDays = holdingPeriods.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        return BigDecimal.valueOf(avgDays).setScale(2, RoundingMode.HALF_UP);
    }
    
    private AssetValueTrendResponse calculateAssetValueTrend(UUID userId) {
        // TODO: 시간별 자산 가치 추이 계산 (현재는 기본 구조만)
        return AssetValueTrendResponse.builder()
            .dataPoints(Collections.emptyList())
            .build();
    }
    
    private ProfitDistributionResponse calculateProfitDistribution(UUID userId) {
        List<BigDecimal> profitLossRates = tradingHistoryRepository.findProfitLossRatesByUserId(userId);
        
        if (profitLossRates.isEmpty()) {
            return ProfitDistributionResponse.builder()
                .totalSellCount(0L)
                .profitCount(0L)
                .lossCount(0L)
                .winRate(BigDecimal.ZERO)
                .averageProfitRate(BigDecimal.ZERO)
                .averageLossRate(BigDecimal.ZERO)
                .medianProfitRate(BigDecimal.ZERO)
                .maxProfitRate(BigDecimal.ZERO)
                .minProfitRate(BigDecimal.ZERO)
                .profitRanges(Collections.emptyList())
                .build();
        }
        
        long totalSellCount = profitLossRates.size();
        long profitCount = profitLossRates.stream()
            .filter(rate -> rate.compareTo(BigDecimal.ZERO) > 0)
            .count();
        long lossCount = totalSellCount - profitCount;
        
        BigDecimal winRate = BigDecimal.valueOf(profitCount)
            .divide(BigDecimal.valueOf(totalSellCount), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        
        List<BigDecimal> profitRates = profitLossRates.stream()
            .filter(rate -> rate.compareTo(BigDecimal.ZERO) > 0)
            .collect(Collectors.toList());
        List<BigDecimal> lossRates = profitLossRates.stream()
            .filter(rate -> rate.compareTo(BigDecimal.ZERO) <= 0)
            .collect(Collectors.toList());
        
        BigDecimal averageProfitRate = profitRates.isEmpty() ? BigDecimal.ZERO :
            profitRates.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(profitRates.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal averageLossRate = lossRates.isEmpty() ? BigDecimal.ZERO :
            lossRates.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(lossRates.size()), 2, RoundingMode.HALF_UP);
        
        // 중앙값 계산
        List<BigDecimal> sortedRates = new ArrayList<>(profitLossRates);
        sortedRates.sort(Comparator.naturalOrder());
        BigDecimal medianProfitRate = sortedRates.isEmpty() ? BigDecimal.ZERO :
            sortedRates.size() % 2 == 0 ?
                sortedRates.get(sortedRates.size() / 2 - 1).add(sortedRates.get(sortedRates.size() / 2))
                    .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP) :
                sortedRates.get(sortedRates.size() / 2);
        
        BigDecimal maxProfitRate = profitLossRates.stream()
            .max(Comparator.naturalOrder())
            .orElse(BigDecimal.ZERO);
        BigDecimal minProfitRate = profitLossRates.stream()
            .min(Comparator.naturalOrder())
            .orElse(BigDecimal.ZERO);
        
        // 수익률 구간별 통계 (간단한 버전)
        List<ProfitDistributionResponse.ProfitRange> profitRanges = calculateProfitRanges(profitLossRates);
        
        return ProfitDistributionResponse.builder()
            .totalSellCount(totalSellCount)
            .profitCount(profitCount)
            .lossCount(lossCount)
            .winRate(winRate)
            .averageProfitRate(averageProfitRate)
            .averageLossRate(averageLossRate)
            .medianProfitRate(medianProfitRate)
            .maxProfitRate(maxProfitRate)
            .minProfitRate(minProfitRate)
            .profitRanges(profitRanges)
            .build();
    }
    
    private List<ProfitDistributionResponse.ProfitRange> calculateProfitRanges(List<BigDecimal> rates) {
        // 간단한 구간 분할: -100% ~ -50%, -50% ~ -30%, -30% ~ -10%, -10% ~ 0%, 0% ~ 10%, 10% ~ 30%, 30% ~ 50%, 50% ~ 100%, 100%+
        List<ProfitDistributionResponse.ProfitRange> ranges = new ArrayList<>();
        BigDecimal[] rangeStarts = {
            BigDecimal.valueOf(-100), BigDecimal.valueOf(-50), BigDecimal.valueOf(-30),
            BigDecimal.valueOf(-10), BigDecimal.ZERO, BigDecimal.valueOf(10),
            BigDecimal.valueOf(30), BigDecimal.valueOf(50), BigDecimal.valueOf(100)
        };
        
        for (int i = 0; i < rangeStarts.length - 1; i++) {
            BigDecimal start = rangeStarts[i];
            BigDecimal end = rangeStarts[i + 1];
            long count = rates.stream()
                .filter(rate -> rate.compareTo(start) >= 0 && rate.compareTo(end) < 0)
                .count();
            
            ranges.add(ProfitDistributionResponse.ProfitRange.builder()
                .rangeStart(start)
                .rangeEnd(end)
                .count(count)
                .build());
        }
        
        // 100% 이상
        long countOver100 = rates.stream()
            .filter(rate -> rate.compareTo(BigDecimal.valueOf(100)) >= 0)
            .count();
        ranges.add(ProfitDistributionResponse.ProfitRange.builder()
            .rangeStart(BigDecimal.valueOf(100))
            .rangeEnd(BigDecimal.valueOf(1000)) // 상한선
            .count(countOver100)
            .build());
        
        return ranges;
    }
    
    private CoinHoldingResponse calculateCoinHoldings(UUID userId) {
        List<Asset> assets = assetRepository.findByUserId(userId);
        List<Integer> coinIds = assets.stream()
            .map(Asset::getCoinId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        
        Map<Integer, CoinResponse> coinMap = coinRepository.findAllById(coinIds).stream()
            .collect(Collectors.toMap(Coin::getId, CoinResponse::from));
        
        List<CoinHoldingResponse.CoinHolding> holdings = assets.stream()
            .filter(a -> a.getCoinId() != null)
            .map(asset -> {
                CoinResponse coin = coinMap.get(asset.getCoinId());
                BigDecimal holdingValue = asset.getAvgBuyPrice().multiply(asset.getQuantity());
                // TODO: 현재 가격 조회하여 실제 평가 금액 계산
                BigDecimal currentPrice = asset.getAvgBuyPrice(); // 임시
                
                return CoinHoldingResponse.CoinHolding.builder()
                    .coin(coin)
                    .quantity(asset.getQuantity())
                    .avgBuyPrice(asset.getAvgBuyPrice())
                    .currentPrice(currentPrice)
                    .holdingValue(holdingValue)
                    .percentage(BigDecimal.ZERO) // TODO: 총 자산 대비 비율 계산
                    .profit(BigDecimal.ZERO) // TODO: 수익 금액 계산
                    .profitRate(BigDecimal.ZERO) // TODO: 수익률 계산
                    .exchangeCode(asset.getExchangeCode())
                    .build();
            })
            .collect(Collectors.toList());
        
        BigDecimal totalValue = holdings.stream()
            .map(CoinHoldingResponse.CoinHolding::getHoldingValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 비율 계산
        holdings.forEach(holding -> {
            if (totalValue.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = holding.getHoldingValue()
                    .divide(totalValue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                holding.setPercentage(percentage);
            }
        });
        
        return CoinHoldingResponse.builder()
            .holdings(holdings)
            .totalValue(totalValue)
            .build();
    }
    
    private TradingFrequencyResponse calculateTradingFrequency(UUID userId) {
        // 시간대별 빈도
        List<Object[]> hourlyData = tradingHistoryRepository.findHourlyFrequencyByUserId(userId);
        Map<Integer, Long> hourlyFrequency = new HashMap<>();
        for (Object[] row : hourlyData) {
            Integer hour = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            hourlyFrequency.put(hour, count);
        }
        
        // 요일별 빈도
        List<Object[]> dayOfWeekData = tradingHistoryRepository.findDayOfWeekFrequencyByUserId(userId);
        Map<Integer, Long> dayOfWeekFrequency = new HashMap<>();
        for (Object[] row : dayOfWeekData) {
            Integer dayOfWeek = ((Number) row[0]).intValue();
            Long count = ((Number) row[1]).longValue();
            dayOfWeekFrequency.put(dayOfWeek, count);
        }
        
        // 월별 빈도
        List<Object[]> monthlyData = tradingHistoryRepository.findMonthlyFrequencyByUserId(userId);
        List<TradingFrequencyResponse.MonthlyFrequency> monthlyFrequency = monthlyData.stream()
            .map(row -> TradingFrequencyResponse.MonthlyFrequency.builder()
                .year(((Number) row[0]).intValue())
                .month(((Number) row[1]).intValue())
                .count(((Number) row[2]).longValue())
                .build())
            .collect(Collectors.toList());
        
        // 가장 활발한 시간대/요일
        Integer mostActiveHour = hourlyFrequency.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        Integer mostActiveDayOfWeek = dayOfWeekFrequency.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        return TradingFrequencyResponse.builder()
            .hourlyFrequency(hourlyFrequency)
            .dayOfWeekFrequency(dayOfWeekFrequency)
            .monthlyFrequency(monthlyFrequency)
            .mostActiveHour(mostActiveHour)
            .mostActiveDayOfWeek(mostActiveDayOfWeek)
            .build();
    }
    
    private TradingStyleResponse analyzeTradingStyle(UUID userId) {
        Object[] stats = tradingHistoryRepository.findTradeStatisticsByUserId(userId);
        if (stats == null || stats.length < 4) {
            return TradingStyleResponse.builder()
                .averageTradeAmount(BigDecimal.ZERO)
                .medianTradeAmount(BigDecimal.ZERO)
                .maxTradeAmount(BigDecimal.ZERO)
                .minTradeAmount(BigDecimal.ZERO)
                .averageHoldingPeriod(BigDecimal.ZERO)
                .medianHoldingPeriod(BigDecimal.ZERO)
                .tradingStyle("UNKNOWN")
                .holdingPeriodRanges(Collections.emptyList())
                .tradeAmountRanges(Collections.emptyList())
                .build();
        }
        
        BigDecimal avgAmount = stats.length > 1 && stats[1] != null ? (BigDecimal) stats[1] : BigDecimal.ZERO;
        BigDecimal minAmount = stats.length > 2 && stats[2] != null ? (BigDecimal) stats[2] : BigDecimal.ZERO;
        BigDecimal maxAmount = stats.length > 3 && stats[3] != null ? (BigDecimal) stats[3] : BigDecimal.ZERO;
        
        // 중앙값은 별도 계산 필요 (간단히 평균값 사용)
        BigDecimal medianAmount = avgAmount;
        
        BigDecimal averageHoldingPeriod = calculateAverageHoldingPeriod(userId);
        BigDecimal medianHoldingPeriod = averageHoldingPeriod; // 간단히 평균값 사용
        
        // 거래 스타일 분류
        String tradingStyle = classifyTradingStyle(averageHoldingPeriod);
        
        // 보유 기간 구간별 통계
        List<TradingStyleResponse.HoldingPeriodRange> holdingPeriodRanges = calculateHoldingPeriodRanges(userId);
        
        // 거래 금액 구간별 통계
        List<TradingStyleResponse.TradeAmountRange> tradeAmountRanges = calculateTradeAmountRanges(userId);
        
        return TradingStyleResponse.builder()
            .averageTradeAmount(avgAmount)
            .medianTradeAmount(medianAmount)
            .maxTradeAmount(maxAmount)
            .minTradeAmount(minAmount)
            .averageHoldingPeriod(averageHoldingPeriod)
            .medianHoldingPeriod(medianHoldingPeriod)
            .tradingStyle(tradingStyle)
            .holdingPeriodRanges(holdingPeriodRanges)
            .tradeAmountRanges(tradeAmountRanges)
            .build();
    }
    
    private String classifyTradingStyle(BigDecimal avgHoldingPeriod) {
        if (avgHoldingPeriod.compareTo(BigDecimal.valueOf(1)) <= 0) {
            return "SHORT_TERM"; // 1일 이하
        } else if (avgHoldingPeriod.compareTo(BigDecimal.valueOf(30)) <= 0) {
            return "MEDIUM_TERM"; // 1일~30일
        } else {
            return "LONG_TERM"; // 30일 이상
        }
    }
    
    private List<TradingStyleResponse.HoldingPeriodRange> calculateHoldingPeriodRanges(UUID userId) {
        // 간단한 구간 분할
        List<TradingStyleResponse.HoldingPeriodRange> ranges = new ArrayList<>();
        ranges.add(TradingStyleResponse.HoldingPeriodRange.builder()
            .rangeName("1일 이하")
            .count(0L) // TODO: 실제 계산
            .percentage(BigDecimal.ZERO)
            .build());
        ranges.add(TradingStyleResponse.HoldingPeriodRange.builder()
            .rangeName("1일~7일")
            .count(0L)
            .percentage(BigDecimal.ZERO)
            .build());
        ranges.add(TradingStyleResponse.HoldingPeriodRange.builder()
            .rangeName("7일~30일")
            .count(0L)
            .percentage(BigDecimal.ZERO)
            .build());
        ranges.add(TradingStyleResponse.HoldingPeriodRange.builder()
            .rangeName("30일 이상")
            .count(0L)
            .percentage(BigDecimal.ZERO)
            .build());
        return ranges;
    }
    
    private List<TradingStyleResponse.TradeAmountRange> calculateTradeAmountRanges(UUID userId) {
        // 간단한 구간 분할
        List<TradingStyleResponse.TradeAmountRange> ranges = new ArrayList<>();
        ranges.add(TradingStyleResponse.TradeAmountRange.builder()
            .rangeName("10만원 미만")
            .count(0L) // TODO: 실제 계산
            .percentage(BigDecimal.ZERO)
            .build());
        ranges.add(TradingStyleResponse.TradeAmountRange.builder()
            .rangeName("10만원~50만원")
            .count(0L)
            .percentage(BigDecimal.ZERO)
            .build());
        ranges.add(TradingStyleResponse.TradeAmountRange.builder()
            .rangeName("50만원~100만원")
            .count(0L)
            .percentage(BigDecimal.ZERO)
            .build());
        ranges.add(TradingStyleResponse.TradeAmountRange.builder()
            .rangeName("100만원 이상")
            .count(0L)
            .percentage(BigDecimal.ZERO)
            .build());
        return ranges;
    }
    
    private MonthlyInvestmentResponse calculateMonthlyInvestment(UUID userId) {
        List<Object[]> monthlyData = tradingHistoryRepository.findMonthlyTradeAmountsByUserId(userId);
        
        // 년-월별로 그룹화
        Map<String, MonthlyInvestmentResponse.MonthlyInvestment> monthlyMap = new HashMap<>();
        
        for (Object[] row : monthlyData) {
            Integer year = ((Number) row[0]).intValue();
            Integer month = ((Number) row[1]).intValue();
            Short tradeType = ((Number) row[2]).shortValue();
            BigDecimal totalAmount = (BigDecimal) row[3];
            
            String key = year + "-" + month;
            MonthlyInvestmentResponse.MonthlyInvestment monthly = monthlyMap.computeIfAbsent(key, k ->
                MonthlyInvestmentResponse.MonthlyInvestment.builder()
                    .year(year)
                    .month(month)
                    .totalBuyAmount(BigDecimal.ZERO)
                    .totalSellAmount(BigDecimal.ZERO)
                    .netInvestment(BigDecimal.ZERO)
                    .monthlyProfitRate(BigDecimal.ZERO)
                    .tradeCount(0L)
                    .build()
            );
            
            if (tradeType == 0) { // 매수
                monthly.setTotalBuyAmount(monthly.getTotalBuyAmount().add(totalAmount));
            } else if (tradeType == 1) { // 매도
                monthly.setTotalSellAmount(monthly.getTotalSellAmount().add(totalAmount));
            }
        }
        
        // 순 투자액 및 수익률 계산
        List<MonthlyInvestmentResponse.MonthlyInvestment> monthlyInvestments = new ArrayList<>(monthlyMap.values());
        monthlyInvestments.forEach(monthly -> {
            BigDecimal netInvestment = monthly.getTotalBuyAmount().subtract(monthly.getTotalSellAmount());
            monthly.setNetInvestment(netInvestment);
            
            if (monthly.getTotalBuyAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal profitRate = monthly.getTotalSellAmount()
                    .subtract(monthly.getTotalBuyAmount())
                    .divide(monthly.getTotalBuyAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
                monthly.setMonthlyProfitRate(profitRate);
            }
        });
        
        monthlyInvestments.sort(Comparator
            .comparing(MonthlyInvestmentResponse.MonthlyInvestment::getYear)
            .thenComparing(MonthlyInvestmentResponse.MonthlyInvestment::getMonth));
        
        return MonthlyInvestmentResponse.builder()
            .monthlyInvestments(monthlyInvestments)
            .build();
    }
    
    private TopCoinResponse getTopProfitLossCoins(UUID userId) {
        List<Object[]> coinProfits = tradingHistoryRepository.findCoinProfitByUserId(userId);
        
        List<Integer> coinIds = coinProfits.stream()
            .map(row -> ((Number) row[0]).intValue())
            .collect(Collectors.toList());
        
        Map<Integer, CoinResponse> coinMap = coinRepository.findAllById(coinIds).stream()
            .collect(Collectors.toMap(Coin::getId, CoinResponse::from));
        
        List<TopCoinResponse.TopCoin> topProfitCoins = coinProfits.stream()
            .filter(row -> {
                BigDecimal totalProfit = (BigDecimal) row[1];
                return totalProfit.compareTo(BigDecimal.ZERO) > 0;
            })
            .limit(10)
            .map(row -> {
                Integer coinId = ((Number) row[0]).intValue();
                BigDecimal totalProfit = (BigDecimal) row[1];
                BigDecimal avgProfitRate = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
                Long sellCount = ((Number) row[3]).longValue();
                
                // 거래소 코드는 별도 조회 필요 (간단히 첫 번째 거래에서 가져오기)
                List<TradingHistory> coinTrades = tradingHistoryRepository.findByUserId(userId).stream()
                    .filter(t -> t.getCoinId().equals(coinId))
                    .limit(1)
                    .collect(Collectors.toList());
                Short exchangeCode = coinTrades.isEmpty() ? null : coinTrades.get(0).getExchangeCode();
                
                return TopCoinResponse.TopCoin.builder()
                    .coin(coinMap.get(coinId))
                    .totalProfit(totalProfit)
                    .averageProfitRate(avgProfitRate)
                    .sellCount(sellCount)
                    .exchangeCode(exchangeCode)
                    .build();
            })
            .collect(Collectors.toList());
        
        List<TopCoinResponse.TopCoin> topLossCoins = coinProfits.stream()
            .filter(row -> {
                BigDecimal totalProfit = (BigDecimal) row[1];
                return totalProfit.compareTo(BigDecimal.ZERO) < 0;
            })
            .sorted(Comparator.comparing(row -> (BigDecimal) row[1]))
            .limit(10)
            .map(row -> {
                Integer coinId = ((Number) row[0]).intValue();
                BigDecimal totalProfit = (BigDecimal) row[1];
                BigDecimal avgProfitRate = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
                Long sellCount = ((Number) row[3]).longValue();
                
                List<TradingHistory> coinTrades = tradingHistoryRepository.findByUserId(userId).stream()
                    .filter(t -> t.getCoinId().equals(coinId))
                    .limit(1)
                    .collect(Collectors.toList());
                Short exchangeCode = coinTrades.isEmpty() ? null : coinTrades.get(0).getExchangeCode();
                
                return TopCoinResponse.TopCoin.builder()
                    .coin(coinMap.get(coinId))
                    .totalProfit(totalProfit)
                    .averageProfitRate(avgProfitRate)
                    .sellCount(sellCount)
                    .exchangeCode(exchangeCode)
                    .build();
            })
            .collect(Collectors.toList());
        
        return TopCoinResponse.builder()
            .topProfitCoins(topProfitCoins)
            .topLossCoins(topLossCoins)
            .build();
    }
    
    private PsychologyAnalysisResponse analyzePsychology(UUID userId) {
        List<Diary> diaries = diaryRepository.findByUserId(userId);
        
        // 심리 상태별 분포
        Map<String, Long> mindDistribution = new HashMap<>();
        Map<String, List<BigDecimal>> mindProfitRates = new HashMap<>();
        
        for (Diary diary : diaries) {
            if (diary.getTradingMind() != null) {
                String mindName = diary.getTradingMind().getName();
                mindDistribution.merge(mindName, 1L, (a, b) -> a + b);
                
                // 해당 거래의 수익률 조회
                TradingHistory history = tradingHistoryRepository.findById(diary.getTradingHistoryId()).orElse(null);
                if (history != null && history.getProfitLossRate() != null) {
                    mindProfitRates.computeIfAbsent(mindName, k -> new ArrayList<>())
                        .add(history.getProfitLossRate());
                }
            }
        }
        
        // 심리 상태별 평균 수익률
        Map<String, BigDecimal> mindAverageProfitRate = new HashMap<>();
        mindProfitRates.forEach((mind, rates) -> {
            BigDecimal avg = rates.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(rates.size()), 2, RoundingMode.HALF_UP);
            mindAverageProfitRate.put(mind, avg);
        });
        
        // 태그 빈도
        Map<String, Long> tagFrequency = new HashMap<>();
        diaries.forEach(diary -> {
            if (diary.getTags() != null) {
                diary.getTags().forEach(tag -> {
                    tagFrequency.merge(tag, 1L, (a, b) -> a + b);
                });
            }
        });
        
        List<PsychologyAnalysisResponse.TagFrequency> topTags = tagFrequency.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .map(entry -> PsychologyAnalysisResponse.TagFrequency.builder()
                .tag(entry.getKey())
                .count(entry.getValue())
                .build())
            .collect(Collectors.toList());
        
        return PsychologyAnalysisResponse.builder()
            .mindDistribution(mindDistribution)
            .mindAverageProfitRate(mindAverageProfitRate)
            .topTags(topTags)
            .build();
    }
    
    private RiskAnalysisResponse analyzeRisk(UUID userId) {
        List<Asset> assets = assetRepository.findByUserId(userId);
        
        // 코인별 자산 집중도 계산
        Map<Integer, BigDecimal> coinValues = new HashMap<>();
        BigDecimal totalValue = BigDecimal.ZERO;
        
        for (Asset asset : assets) {
            if (asset.getCoinId() != null) {
                BigDecimal value = asset.getAvgBuyPrice().multiply(asset.getQuantity());
                coinValues.merge(asset.getCoinId(), value, BigDecimal::add);
                totalValue = totalValue.add(value);
            }
        }
        
        // 람다에서 사용하기 위해 final 변수로 복사
        final BigDecimal finalTotalValue = totalValue;
        
        // 상위 5개 코인 집중도
        List<RiskAnalysisResponse.CoinConcentration> topCoinConcentrations = coinValues.entrySet().stream()
            .sorted(Map.Entry.<Integer, BigDecimal>comparingByValue().reversed())
            .limit(5)
            .map(entry -> {
                Integer coinId = entry.getKey();
                BigDecimal value = entry.getValue();
                BigDecimal percentage = finalTotalValue.compareTo(BigDecimal.ZERO) > 0 ?
                    value.divide(finalTotalValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)) :
                    BigDecimal.ZERO;
                
                Coin coin = coinRepository.findById(coinId).orElse(null);
                String symbol = coin != null ? coin.getSymbol() : "UNKNOWN";
                
                return RiskAnalysisResponse.CoinConcentration.builder()
                    .symbol(symbol)
                    .percentage(percentage)
                    .build();
            })
            .collect(Collectors.toList());
        
        BigDecimal top5Concentration = topCoinConcentrations.stream()
            .map(RiskAnalysisResponse.CoinConcentration::getPercentage)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 다양성 지수 (1 - 상위 5개 코인 집중도 합의 제곱합)
        BigDecimal diversityIndex = BigDecimal.ONE.subtract(
            topCoinConcentrations.stream()
                .map(c -> c.getPercentage().divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP))
                .map(p -> p.multiply(p))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
        );
        
        return RiskAnalysisResponse.builder()
            .top5CoinConcentration(top5Concentration)
            .topCoinConcentrations(topCoinConcentrations)
            .diversityIndex(diversityIndex)
            .build();
    }
    
    private TradingTendencySummaryResponse summarizeTradingTendency(
        TradingStyleResponse tradingStyle,
        TradingFrequencyResponse tradingFrequency,
        RiskAnalysisResponse riskAnalysis
    ) {
        String tradingStyleStr = tradingStyle.getTradingStyle();
        
        // 리스크 성향 분류
        String riskTendency = "MODERATE";
        if (riskAnalysis.getTop5CoinConcentration().compareTo(BigDecimal.valueOf(80)) > 0) {
            riskTendency = "AGGRESSIVE";
        } else if (riskAnalysis.getTop5CoinConcentration().compareTo(BigDecimal.valueOf(50)) < 0) {
            riskTendency = "CONSERVATIVE";
        }
        
        Integer preferredTradingHour = tradingFrequency.getMostActiveHour();
        Integer preferredTradingDay = tradingFrequency.getMostActiveDayOfWeek();
        
        // 선호 코인 카테고리 (상위 3개 코인)
        List<String> preferredCoinCategories = riskAnalysis.getTopCoinConcentrations().stream()
            .limit(3)
            .map(RiskAnalysisResponse.CoinConcentration::getSymbol)
            .collect(Collectors.toList());
        
        return TradingTendencySummaryResponse.builder()
            .tradingStyle(tradingStyleStr)
            .riskTendency(riskTendency)
            .preferredTradingHour(preferredTradingHour)
            .preferredTradingDay(preferredTradingDay)
            .preferredCoinCategories(preferredCoinCategories)
            .build();
    }
}
