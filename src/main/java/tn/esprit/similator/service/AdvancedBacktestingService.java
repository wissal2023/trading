package tn.esprit.similator.service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tn.esprit.similator.entity.StockData;
import tn.esprit.similator.entity.Trade;

@Service
public class AdvancedBacktestingService {
  private static final Logger logger = LoggerFactory.getLogger(AdvancedBacktestingService.class);



  @Data
  public static class BacktestMetrics {
    private double totalReturn;
    private double sharpeRatio;
    private double maxDrawdown;
    private double winRate;
    private List<Trade> trades;
    private Map<LocalDate, Double> equityCurve;

    public BacktestMetrics() {
      this.trades = new ArrayList<>();
      this.equityCurve = new HashMap<>();
    }
  }

  @Data
  public static class MultiStrategyResult {
    private Map<String, BacktestMetrics> strategyResults;
    private String bestStrategy;

    public MultiStrategyResult() {
      this.strategyResults = new HashMap<>();
    }
  }

  // Multi-Strategy Comparison
  public MultiStrategyResult compareStrategies(List<StockData> data, List<String> strategies) {
    MultiStrategyResult result = new MultiStrategyResult();
    double bestSharpeRatio = Double.NEGATIVE_INFINITY;

    // Ensure strategies list is not empty
    if (strategies == null || strategies.isEmpty()) {
      throw new IllegalArgumentException("Strategies list cannot be empty");
    }

    for (String strategy : strategies) {
      BacktestMetrics metrics = runSingleStrategyBacktest(data, strategy);
      result.getStrategyResults().put(strategy, metrics);

      // Robust comparison with null check
      if (metrics.getSharpeRatio() > bestSharpeRatio) {
        bestSharpeRatio = metrics.getSharpeRatio();
        result.setBestStrategy(strategy);
      }
    }

    // Fallback if no strategy is selected
    if (result.getBestStrategy() == null && !strategies.isEmpty()) {
      result.setBestStrategy(strategies.get(0)); // Default to first strategy
    }

    return result;
  }

  // Walk-Forward Optimization
  public BacktestMetrics walkForwardOptimization(List<StockData> data, String strategy, int windows) {
    // Ensure minimum data and windows
    if (data == null || data.size() < 100 || windows < 2) {
      throw new IllegalArgumentException("Insufficient data for walk-forward optimization");
    }

    int windowSize = Math.max(1, data.size() / windows);
    List<BacktestMetrics> windowResults = new ArrayList<>();

    for (int i = 0; i < windows - 1; i++) {
      int startTrain = i * windowSize;
      int endTrain = (i + 1) * windowSize;
      int startTest = endTrain;
      int endTest = Math.min((i + 2) * windowSize, data.size());

      // Validate window indices
      if (startTrain >= data.size() || endTest > data.size()) continue;

      // Training window
      List<StockData> trainingData = data.subList(startTrain, endTrain);
      // Testing window
      List<StockData> testingData = data.subList(startTest, endTest);

      // Optimize parameters on training data
      Map<String, Double> optimizedParams = optimizeParameters(trainingData, strategy);

      // Test on out-of-sample data
      BacktestMetrics windowMetrics = runSingleStrategyBacktest(testingData, strategy, optimizedParams);

      // Only add valid metrics
      if (windowMetrics != null && !windowMetrics.getTrades().isEmpty()) {
        windowResults.add(windowMetrics);
      }
    }

    // Robust aggregation with fallback
    return windowResults.isEmpty() ? createEmptyMetrics() : aggregateResults(windowResults);
  }

  private BacktestMetrics createEmptyMetrics() {
    BacktestMetrics emptyMetrics = new BacktestMetrics();
    emptyMetrics.setTotalReturn(0.0);
    emptyMetrics.setSharpeRatio(0.0);
    emptyMetrics.setMaxDrawdown(0.0);
    emptyMetrics.setWinRate(0.0);
    return emptyMetrics;
  }

  // Monte Carlo Simulation
  public List<BacktestMetrics> monteCarloSimulation(List<StockData> data, String strategy, int simulations) {
    List<BacktestMetrics> simulationResults = new ArrayList<>();
    Random random = new Random();

    for (int i = 0; i < simulations; i++) {
      List<StockData> simulatedData = generateSimulatedData(data, random);
      BacktestMetrics metrics = runSingleStrategyBacktest(simulatedData, strategy);
      simulationResults.add(metrics);
    }

    return simulationResults;
  }

