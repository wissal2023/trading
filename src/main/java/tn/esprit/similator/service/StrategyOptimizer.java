package tn.esprit.similator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tn.esprit.similator.entity.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StrategyOptimizer {

  private final List<StockData> stockDataList;
  private final String strategyType;
  private final OptimizationPreferences preferences;
  private final MarketConditions marketConditions;
  private final RiskAnalyzer riskAnalyzer;
  private final double baselineVolatilityThreshold;
  private final double baselineDrawdownThreshold;
  private static final int VOLUME_MA_PERIOD = 20;
  private static final double VOLUME_SPIKE_THRESHOLD = 2.0;
  private static final int DEFAULT_ROLLING_WINDOW = 63;
  private static final int MIN_DATA_POINTS = 20;
  private double riskFreeRate; // Made accessible
  private static final Logger log = LoggerFactory.getLogger(StrategyOptimizer.class);
  public StrategyOptimizer(List<StockData> stockDataList, String strategyType,
                           OptimizationPreferences preferences) {
    this.stockDataList = stockDataList;
    this.strategyType = strategyType;
    this.preferences = preferences;
    this.marketConditions = analyzeMarketConditions();
    this.riskAnalyzer = new RiskAnalyzer(stockDataList, preferences.getRiskFreeRate());
    this.baselineVolatilityThreshold = preferences.getVolatilityThreshold();
    this.baselineDrawdownThreshold = preferences.getMaxDrawdownThreshold();
  }

  private double getDynamicVolatilityThreshold() {
    double marketVol = marketConditions.getVolatility();
    double trend = Math.abs(marketConditions.getTrend());

    // Increase threshold in strong trending markets
    if (trend > 0.2) {
      return baselineVolatilityThreshold * 1.3;
    }
    // Decrease threshold in choppy markets
    else if (marketVol > baselineVolatilityThreshold) {
      return baselineVolatilityThreshold * 0.8;
    }
    return baselineVolatilityThreshold;
  }

  private double getDynamicDrawdownThreshold() {
    double marketVol = marketConditions.getVolatility();
    double trend = Math.abs(marketConditions.getTrend());

    // Allow larger drawdowns in trending markets
    if (trend > 0.2) {
      return baselineDrawdownThreshold * 1.2;
    }
    // Tighten drawdown limits in volatile markets
    else if (marketVol > baselineVolatilityThreshold) {
      return baselineDrawdownThreshold * 0.7;
    }
    return baselineDrawdownThreshold;
  }

  public OptimizationResponse optimize() {
    VolumeMetrics volumeMetrics = analyzeVolume();
    OptimizationOutcome outcome = null;
    switch (strategyType) {
      case "SMA":
        outcome = optimizeSMAStrategy();
        break;
      case "RSI":
        outcome = optimizeRSIStrategy();
        break;
      default:
        throw new IllegalArgumentException("Unsupported strategy type: " + strategyType);
    }

    if (outcome == null || outcome.getParameters() == null) {
      throw new RuntimeException("Strategy optimization failed to produce valid parameters");
    }

    RiskMetrics riskMetrics = riskAnalyzer.calculateRiskMetrics();
    Map<String, Double> metrics = calculatePerformanceMetrics(outcome.getResult(), riskMetrics);

    RollingMetrics rollingMetrics = calculateRollingMetrics(outcome.getResult());
    StreakAnalysis streakAnalysis = analyzeStreaks(rollingMetrics.getReturns());
    // Add volume metrics to the performance metrics
    metrics.put("volumeVolatility", volumeMetrics.getVolumeVolatility());
    metrics.put("volumeTrend", volumeMetrics.getVolumeTrend());
    metrics.put("volumeMomentum", volumeMetrics.getVolumeMomentum());
    metrics.put("priceVolumeCorrelation", volumeMetrics.getPriceVolumeCorrelation());

    Map<String, Double> marketConditionsMap = new HashMap<>();
    marketConditionsMap.put("volatility", marketConditions.getVolatility());
    marketConditionsMap.put("trend", marketConditions.getTrend());
    marketConditionsMap.put("volume", marketConditions.getVolume());
    marketConditionsMap.put("averageVolume", volumeMetrics.getAverageVolume());
    FeedbackResponse feedback = generateFeedback(outcome, metrics, riskMetrics, volumeMetrics);
    return new OptimizationResponse(
      outcome.getParameters().toMap(),
      metrics,
      marketConditionsMap,
      outcome.getResult(),
      feedback,
      rollingMetrics,
      streakAnalysis
    );
  }
  /**
   * Calculates the annualized market return using logarithmic returns
   * and proper date handling
   */
  private double calculateMarketReturn() {
    if (stockDataList == null || stockDataList.size() < 2) {
      log.warn("Insufficient data to calculate market return");
      return 0.0;
    }

    List<StockData> sortedData = new ArrayList<>(stockDataList);
    Collections.sort(sortedData);

    double firstPrice = sortedData.get(0).getClosePrice();
    double lastPrice = sortedData.get(sortedData.size() - 1).getClosePrice();

    // Calculate number of years between first and last date
    long daysBetween = ChronoUnit.DAYS.between(
      sortedData.get(0).getDate(),
      sortedData.get(sortedData.size() - 1).getDate()
    );
    double years = daysBetween / 365.25;  // Account for leap years

    // Calculate annualized return using log returns
    double totalReturn = Math.log(lastPrice / firstPrice);
    return Math.exp(totalReturn / years) - 1;
  }


  /**
   * Validates and adjusts the risk-free rate based on market conditions
   * @param inputRate The nominal risk-free rate
   * @return Adjusted daily risk-free rate
   */
  private double validateRiskFreeRate(double inputRate) {
    if (inputRate < 0) {
      log.warn("Negative risk-free rate provided: {}. Using 0.0", inputRate);
      return 0.0;
    }

    if (inputRate > 0.15) {  // 15% annual rate as upper threshold
      log.warn("Unusually high risk-free rate provided: {}. Consider reviewing.", inputRate);
    }

    // Convert annual rate to daily rate
    return Math.pow(1 + inputRate, 1.0/252) - 1;
  }


  /**
   * Calculates beta using the stock's returns against its moving average
   * This is a simplified beta calculation when no market benchmark is available
   * @return Beta coefficient
   */
  private double calculateBeta() {
    if (stockDataList == null || stockDataList.size() < MIN_DATA_POINTS) {
      log.warn("Insufficient data points for beta calculation");
      return 1.0;
    }

    try {
      List<Double> returns = calculateDailyReturns(stockDataList);
      if (returns.isEmpty()) {
        return 1.0;
      }

      // Calculate moving average of returns as a proxy for "market"
      int maWindow = Math.min(20, returns.size() - 1); // 20-day moving average
      List<Double> maReturns = new ArrayList<>();

      for (int i = maWindow; i < returns.size(); i++) {
        double sum = 0;
        for (int j = 0; j < maWindow; j++) {
          sum += returns.get(i - j);
        }
        maReturns.add(sum / maWindow);
      }

      // Trim the original returns list to match MA length
      returns = returns.subList(maWindow, returns.size());

      // Calculate means
      double returnsMean = returns.stream()
        .mapToDouble(Double::doubleValue)
        .average()
        .orElse(0.0);

      double maMean = maReturns.stream()
        .mapToDouble(Double::doubleValue)
        .average()
        .orElse(0.0);

      // Calculate covariance and variance
      double covariance = 0.0;
      double maVariance = 0.0;

      for (int i = 0; i < returns.size(); i++) {
        double returnsDev = returns.get(i) - returnsMean;
        double maDev = maReturns.get(i) - maMean;

        covariance += returnsDev * maDev;
        maVariance += maDev * maDev;
      }

      covariance /= (returns.size() - 1);
      maVariance /= (returns.size() - 1);

      if (maVariance == 0) {
        log.warn("Moving average variance is zero, defaulting to beta = 1.0");
        return 1.0;
      }

      double beta = covariance / maVariance;

      // Sanity check on beta value
      if (Math.abs(beta) > 4.0) {
        log.warn("Unusually high beta calculated: {}. Consider reviewing data.", beta);
      }

      return beta;

    } catch (Exception e) {
      log.error("Error calculating beta: " + e.getMessage(), e);
      return 1.0;
    }
  }


  /**
   * Helper method to calculate daily returns from price data
   */
  private List<Double> calculateDailyReturns(List<StockData> data) {
    List<Double> returns = new ArrayList<>();
    List<StockData> sortedData = new ArrayList<>(data);
    Collections.sort(sortedData);

    for (int i = 1; i < sortedData.size(); i++) {
      double previousClose = sortedData.get(i-1).getClosePrice();
      double currentClose = sortedData.get(i).getClosePrice();

      // Use log returns for better statistical properties
      double dailyReturn = Math.log(currentClose / previousClose);
      returns.add(dailyReturn);
    }

    return returns;
  }


  private MarketConditions analyzeMarketConditions() {
    double volatility = calculateHistoricalVolatility();
    double trend = calculateMarketTrend();
    double volume = calculateAverageVolume();

    return new MarketConditions(volatility, trend, volume);
  }
  private double calculateHistoricalVolatility() {
    List<Double> returns = new ArrayList<>();
    List<StockData> sortedData = new ArrayList<>(stockDataList);
    Collections.sort(sortedData);

    for (int i = 1; i < sortedData.size(); i++) {
      double previousClose = sortedData.get(i-1).getClosePrice();
      double currentClose = sortedData.get(i).getClosePrice();
      double dailyReturn = Math.log(currentClose / previousClose);
      returns.add(dailyReturn);
    }
    if (returns.isEmpty()) return 0.0;
    double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    double variance = returns.stream()
      .mapToDouble(r -> Math.pow(r - mean, 2))
      .sum() / (returns.size() - 1);
    return Math.sqrt(variance * 252);
  }
  private double calculateMarketTrend() {
    if (stockDataList.size() < 2) return 0.0;
    List<StockData> sortedData = new ArrayList<>(stockDataList);
    Collections.sort(sortedData);
    double sumX = 0;
    double sumY = 0;
    double sumXY = 0;
    double sumX2 = 0;
    for (int i = 0; i < sortedData.size(); i++) {
      double price = sortedData.get(i).getClosePrice();
      sumX += i;
      sumY += price;
      sumXY += i * price;
      sumX2 += i * i;
    }
    double n = sortedData.size();
    double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    double averagePrice = sumY / n;
    double normalizedSlope = slope / averagePrice;
    double firstPrice = sortedData.get(0).getClosePrice();
    double lastPrice = sortedData.get(sortedData.size() - 1).getClosePrice();
    double totalDays = sortedData.size();
    double totalReturn = Math.pow(lastPrice / firstPrice, 252.0 / totalDays) - 1.0;
    return 0.7 * totalReturn + 0.3 * normalizedSlope;
  }
  private double calculateAverageVolume() {
    return stockDataList.stream()
      .mapToDouble(StockData::getVolume)
      .average()
      .orElse(0.0);
  }
  private OptimizationOutcome optimizeSMAStrategy() {
    ParameterSet bestParams = null;
    double bestScore = Double.NEGATIVE_INFINITY;
    OptimizationResult bestResult = null;
    RiskMetrics bestRiskMetrics = null;

    List<ParameterSet> parameterSets = generateSMAParameters();

    for (ParameterSet params : parameterSets) {
      Strategy strategy = new SMACrossoverStrategy(
        params.getIntValue("shortPeriod"),
        params.getIntValue("longPeriod")
      );

      BacktestResult backtestResult = strategy.execute(stockDataList);
      RiskMetrics riskMetrics = riskAnalyzer.calculateRiskMetrics();

      OptimizationResult result = convertBacktestToOptimizationResult(backtestResult, params);
      double score = evaluateStrategy(result, riskMetrics);

      if (score > bestScore) {
        bestScore = score;
        bestParams = params;
        bestResult = result;
        bestRiskMetrics = riskMetrics;
      }
    }

    return new OptimizationOutcome(bestParams, bestResult, bestScore, bestRiskMetrics);
  }
  private boolean meetsRiskConstraints(RiskMetrics metrics) {
    return metrics.getVolatility() <= preferences.getVolatilityThreshold() &&
      metrics.getMaxDrawdown() <= preferences.getMaxDrawdownThreshold() &&
      metrics.getSharpeRatio() >= preferences.getMinSharpeRatio();
  }
  private OptimizationOutcome optimizeRSIStrategy() {
    ParameterSet bestParams = null;
    double bestScore = Double.NEGATIVE_INFINITY;
    OptimizationResult bestResult = null;
    RiskMetrics bestRiskMetrics = null;
    List<ParameterSet> parameterSets = generateRSIParameters();
    for (ParameterSet params : parameterSets) {
      Strategy strategy = new RSIStrategy(
        params.getIntValue("period"),
        params.getDoubleValue("oversoldThreshold"),
        params.getDoubleValue("overboughtThreshold")
      );
      BacktestResult backtestResult = strategy.execute(stockDataList);
      RiskMetrics riskMetrics = riskAnalyzer.calculateRiskMetrics();
      OptimizationResult result = convertBacktestToOptimizationResult(backtestResult, params);
      double score = evaluateStrategy(result, riskMetrics);
      if (score > bestScore) {
        bestScore = score;
        bestParams = params;
        bestResult = result;
        bestRiskMetrics = riskMetrics;
      }
    }
    return new OptimizationOutcome(bestParams, bestResult, bestScore, bestRiskMetrics);
  }
  private List<ParameterSet> generateSMAParameters() {
    List<ParameterSet> parameterSets = new ArrayList<>();
    if (preferences.isAdaptToMarketConditions()) {
      if (marketConditions.getVolatility() > preferences.getVolatilityThreshold()) {
        addHighVolatilityParameters(parameterSets);
      } else {
        addLowVolatilityParameters(parameterSets);
      }
    } else {
      addDefaultSMAParameters(parameterSets);
    }
    return parameterSets;
  }
  private List<ParameterSet> generateRSIParameters() {
    List<ParameterSet> parameterSets = new ArrayList<>();
    if (preferences.isAdaptToMarketConditions()) {
      double volatility = marketConditions.getVolatility();
      double trend = marketConditions.getTrend();
      int[] periods = determineRSIPeriods(volatility);
      double[] oversoldLevels = determineOversoldLevels(trend);
      double[] overboughtLevels = determineOverboughtLevels(trend);

      for (int period : periods) {
        for (double oversold : oversoldLevels) {
          for (double overbought : overboughtLevels) {
            if (overbought > oversold + 20) {
              parameterSets.add(new ParameterSet()
                .add("period", period)
                .add("oversoldThreshold", oversold)
                .add("overboughtThreshold", overbought));
            }
          }
        }
      }
    } else {
      addDefaultRSIParameters(parameterSets);
    }
    return parameterSets;
  }
  private void addHighVolatilityParameters(List<ParameterSet> parameterSets) {
    int[] shortPeriods = {3, 5, 8, 10};
    int[] longPeriods = {15, 20, 25, 30};
    for (int shortPeriod : shortPeriods) {
      for (int longPeriod : longPeriods) {
        if (shortPeriod < longPeriod) {
          parameterSets.add(new ParameterSet()
            .add("shortPeriod", shortPeriod)
            .add("longPeriod", longPeriod));
        }
      }
    }
  }
  private void addLowVolatilityParameters(List<ParameterSet> parameterSets) {
    int[] shortPeriods = {10, 15, 20, 25};
    int[] longPeriods = {30, 50, 75, 100};
    for (int shortPeriod : shortPeriods) {
      for (int longPeriod : longPeriods) {
        if (shortPeriod < longPeriod) {
          parameterSets.add(new ParameterSet()
            .add("shortPeriod", shortPeriod)
            .add("longPeriod", longPeriod));
        }
      }
    }
  }
  private void addDefaultSMAParameters(List<ParameterSet> parameterSets) {
    int[] shortPeriods = {5, 10, 20};
    int[] longPeriods = {20, 50, 200};
    for (int shortPeriod : shortPeriods) {
      for (int longPeriod : longPeriods) {
        if (shortPeriod < longPeriod) {
          parameterSets.add(new ParameterSet()
            .add("shortPeriod", shortPeriod)
            .add("longPeriod", longPeriod));
        }
      }
    }
  }
  private void addDefaultRSIParameters(List<ParameterSet> parameterSets) {
    int[] periods = {14, 21};
    double[] oversoldLevels = {30};
    double[] overboughtLevels = {70};
    for (int period : periods) {
      for (double oversold : oversoldLevels) {
        for (double overbought : overboughtLevels) {
          parameterSets.add(new ParameterSet()
            .add("period", period)
            .add("oversoldThreshold", oversold)
            .add("overboughtThreshold", overbought));
        }
      }
    }
  }
  private int[] determineRSIPeriods(double volatility) {
    if (volatility > preferences.getVolatilityThreshold()) {
      return new int[]{7, 9, 11, 14}; // More responsive for high volatility
    } else {
      return new int[]{14, 21, 28, 30}; // Less responsive for low volatility
    }
  }
  private double[] determineOversoldLevels(double trend) {
    if (trend > 0.1) {
      return new double[]{25, 30, 35};
    } else if (trend < -0.1) {
      return new double[]{20, 25, 30};
    } else {
      return new double[]{20, 25, 30, 35};
    }
  }
  private double[] determineOverboughtLevels(double trend) {
    if (trend > 0.1) {
      return new double[]{70, 75, 80};
    } else if (trend < -0.1) {
      return new double[]{65, 70, 75};
    } else {
      return new double[]{65, 70, 75, 80};
    }
  }
  private double evaluateStrategy(OptimizationResult result, RiskMetrics riskMetrics) {
    Map<String, Double> metrics = calculatePerformanceMetrics(result, riskMetrics);
    return calculateWeightedScore(metrics);
  }
  private Map<String, Double> calculatePerformanceMetrics(OptimizationResult result, RiskMetrics riskMetrics) {
    Map<String, Double> metrics = new HashMap<>();
    try {
      BacktestResult backtestResult = executeBacktest(result.getParameters());

      // Calculate beta using our modified method
      double beta = calculateBeta();

      metrics.put("return", backtestResult.getTotalReturn());
      metrics.put("sharpe", riskMetrics.getSharpeRatio());
      metrics.put("maxDrawdown", -riskMetrics.getMaxDrawdown());
      metrics.put("volatility", -riskMetrics.getVolatility());
      metrics.put("sortino", riskMetrics.getSortinoRatio());
      metrics.put("beta", beta);
      metrics.put("alpha", calculateAlpha(backtestResult.getTotalReturn(), beta));
      metrics.put("cvar", -riskMetrics.getCvar());
      metrics.put("calmar", riskMetrics.getCalmarRatio());
    } catch (Exception e) {
      log.error("Error calculating performance metrics: {}", e.getMessage());
      metrics.put("return", 0.0);
      metrics.put("sharpe", 0.0);
      metrics.put("maxDrawdown", 0.0);
      metrics.put("volatility", 0.0);
      metrics.put("sortino", 0.0);
      metrics.put("beta", 1.0);
      metrics.put("alpha", 0.0);
      metrics.put("cvar", 0.0);
      metrics.put("calmar", 0.0);
    }
    return metrics;
  }
  /**
   * Calculates Jensen's Alpha using the security's returns against its moving average trend
   * Using the same moving average approach as our beta calculation for consistency
   * @param totalReturn The total return from the backtest
   * @param beta The calculated beta coefficient
   * @return Alpha value representing risk-adjusted excess return
   */
  private double calculateAlpha(double totalReturn, double beta) {
    if (stockDataList == null || stockDataList.size() < MIN_DATA_POINTS) {
      log.warn("Insufficient data points for alpha calculation");
      return 0.0;
    }

    try {
      // Calculate annualized return for the security
      List<Double> returns = calculateDailyReturns(stockDataList);
      if (returns.isEmpty()) {
        return 0.0;
      }

      // Calculate moving average returns (same as in beta calculation)
      int maWindow = Math.min(20, returns.size() - 1);
      List<Double> maReturns = new ArrayList<>();

      for (int i = maWindow; i < returns.size(); i++) {
        double sum = 0;
        for (int j = 0; j < maWindow; j++) {
          sum += returns.get(i - j);
        }
        maReturns.add(sum / maWindow);
      }

      // Calculate average daily returns
      double avgDailyReturn = returns.stream()
        .mapToDouble(Double::doubleValue)
        .average()
        .orElse(0.0);

      double avgMaReturn = maReturns.stream()
        .mapToDouble(Double::doubleValue)
        .average()
        .orElse(0.0);

      // Annualize returns (252 trading days)
      double annualizedReturn = Math.exp(avgDailyReturn * 252) - 1;
      double annualizedMaReturn = Math.exp(avgMaReturn * 252) - 1;

      // Convert daily risk-free rate to annual
      double annualRiskFreeRate = Math.exp(riskFreeRate * 252) - 1;

      // Calculate alpha using CAPM formula
      double excessReturn = annualizedReturn - annualRiskFreeRate;
      double maExcessReturn = annualizedMaReturn - annualRiskFreeRate;

      double alpha = excessReturn - (beta * maExcessReturn);

      // Sanity check
      if (Math.abs(alpha) > 0.5) { // 50% alpha would be extremely unusual
        log.warn("Unusually large alpha calculated: {}. Consider reviewing data.", alpha);
      }

      return alpha;

    } catch (Exception e) {
      log.error("Error calculating alpha: {} | Stack trace: {}",
        e.getMessage(),
        Arrays.toString(e.getStackTrace()));
      return 0.0;
    }
  }
  private double calculateWeightedScore(Map<String, Double> metrics) {
    double score = 0;
    for (Map.Entry<String, Double> weight : preferences.getMetricWeights().entrySet()) {
      Double metricValue = metrics.get(weight.getKey());
      if (metricValue != null) {
        score += weight.getValue() * metricValue;
      }
    }
    return score;
  }
  private OptimizationResult convertBacktestToOptimizationResult(BacktestResult backtestResult, ParameterSet params) {
    Map<String, Object> parameters = params.toMap();
    double performance = backtestResult.getTotalReturn();
    return new OptimizationResult(parameters, performance);
  }
  private BacktestResult executeBacktest(Map<String, Object> parameters) {
    Strategy strategy = createStrategy(parameters);
    return strategy.execute(stockDataList);
  }
  private Strategy createStrategy(Map<String, Object> parameters) {
    switch (strategyType) {
      case "SMA":
        return new SMACrossoverStrategy(
          (Integer) parameters.get("shortPeriod"),
          (Integer) parameters.get("longPeriod")
        );
      case "RSI":
        return new RSIStrategy(
          (Integer) parameters.get("period"),
          (Double) parameters.get("oversoldThreshold"),
          (Double) parameters.get("overboughtThreshold")
        );
      default:
        throw new IllegalArgumentException("Unsupported strategy type: " + strategyType);
    }
  }
  private FeedbackResponse generateFeedback(OptimizationOutcome outcome, Map<String, Double> metrics, RiskMetrics riskMetrics, VolumeMetrics volumeMetrics) {
    List<String> recommendations = new ArrayList<>();
    Map<String, String> metricAnalysis = new HashMap<>();
    StringBuilder overallAssessment = new StringBuilder();

    analyzeVolumeMetrics(volumeMetrics, recommendations, metricAnalysis);
    // Dynamic risk analysis
    double dynamicVolThreshold = getDynamicVolatilityThreshold();
    double dynamicDrawdownThreshold = getDynamicDrawdownThreshold();

    analyzeRiskMetrics(riskMetrics, recommendations, metricAnalysis, dynamicVolThreshold, dynamicDrawdownThreshold);
    generateOverallAssessment(outcome, riskMetrics, overallAssessment, metrics);
    addRiskAdjustedRecommendations(metrics, riskMetrics, recommendations);

    RiskAssessment riskAssessment = assessRisk(metrics, marketConditions, riskMetrics);

    return new FeedbackResponse(
      overallAssessment.toString(),
      recommendations,
      metricAnalysis,
      riskAssessment
    );
  }
  private void analyzeVolumeMetrics(VolumeMetrics volumeMetrics, List<String> recommendations,
                                    Map<String, String> metricAnalysis) {
    // Add volume metrics analysis
    metricAnalysis.put("volumeVolatility",
      formatMetric("Volume volatility", volumeMetrics.getVolumeVolatility() * 100, "%"));
    metricAnalysis.put("volumeTrend",
      formatMetric("Volume trend", volumeMetrics.getVolumeTrend() * 100, "%"));
    metricAnalysis.put("volumeMomentum",
      formatMetric("Volume momentum", volumeMetrics.getVolumeMomentum() * 100, "%"));
    metricAnalysis.put("priceVolumeCorrelation",
      formatMetric("Price-volume correlation", volumeMetrics.getPriceVolumeCorrelation(), ""));

    // Volume spike analysis
    if (!volumeMetrics.getVolumeSpikeDates().isEmpty()) {
      recommendations.add(String.format(
        "Detected %d significant volume spikes - Consider adjusting position sizing around these events",
        volumeMetrics.getVolumeSpikeDates().size()));
    }

    // Volume trend analysis
    if (volumeMetrics.getVolumeTrend() > 0.1) {
      recommendations.add("Rising volume trend detected - Consider increasing position sizes");
    } else if (volumeMetrics.getVolumeTrend() < -0.1) {
      recommendations.add("Declining volume trend detected - Consider reducing position sizes");
    }

    // Price-volume correlation analysis
    double correlation = volumeMetrics.getPriceVolumeCorrelation();
    if (Math.abs(correlation) > 0.7) {
      recommendations.add(String.format(
        "Strong price-volume correlation (%.2f) detected - Consider volume-weighted position sizing",
        correlation));
    }

    // Volume distribution analysis
    Map<String, Double> distribution = volumeMetrics.getVolumeDistribution();
    double volumeRange = distribution.get("q3") - distribution.get("q1");
    if (volumeRange / distribution.get("median") > 1.5) {
      recommendations.add("High volume dispersion detected - Consider using volume-based filters");
    }
  }
  private void analyzeRiskMetrics(RiskMetrics riskMetrics, List<String> recommendations,
                                  Map<String, String> metricAnalysis, double dynamicVolThreshold,
                                  double dynamicDrawdownThreshold) {
    // Existing metrics
    metricAnalysis.put("volatility", formatMetric("Annualized volatility", riskMetrics.getVolatility() * 100, "%"));
    metricAnalysis.put("valueAtRisk", formatMetric("Daily VaR (95%)", riskMetrics.getValueAtRisk() * 100, "%"));
    metricAnalysis.put("sharpeRatio", formatMetric("Sharpe ratio", riskMetrics.getSharpeRatio(), ""));
    metricAnalysis.put("sortinoRatio", formatMetric("Sortino ratio", riskMetrics.getSortinoRatio(), ""));
    metricAnalysis.put("beta", formatMetric("Beta", riskMetrics.getBeta(), ""));

    // New metrics
    metricAnalysis.put("cvar", formatMetric("Conditional VaR (95%)", riskMetrics.getCvar() * 100, "%"));
    metricAnalysis.put("calmarRatio", formatMetric("Calmar ratio", riskMetrics.getCalmarRatio(), ""));

    // Add CVaR-specific recommendations
    if (Math.abs(riskMetrics.getCvar()) > Math.abs(riskMetrics.getValueAtRisk()) * 1.5) {
      recommendations.add(String.format(
        "High tail risk detected: CVaR (%.2f%%) significantly exceeds VaR (%.2f%%) - " +
          "Consider implementing tail risk hedging strategies",
        riskMetrics.getCvar() * 100, riskMetrics.getValueAtRisk() * 100));
    }

    // Add Calmar ratio recommendations
    if (riskMetrics.getCalmarRatio() < 0.5) {
      recommendations.add(
        "Low Calmar ratio indicates poor return relative to maximum drawdown - " +
          "Consider adjusting position sizing or implementing stronger drawdown controls");
    } else if (riskMetrics.getCalmarRatio() > 2.0) {
      recommendations.add(
        "Strong Calmar ratio suggests effective drawdown management - " +
          "Current risk parameters appear well-calibrated");
    }

    // Existing risk checks...
    if (riskMetrics.getVolatility() > dynamicVolThreshold) {
      String reason = String.format(
        "Current volatility (%.2f%%) exceeds dynamic threshold (%.2f%%) adjusted for market conditions",
        riskMetrics.getVolatility() * 100, dynamicVolThreshold * 100);
      recommendations.add("Consider increasing smoothing parameters: " + reason);
    }

    // Dynamic threshold-based recommendations
    if (riskMetrics.getVolatility() > dynamicVolThreshold) {
      String reason = String.format(
        "Current volatility (%.2f%%) exceeds dynamic threshold (%.2f%%) adjusted for market conditions",
        riskMetrics.getVolatility() * 100, dynamicVolThreshold * 100);
      recommendations.add("Consider increasing smoothing parameters: " + reason);
    }

    if (riskMetrics.getMaxDrawdown() > dynamicDrawdownThreshold) {
      String reason = String.format(
        "Maximum drawdown (%.2f%%) exceeds dynamic threshold (%.2f%%) calibrated to current market volatility",
        riskMetrics.getMaxDrawdown() * 100, dynamicDrawdownThreshold * 100);
      recommendations.add("Implement tighter stop-loss controls: " + reason);
    }

    // Sortino-based analysis
    if (riskMetrics.getSortinoRatio() < 1.0 && riskMetrics.getSharpeRatio() > 1.0) {
      recommendations.add("Strategy shows good overall risk-adjusted returns but poor downside protection: " +
        "Consider implementing asymmetric stop losses");
    }
  }
  private String formatMetric(String name, double value, String unit) {
    return String.format("%s: %.2f%s", name, value, unit);
  }

  private void generateOverallAssessment(OptimizationOutcome outcome, RiskMetrics riskMetrics,
                                         StringBuilder assessment, Map<String, Double> metrics) {
    assessment.append("Strategy optimization completed with the following characteristics:\n");

    // Risk-adjusted returns assessment
    double sharpeRatio = riskMetrics.getSharpeRatio();
    if (sharpeRatio > 2.0) {
      assessment.append("- Excellent risk-adjusted returns (Sharpe > 2.0)\n");
    } else if (sharpeRatio > 1.0) {
      assessment.append("- Good risk-adjusted returns (Sharpe > 1.0)\n");
    } else {
      assessment.append("- Moderate risk-adjusted returns (Sharpe < 1.0)\n");
    }

    // Market correlation assessment
    double beta = metrics.getOrDefault("beta", 1.0);
    if (beta > 1.2) {
      assessment.append("- High market sensitivity (Beta > 1.2)\n");
    } else if (beta < 0.8) {
      assessment.append("- Low market correlation (Beta < 0.8)\n");
    } else {
      assessment.append("- Moderate market correlation (Beta 0.8-1.2)\n");
    }

    // Downside protection assessment
    double sortinoRatio = metrics.getOrDefault("sortino", 0.0);
    if (sortinoRatio > sharpeRatio) {
      assessment.append("- Strong downside protection (Sortino > Sharpe)\n");
    }

    // Market conditions impact
    assessment.append(String.format("Current market conditions: %.2f%% volatility, %.2f trend strength\n",
      marketConditions.getVolatility() * 100, marketConditions.getTrend()));
  }

  private void addRiskAdjustedRecommendations(Map<String, Double> metrics, RiskMetrics riskMetrics,
                                              List<String> recommendations) {
    // Calculate optimal position size based on dynamic risk metrics
    double positionSize = calculateOptimalPositionSize(riskMetrics);
    recommendations.add(String.format("Recommended position size: %.1f%% of portfolio - " +
        "Based on current market volatility and strategy Sharpe ratio",
      positionSize * 100));

    // Dynamic stop loss based on market conditions
    double stopLoss = calculateOptimalStopLoss(riskMetrics);
    recommendations.add(String.format("Recommended stop loss: %.1f%% below entry - " +
        "Adjusted for current market volatility",
      stopLoss * 100));

    // Beta-based recommendations
    Double beta = metrics.getOrDefault("beta", 0.0);
    if (beta > 1.2) {
      recommendations.add("Consider reducing position size in high market volatility periods - " +
        "Strategy shows high market sensitivity");
    }

    // Alpha-based recommendations
    Double alpha = metrics.getOrDefault("alpha", 0.0);
    if (alpha > 0.05) {
      recommendations.add("Strategy shows strong alpha generation - " +
        "Consider increasing allocation during favorable market conditions");
    }
  }

  private double calculateOptimalPositionSize(RiskMetrics riskMetrics) {
    double winRate = 0.5;
    double winLossRatio = 1.5;
    double kelly = winRate - ((1 - winRate) / winLossRatio);
    double adjustmentFactor = Math.min(1.0,
      (preferences.getVolatilityThreshold() / riskMetrics.getVolatility()) *
        (preferences.getMaxDrawdownThreshold() / riskMetrics.getMaxDrawdown()));
    return kelly * adjustmentFactor * 0.5;
  }
  private double calculateOptimalStopLoss(RiskMetrics riskMetrics) {
    double dailyVolatility = riskMetrics.getVolatility() / Math.sqrt(252);
    return dailyVolatility * 2;
  }
  private RiskAssessment assessRisk(Map<String, Double> metrics, MarketConditions conditions, RiskMetrics riskMetrics) {
    double riskScore = 0.0;
    double volatility = riskMetrics.getVolatility();
    double maxDrawdown = riskMetrics.getMaxDrawdown();
    double sharpeRatio = riskMetrics.getSharpeRatio();

    // Add safety checks for division by zero and null values
    riskScore += volatility * 0.3;
    riskScore += maxDrawdown * 0.3;
    riskScore += Math.abs(conditions.getTrend()) * 0.2;

    // Protect against division by zero for Sharpe ratio
    if (sharpeRatio != 0) {
      riskScore += (1.0 / Math.abs(sharpeRatio)) * 0.2;
    } else {
      riskScore += 0.2; // Max contribution for this component if Sharpe is 0
    }

    RiskLevel level;
    if (riskScore > 0.7) {
      level = RiskLevel.HIGH;
    } else if (riskScore > 0.4) {
      level = RiskLevel.MEDIUM;
    } else {
      level = RiskLevel.LOW;
    }
    return new RiskAssessment(level, riskScore);
  }
  private VolumeMetrics analyzeVolume() {
    List<Double> volumes = stockDataList.stream()
      .map(StockData::getVolume)
      .map(Long::doubleValue)
      .collect(Collectors.toList());

    // Calculate basic volume metrics
    double averageVolume = calculateMovingAverage(volumes, VOLUME_MA_PERIOD);
    double volumeVolatility = calculateVolumeVolatility(volumes);
    double volumeTrend = calculateVolumeTrend(volumes);
    List<LocalDate> volumeSpikeDates = identifyVolumeSpikes(stockDataList, averageVolume);
    double volumeMomentum = calculateVolumeMomentum(volumes);
    double priceVolumeCorrelation = calculatePriceVolumeCorrelation();
    Map<String, Double> volumeDistribution = calculateVolumeDistribution(volumes);

    return new VolumeMetrics(
      averageVolume,
      volumeVolatility,
      volumeTrend,
      volumeSpikeDates,
      volumeMomentum,
      priceVolumeCorrelation,
      volumeDistribution
    );
  }

  private double calculateVolumeVolatility(List<Double> volumes) {
    double mean = volumes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    return Math.sqrt(volumes.stream()
      .mapToDouble(v -> Math.pow(v - mean, 2))
      .average()
      .orElse(0.0)) / mean;
  }

  private double calculateVolumeTrend(List<Double> volumes) {
    if (volumes.size() < 2) return 0.0;

    double sumX = 0;
    double sumY = 0;
    double sumXY = 0;
    double sumX2 = 0;

    for (int i = 0; i < volumes.size(); i++) {
      sumX += i;
      sumY += volumes.get(i);
      sumXY += i * volumes.get(i);
      sumX2 += i * i;
    }

    int n = volumes.size();
    double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    return slope / volumes.get(0); // Normalize by initial volume
  }

  private List<LocalDate> identifyVolumeSpikes(List<StockData> data, double averageVolume) {
    return IntStream.range(0, data.size())
      .filter(i -> data.get(i).getVolume() > averageVolume * VOLUME_SPIKE_THRESHOLD)
      .mapToObj(i -> data.get(i).getDate())
      .collect(Collectors.toList());
  }

  private double calculateVolumeMomentum(List<Double> volumes) {
    if (volumes.size() < VOLUME_MA_PERIOD) return 0.0;

    double shortTermAvg = calculateMovingAverage(
      volumes.subList(volumes.size() - VOLUME_MA_PERIOD/2, volumes.size()),
      VOLUME_MA_PERIOD/2
    );

    double longTermAvg = calculateMovingAverage(
      volumes.subList(volumes.size() - VOLUME_MA_PERIOD, volumes.size()),
      VOLUME_MA_PERIOD
    );

    return (shortTermAvg - longTermAvg) / longTermAvg;
  }

  private double calculatePriceVolumeCorrelation() {
    List<Double> priceChanges = new ArrayList<>();
    List<Double> volumeChanges = new ArrayList<>();

    for (int i = 1; i < stockDataList.size(); i++) {
      StockData current = stockDataList.get(i);
      StockData previous = stockDataList.get(i - 1);

      double priceChange = (current.getClosePrice() - previous.getClosePrice()) / previous.getClosePrice();
      double volumeChange = (current.getVolume() - previous.getVolume()) / (double)previous.getVolume();

      priceChanges.add(priceChange);
      volumeChanges.add(volumeChange);
    }

    return calculateCorrelation(priceChanges, volumeChanges);
  }

  private Map<String, Double> calculateVolumeDistribution(List<Double> volumes) {
    Map<String, Double> distribution = new HashMap<>();

    // Calculate quartiles
    List<Double> sortedVolumes = new ArrayList<>(volumes);
    Collections.sort(sortedVolumes);

    int size = sortedVolumes.size();
    distribution.put("min", sortedVolumes.get(0));
    distribution.put("q1", sortedVolumes.get(size / 4));
    distribution.put("median", sortedVolumes.get(size / 2));
    distribution.put("q3", sortedVolumes.get(3 * size / 4));
    distribution.put("max", sortedVolumes.get(size - 1));

    return distribution;
  }

  private double calculateMovingAverage(List<Double> values, int period) {
    if (values.isEmpty()) return 0.0;
    return values.subList(Math.max(0, values.size() - period), values.size())
      .stream()
      .mapToDouble(Double::doubleValue)
      .average()
      .orElse(0.0);
  }

  private double calculateCorrelation(List<Double> x, List<Double> y) {
    if (x.size() != y.size() || x.isEmpty()) return 0.0;

    double xMean = x.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    double yMean = y.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

    double covariance = 0.0;
    double xVariance = 0.0;
    double yVariance = 0.0;

    for (int i = 0; i < x.size(); i++) {
      double xDiff = x.get(i) - xMean;
      double yDiff = y.get(i) - yMean;
      covariance += xDiff * yDiff;
      xVariance += xDiff * xDiff;
      yVariance += yDiff * yDiff;
    }

    if (xVariance == 0.0 || yVariance == 0.0) return 0.0;
    return covariance / Math.sqrt(xVariance * yVariance);
  }
  private RollingMetrics calculateRollingMetrics(OptimizationResult result) {
    List<LocalDate> dates = new ArrayList<>();
    List<Double> returns = new ArrayList<>();
    List<Double> volatilities = new ArrayList<>();
    List<Double> sharpeRatios = new ArrayList<>();
    List<Double> drawdowns = new ArrayList<>();
    Map<LocalDate, Double> efficiencyRatios = new HashMap<>();

    List<StockData> sortedData = new ArrayList<>(stockDataList);
    Collections.sort(sortedData);

    // Calculate daily returns
    List<Double> dailyReturns = new ArrayList<>();
    for (int i = 1; i < sortedData.size(); i++) {
      double prevClose = sortedData.get(i-1).getClosePrice();
      double currClose = sortedData.get(i).getClosePrice();
      dailyReturns.add((currClose - prevClose) / prevClose);
    }

    // Rolling window analysis
    for (int i = DEFAULT_ROLLING_WINDOW; i < sortedData.size(); i++) {
      List<Double> windowReturns = dailyReturns.subList(i - DEFAULT_ROLLING_WINDOW, i);

      // Calculate metrics for this window
      double windowReturn = windowReturns.stream()
        .mapToDouble(r -> 1 + r)
        .reduce(1, (a, b) -> a * b) - 1;

      double windowVol = calculateVolatility(windowReturns) * Math.sqrt(252);
      double windowSharpe = calculateSharpeRatio(windowReturns, preferences.getRiskFreeRate());
      double windowDrawdown = calculateMaxDrawdown(windowReturns);

      // Calculate efficiency ratio for this window
      double efficiencyRatio = calculateEfficiencyRatio(windowReturns);

      // Store results
      dates.add(sortedData.get(i).getDate());
      returns.add(windowReturn);
      volatilities.add(windowVol);
      sharpeRatios.add(windowSharpe);
      drawdowns.add(windowDrawdown);
      efficiencyRatios.put(sortedData.get(i).getDate(), efficiencyRatio);
    }

    return new RollingMetrics(dates, returns, volatilities, sharpeRatios, drawdowns, efficiencyRatios);
  }

  private double calculateEfficiencyRatio(List<Double> returns) {
    if (returns.isEmpty()) return 0.0;

    double totalReturn = returns.stream()
      .mapToDouble(r -> 1 + r)
      .reduce(1, (a, b) -> a * b) - 1;

    double pathLength = returns.stream()
      .mapToDouble(Math::abs)
      .sum();

    return pathLength == 0 ? 0 : Math.abs(totalReturn) / pathLength;
  }

  private StreakAnalysis analyzeStreaks(List<Double> returns) {
    List<Integer> streaks = new ArrayList<>();
    int currentStreak = 0;
    int maxWinStreak = 0;
    int maxLossStreak = 0;
    List<Integer> winStreaks = new ArrayList<>();
    List<Integer> lossStreaks = new ArrayList<>();
    List<Integer> streakDistribution = new ArrayList<>(Collections.nCopies(20, 0)); // Track up to 20-day streaks

    for (Double ret : returns) {
      if (ret > 0) {
        if (currentStreak > 0) {
          currentStreak++;
        } else {
          if (currentStreak < 0) {
            lossStreaks.add(-currentStreak);
            int idx = Math.min(-currentStreak, streakDistribution.size() - 1);
            streakDistribution.set(idx, streakDistribution.get(idx) + 1);
          }
          currentStreak = 1;
        }
        maxWinStreak = Math.max(maxWinStreak, currentStreak);
      } else if (ret < 0) {
        if (currentStreak < 0) {
          currentStreak--;
        } else {
          if (currentStreak > 0) {
            winStreaks.add(currentStreak);
            int idx = Math.min(currentStreak, streakDistribution.size() - 1);
            streakDistribution.set(idx, streakDistribution.get(idx) + 1);
          }
          currentStreak = -1;
        }
        maxLossStreak = Math.max(maxLossStreak, -currentStreak);
      }
    }

    // Handle final streak
    if (currentStreak > 0) {
      winStreaks.add(currentStreak);
    } else if (currentStreak < 0) {
      lossStreaks.add(-currentStreak);
    }

    double avgWinStreak = winStreaks.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);
    double avgLossStreak = lossStreaks.stream().mapToDouble(Integer::doubleValue).average().orElse(0.0);

    return new StreakAnalysis(
      maxWinStreak,
      maxLossStreak,
      avgWinStreak,
      avgLossStreak,
      currentStreak,
      streakDistribution
    );
  }
  private double calculateVolatility(List<Double> returns) {
    if (returns.size() < MIN_DATA_POINTS) return 0.0;
    double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    double variance = returns.stream()
      .mapToDouble(r -> Math.pow(r - mean, 2))
      .average()
      .orElse(0.0);
    return Math.sqrt(variance);
  }
  private double calculateSharpeRatio(List<Double> returns, double riskFreeRate) {
    if (returns.size() < MIN_DATA_POINTS) return 0.0;
    double meanReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    double volatility = calculateVolatility(returns);
    if (volatility == 0) return 0.0;
    return (meanReturn - riskFreeRate / 252) / volatility;
  }
  private double calculateMaxDrawdown(List<Double> returns) {
    double peak = 0.0;
    double maxDrawdown = 0.0;
    double currentValue = 1.0;
    for (double ret : returns) {
      currentValue *= (1 + ret);
      peak = Math.max(peak, currentValue);
      maxDrawdown = Math.max(maxDrawdown, (peak - currentValue) / peak);
    }
    return maxDrawdown;
  }
}