  // Stress Testing
  public BacktestMetrics stressTest(List<StockData> data, String strategy) {
    // Create stress scenarios
    List<List<StockData>> scenarios = new ArrayList<>();
    scenarios.add(generateBearMarketScenario(data));
    scenarios.add(generateHighVolatilityScenario(data));
    scenarios.add(generateLowLiquidityScenario(data));

    List<BacktestMetrics> stressResults = scenarios.stream()
      .map(scenario -> runSingleStrategyBacktest(scenario, strategy))
      .collect(Collectors.toList());

    return getWorstScenarioResult(stressResults);
  }

  // Helper methods
  private BacktestMetrics runSingleStrategyBacktest(List<StockData> data, String strategy) {
    return runSingleStrategyBacktest(data, strategy, new HashMap<>());
  }

  private BacktestMetrics runSingleStrategyBacktest(
    List<StockData> data,
    String strategy,
    Map<String, Double> parameters) {

    BacktestMetrics metrics = new BacktestMetrics();
    double initialCapital = 100000.0;
    double currentCapital = initialCapital;
    int position = 0;
    boolean inPosition = false;

    // Add comprehensive logging
    logger.info("Starting backtest for strategy: {}", strategy);
    logger.info("Total data points: {}", data.size());
    logger.info("Initial parameters: {}", parameters);

    for (int i = 1; i < data.size(); i++) {
      StockData current = data.get(i);
      StockData previous = data.get(i - 1);

      // Trading logic based on strategy
      List<StockData> historicalData = data.subList(0, i + 1);
      String signal = generateSignal(historicalData, strategy, parameters);

      // Log each iteration's details
      logger.debug("Date: {}, Signal: {}, Current Price: {}",
        current.getDate(), signal, current.getClosePrice());

      // Buy logic with more robust checks
      if (signal.equals("BUY") && !inPosition) {
        int shares = (int) (currentCapital * 0.95 / current.getClosePrice());
        if (shares > 0) {
          currentCapital -= shares * current.getClosePrice();
          position = shares;
          inPosition = true;
          metrics.getTrades().add(new Trade(current.getDate(), "BUY", shares, current.getClosePrice()));
          logger.info("Buy signal executed: {} shares at ${}", shares, current.getClosePrice());
        }
      }
      // Sell logic with more robust checks
      else if (signal.equals("SELL") && inPosition) {
        currentCapital += position * current.getClosePrice();
        metrics.getTrades().add(new Trade(current.getDate(), "SELL", position, current.getClosePrice()));
        logger.info("Sell signal executed: {} shares at ${}", position, current.getClosePrice());
        position = 0;
        inPosition = false;
      }

      double portfolioValue = currentCapital + position * current.getClosePrice();
      metrics.getEquityCurve().put(current.getDate(), portfolioValue);
    }

    calculateMetrics(metrics, initialCapital);

    // Log final metrics for verification
    logger.info("Final backtest metrics: {}", metrics);

    return metrics;
  }


  private Map<String, Double> optimizeParameters(List<StockData> data, String strategy) {
    Map<String, Double> optimizedParams = new HashMap<>();
    // Example parameters for different strategies
    switch (strategy) {
      case "MovingAverage":
        optimizedParams.put("shortPeriod", optimizeParameter(data, 5, 50, 5));
        optimizedParams.put("longPeriod", optimizeParameter(data, 10, 200, 10));
        break;
      case "MeanReversion":
        optimizedParams.put("lookbackPeriod", optimizeParameter(data, 2, 20, 2));
        optimizedParams.put("stdDevMultiplier", optimizeParameter(data, 1.0, 3.0, 0.5));
        break;
      // Add more strategy parameter optimization cases
    }
    return optimizedParams;
  }

  private double optimizeParameter(List<StockData> data, double start, double end, double step) {
    double bestParam = start;
    double bestMetric = Double.NEGATIVE_INFINITY;

    for (double param = start; param <= end; param += step) {
      Map<String, Double> testParams = new HashMap<>();
      testParams.put("param", param);
      BacktestMetrics metrics = runSingleStrategyBacktest(data, "test", testParams);

      if (metrics.getSharpeRatio() > bestMetric) {
        bestMetric = metrics.getSharpeRatio();
        bestParam = param;
      }
    }

    return bestParam;
  }

  private List<StockData> generateSimulatedData(List<StockData> originalData, Random random) {
    List<StockData> simulatedData = new ArrayList<>();
    double volatility = calculateVolatility(originalData);

    for (int i = 0; i < originalData.size(); i++) {
      StockData original = originalData.get(i);
      double randomReturn = random.nextGaussian() * volatility;
      double simulatedPrice = original.getClosePrice() * (1 + randomReturn);

      simulatedData.add(new StockData(
        original.getDate(),
        simulatedPrice * 0.99, // Simulated open
        simulatedPrice * 1.02, // Simulated high
        simulatedPrice * 0.98, // Simulated low
        simulatedPrice,        // Simulated close
        original.getVolume()
      ));
    }

    return simulatedData;
  }

  private List<StockData> generateBearMarketScenario(List<StockData> data) {
    return data.stream()
      .map(d -> new StockData(
        d.getDate(),
        d.getOpenPrice() * 0.95,
        d.getHighPrice() * 0.95,
        d.getLowPrice() * 0.95,
        d.getClosePrice() * 0.95,
        d.getVolume()
      ))
      .collect(Collectors.toList());
  }

  private List<StockData> generateHighVolatilityScenario(List<StockData> data) {
    return data.stream()
      .map(d -> new StockData(
        d.getDate(),
        d.getOpenPrice() * (1 + 0.1),
        d.getHighPrice() * (1 + 0.2),
        d.getLowPrice() * (1 - 0.2),
        d.getClosePrice() * (1 - 0.1),
        d.getVolume()
      ))
      .collect(Collectors.toList());
  }

  private List<StockData> generateLowLiquidityScenario(List<StockData> data) {
    return data.stream()
      .map(d -> new StockData(
        d.getDate(),
        d.getOpenPrice(),
        d.getHighPrice(),
        d.getLowPrice(),
        d.getClosePrice(),
        d.getVolume() / 2
      ))
      .collect(Collectors.toList());
  }

  private void calculateMetrics(BacktestMetrics metrics, double initialCapital) {
    // Prevent division by zero and handle empty trade scenarios
    if (metrics.getTrades().isEmpty()) {
      metrics.setTotalReturn(0.0);
      metrics.setSharpeRatio(0.0);
      metrics.setMaxDrawdown(0.0);
      metrics.setWinRate(0.0);
      return;
    }

    // Existing calculation logic with additional safeguards
    List<Double> returns = new ArrayList<>();
    double peak = initialCapital;
    double maxDrawdown = 0;
    int winningTrades = 0;

    for (Map.Entry<LocalDate, Double> entry : metrics.getEquityCurve().entrySet()) {
      double value = entry.getValue();
      if (value > peak) {
        peak = value;
      }
      double drawdown = (peak - value) / Math.max(peak, 1.0);
      maxDrawdown = Math.max(maxDrawdown, drawdown);

      if (!returns.isEmpty()) {
        double lastReturn = returns.get(returns.size() - 1);
        double dailyReturn = (value - lastReturn) / Math.max(lastReturn, 1.0);
        returns.add(dailyReturn);
      } else if (returns.isEmpty() && value > 0) {
        returns.add(0.0);
      }
    }

    // Safe trade winning calculation
    for (int i = 1; i < metrics.getTrades().size(); i += 2) {
      if (i < metrics.getTrades().size() &&
        metrics.getTrades().get(i).getPrice() > metrics.getTrades().get(i-1).getPrice()) {
        winningTrades++;
      }
    }

    // Prevent division by zero
    double finalValue = metrics.getEquityCurve().get(
      metrics.getEquityCurve().keySet().stream().max(LocalDate::compareTo).orElse(null)
    );

    metrics.setTotalReturn(
      finalValue > 0 ? (finalValue - initialCapital) / initialCapital * 100 : 0.0
    );

    metrics.setSharpeRatio(calculateSharpeRatio(returns));
    metrics.setMaxDrawdown(maxDrawdown * 100);

    metrics.setWinRate(
      metrics.getTrades().size() > 1 ?
        (double) winningTrades / (metrics.getTrades().size() / 2) * 100 :
        0.0
    );
  }


  private double calculateSharpeRatio(List<Double> returns) {
    // Prevent calculation issues with insufficient data
    if (returns == null || returns.size() < 2) return 0.0;

    double meanReturn = returns.stream()
      .filter(r -> !Double.isNaN(r) && !Double.isInfinite(r))
      .mapToDouble(r -> r)
      .average()
      .orElse(0.0);

    double stdDev = Math.sqrt(
      returns.stream()
        .filter(r -> !Double.isNaN(r) && !Double.isInfinite(r))
        .mapToDouble(r -> Math.pow(r - meanReturn, 2))
        .average()
        .orElse(0.0)
    );

    // Prevent division by zero and handle edge cases
    return stdDev > 0 ? meanReturn / stdDev * Math.sqrt(252) : 0.0;
  }

  private double calculateVolatility(List<StockData> data) {
    List<Double> returns = new ArrayList<>();
    for (int i = 1; i < data.size(); i++) {
      double dailyReturn = (data.get(i).getClosePrice() - data.get(i-1).getClosePrice())
        / data.get(i-1).getClosePrice();
      returns.add(dailyReturn);
    }

    double meanReturn = returns.stream().mapToDouble(r -> r).average().orElse(0);
    return Math.sqrt(
      returns.stream()
        .mapToDouble(r -> Math.pow(r - meanReturn, 2))
        .average()
        .orElse(0)
    );
  }

  private String generateSignal(List<StockData> data, String strategy, Map<String, Double> parameters) {
    logger.debug("Generating signal for strategy: {}", strategy);
    logger.debug("Data points available: {}", data.size());
    logger.debug("Parameters: {}", parameters);

    try {
      switch (strategy) {
        case "MovingAverage":
          return generateMovingAverageSignal(data, parameters);
        case "MeanReversion":
          return generateMeanReversionSignal(data, parameters);
        default:
          logger.warn("Unknown strategy: {}", strategy);
          return "HOLD";
      }
    } catch (Exception e) {
      logger.error("Error generating signal: {}", e.getMessage());
      return "HOLD";
    }
  }


  private String generateMovingAverageSignal(List<StockData> data, Map<String, Double> parameters) {
    int shortPeriod = Math.max(5, parameters.getOrDefault("shortPeriod", 20.0).intValue());
    int longPeriod = Math.max(10, parameters.getOrDefault("longPeriod", 50.0).intValue());

    if (data.size() < longPeriod) return "HOLD";

    double currentShortMA = calculateSMA(data, shortPeriod);
    double currentLongMA = calculateSMA(data, longPeriod);

    // Previous period MAs for crossover detection
    List<StockData> prevData = data.subList(0, data.size() - 1);
    double prevShortMA = calculateSMA(prevData, shortPeriod);
    double prevLongMA = calculateSMA(prevData, longPeriod);

    // Golden Cross (bullish): Short MA crosses above Long MA
    if (prevShortMA <= prevLongMA && currentShortMA > currentLongMA) {
      return "BUY";
    }

    // Death Cross (bearish): Short MA crosses below Long MA
    if (prevShortMA >= prevLongMA && currentShortMA < currentLongMA) {
      return "SELL";
    }

    return "HOLD";
  }

  private String generateMeanReversionSignal(List<StockData> data, Map<String, Double> parameters) {
    // Ensure we have enough data
    if (data.size() < 20) return "HOLD";

    // Default parameters
    int lookback = parameters.getOrDefault("lookbackPeriod", 20.0).intValue();
    double multiplier = parameters.getOrDefault("stdDevMultiplier", 2.0);

    // Calculate mean and standard deviation
    double mean = calculateSMA(data, lookback);
    double stdDev = calculateStdDev(data, lookback, mean);
    double currentPrice = data.get(data.size() - 1).getClosePrice();

    // Mean reversion logic
    if (currentPrice < mean - multiplier * stdDev) return "BUY";
    if (currentPrice > mean + multiplier * stdDev) return "SELL";
    return "HOLD";
  }
  private double calculateSMA(List<StockData> data, int period) {
    // Ensure period doesn't exceed data size
    period = Math.min(period, data.size());

    // Take the last 'period' elements
    List<StockData> subset = data.subList(
      Math.max(0, data.size() - period),
      data.size()
    );

    return subset.stream()
      .mapToDouble(StockData::getClosePrice)
      .average()
      .orElse(0.0);
  }

  private double calculateStdDev(List<StockData> data, int period, double mean) {
    return Math.sqrt(
      data.subList(data.size() - period, data.size()).stream()
        .mapToDouble(d -> Math.pow(d.getClosePrice() - mean, 2))
        .average()
        .orElse(0.0)
    );
  }
  private BacktestMetrics aggregateResults(List<BacktestMetrics> windowResults) {
    BacktestMetrics aggregated = new BacktestMetrics();

    // Calculate average metrics across all windows
    aggregated.setTotalReturn(
      windowResults.stream()
        .mapToDouble(BacktestMetrics::getTotalReturn)
        .average()
        .orElse(0.0)
    );

    aggregated.setSharpeRatio(
      windowResults.stream()
        .mapToDouble(BacktestMetrics::getSharpeRatio)
        .average()
        .orElse(0.0)
    );

    aggregated.setMaxDrawdown(
      windowResults.stream()
        .mapToDouble(BacktestMetrics::getMaxDrawdown)
        .max()
        .orElse(0.0)
    );

    aggregated.setWinRate(
      windowResults.stream()
        .mapToDouble(BacktestMetrics::getWinRate)
        .average()
        .orElse(0.0)
    );

    // Combine all trades from different windows
    List<Trade> allTrades = windowResults.stream()
      .flatMap(result -> result.getTrades().stream())
      .sorted(Comparator.comparing(Trade::getDate))
      .collect(Collectors.toList());
    aggregated.setTrades(allTrades);

    // Combine equity curves from different windows
    Map<LocalDate, Double> combinedEquityCurve = new TreeMap<>();
    windowResults.forEach(result ->
      result.getEquityCurve().forEach((date, value) ->
        combinedEquityCurve.merge(date, value, Double::sum)
      )
    );

    // Average the equity curve values where windows overlap
    Map<LocalDate, Long> dateCount = new HashMap<>();
    windowResults.forEach(result ->
      result.getEquityCurve().keySet().forEach(date ->
        dateCount.merge(date, 1L, Long::sum)
      )
    );

    Map<LocalDate, Double> averagedEquityCurve = combinedEquityCurve.entrySet().stream()
      .collect(Collectors.toMap(
        Map.Entry::getKey,
        e -> e.getValue() / dateCount.get(e.getKey())
      ));

    aggregated.setEquityCurve(averagedEquityCurve);

    return aggregated;
  }

  private BacktestMetrics getWorstScenarioResult(List<BacktestMetrics> stressResults) {
    // Create a scoring system to determine the worst scenario
    // Lower score means worse performance
    Map<BacktestMetrics, Double> scenarioScores = new HashMap<>();

    for (BacktestMetrics result : stressResults) {
      double score = 0.0;

      // Normalize and weight different metrics
      // Higher weight means more importance in determining worst scenario
      score += normalizeMetric(result.getTotalReturn(), stressResults, true) * 0.3;  // 30% weight
      score += normalizeMetric(result.getSharpeRatio(), stressResults, true) * 0.3;  // 30% weight
      score += normalizeMetric(result.getMaxDrawdown(), stressResults, false) * 0.25; // 25% weight
      score += normalizeMetric(result.getWinRate(), stressResults, true) * 0.15;      // 15% weight

      scenarioScores.put(result, score);
    }

    // Return the scenario with the lowest score (worst performance)
    return scenarioScores.entrySet().stream()
      .min(Map.Entry.comparingByValue())
      .map(Map.Entry::getKey)
      .orElseThrow(() -> new RuntimeException("No stress test results available"));
  }

  private double normalizeMetric(double value, List<BacktestMetrics> results, boolean higherIsBetter) {
    // Extract the specific metric from all results
    Function<BacktestMetrics, Double> metricExtractor;
    if (Double.compare(value, results.get(0).getTotalReturn()) == 0) {
      metricExtractor = BacktestMetrics::getTotalReturn;
    } else if (Double.compare(value, results.get(0).getSharpeRatio()) == 0) {
      metricExtractor = BacktestMetrics::getSharpeRatio;
    } else if (Double.compare(value, results.get(0).getMaxDrawdown()) == 0) {
      metricExtractor = BacktestMetrics::getMaxDrawdown;
    } else {
      metricExtractor = BacktestMetrics::getWinRate;
    }

    double min = results.stream().mapToDouble(metricExtractor::apply).min().orElse(0.0);
    double max = results.stream().mapToDouble(metricExtractor::apply).max().orElse(1.0);

    // Avoid division by zero
    if (Double.compare(max, min) == 0) {
      return 0.5;
    }

    // Normalize to [0,1] range
    double normalized = (value - min) / (max - min);

    // Invert if lower values are better
    return higherIsBetter ? normalized : 1 - normalized;
  }

}
