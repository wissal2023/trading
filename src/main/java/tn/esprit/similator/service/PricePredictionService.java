package tn.esprit.similator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.esprit.similator.controller.BacktestingApplication;
import tn.esprit.similator.entity.PredictionRequest;
import tn.esprit.similator.entity.PredictionResponse;
import tn.esprit.similator.entity.RandomForestClassifier;
import tn.esprit.similator.entity.StockData;
import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
@Service
@Slf4j
public class PricePredictionService {


  private static final int MIN_TRAINING_DAYS = 60; // Reduced from 252 to allow for shorter training periods
  private static final int PREFERRED_TRAINING_DAYS = 252; // Keep track of the ideal period
  private static final int MIN_TECHNICAL_ANALYSIS_DAYS = 50;
  private static final int MAX_LOOKBACK = 50;
  private static final int[] MOMENTUM_PERIODS = {5, 10, 20, 30};
  private static final int[] VOLATILITY_PERIODS = {5, 10, 20, 30};
  private static final int[] MA_PERIODS = {5, 10, 20, 50, 200};
  private static final double ENSEMBLE_WEIGHT_RECENT = 0.6;
  private static final double ENSEMBLE_WEIGHT_HISTORICAL = 0.4;
  @Autowired
  private BacktestingApplication backtestingApplication;
  @Value("${prediction.market.hours.start:21:00}")
  private String marketHoursStart;

  @Value("${prediction.market.hours.end:16:00}")
  private String marketHoursEnd;
  public PredictionResponse predictDirection(PredictionRequest request) {
    try {
      log.debug("Starting prediction process for symbol: {}", request.getSymbol());
      validateRequest(request);

      LocalDateTime currentDateTime = LocalDateTime.now();
      validateMarketHours(currentDateTime);

      LocalDate targetDate = calculateTargetDate(
        currentDateTime.toLocalDate(),
        request.getPredictionTimeframe(),
        request.getTimeframeUnit()
      );

      List<StockData> stockData = fetchAndValidateStockData(
        request.getSymbol(),
        request.getStartDate(),
        currentDateTime.toLocalDate()
      );

      log.debug("Successfully fetched {} data points for {}",
        stockData.size(), request.getSymbol());

      validateDataSize(stockData);

      // Extract enhanced features
      double[][] features = extractFeatures(stockData);
      log.debug("Successfully extracted {} features for {} data points",
        features[0].length, features.length);

      int[] labels = createLabels(stockData, request.getPredictionTimeframe(), MAX_LOOKBACK);
      validateFeatureLabels(features, labels);

      // Train ensemble models
      RandomForestClassifier recentModel = trainModel(
        getRecentData(features, 126), // Last 6 months
        getRecentData(labels, 126)
      );

      RandomForestClassifier historicalModel = trainModel(features, labels);

      // Get predictions from both models
      double[] latestFeatures = features[features.length - 1];
      double recentConfidence = calculateConfidence(recentModel, latestFeatures);
      double historicalConfidence = calculateConfidence(historicalModel, latestFeatures);

      // Weighted ensemble prediction
      double ensembleConfidence = calculateEnsembleConfidence(
        recentConfidence,
        historicalConfidence
      );

      boolean isPriceGoingUp = predictEnsemble(
        recentModel,
        historicalModel,
        latestFeatures
      );

      // Enhanced market indicators
      Map<String, Double> indicators = calculateEnhancedIndicators(stockData);

      // Add market regime analysis
      indicators.putAll(analyzeMarketRegime(stockData));

      log.info("Prediction generated for {}: Direction={}, Confidence={}",
        request.getSymbol(),
        isPriceGoingUp ? "Bullish" : "Bearish",
        String.format("%.2f", ensembleConfidence));

      return buildPredictionResponse(
        request,
        currentDateTime.toLocalDate(),
        targetDate,
        isPriceGoingUp,
        indicators,
        ensembleConfidence
      );

    } catch (Exception e) {
      handlePredictionError(request, e);
      throw e;
    }
  }

  private void validateRequest(PredictionRequest request) {
    if (request == null || request.getSymbol() == null || request.getSymbol().trim().isEmpty()) {
      throw new PredictionServiceException("Invalid request: Symbol cannot be null or empty");
    }

    if (request.getStartDate() == null) {
      throw new PredictionServiceException("Start date cannot be null");
    }

    if (request.getStartDate().plusDays(MIN_TRAINING_DAYS).isAfter(LocalDate.now())) {
      throw new PredictionServiceException(
        String.format("Start date must be at least %d days before current date for sufficient training data",
          MIN_TRAINING_DAYS)
      );
    }
  }

  private void validateMarketHours(LocalDateTime currentDateTime) {
    LocalTime currentTime = currentDateTime.toLocalTime();
    LocalTime marketStart = LocalTime.parse(marketHoursStart);
    LocalTime marketEnd = LocalTime.parse(marketHoursEnd);

    if (currentDateTime.getDayOfWeek().getValue() >= 6 ||
      currentTime.isBefore(marketStart) ||
      currentTime.isAfter(marketEnd)) {
      log.warn("Prediction requested outside market hours");
    }
  }

  private List<StockData> fetchAndValidateStockData(
    String symbol, LocalDate startDate, LocalDate endDate) {
    List<StockData> stockData = backtestingApplication.fetchStockData(
      symbol, startDate, endDate);

    if (stockData == null || stockData.isEmpty()) {
      throw new PredictionServiceException(
        "No stock data available for symbol: " + symbol);
    }

    if (stockData.size() < MIN_TECHNICAL_ANALYSIS_DAYS) {
      throw new PredictionServiceException(
        String.format("Insufficient historical data for prediction. Need at least %d data points, but got %d.",
          MIN_TECHNICAL_ANALYSIS_DAYS, stockData.size())
      );
    }

    return stockData;
  }
  private LocalDate calculateTargetDate(LocalDate currentDate, int timeframe, String unit) {
    return switch (unit.toUpperCase()) {
      case "DAYS" -> currentDate.plusDays(timeframe);
      case "WEEKS" -> currentDate.plusWeeks(timeframe);
      case "MONTHS" -> currentDate.plusMonths(timeframe);
      default -> throw new IllegalArgumentException("Invalid timeframe unit: " + unit);
    };
  }
  private double[][] extractFeatures(List<StockData> stockData) {
    validateDataForFeatures(stockData);

    // Calculate valid data points using the same logic as label creation
    int maxPeriod = Collections.max(Arrays.asList(Arrays.stream(MA_PERIODS).boxed().toArray(Integer[]::new)));
    int startIndex = Math.max(maxPeriod, MAX_LOOKBACK);
    int validDataPoints = stockData.size() - startIndex;

    int totalFeatures = calculateTotalFeatures();
    double[][] features = new double[validDataPoints][totalFeatures];

    for (int i = 0; i < validDataPoints; i++) {
      int dataIndex = i + startIndex;
      features[i] = calculateFeatureVector(stockData, dataIndex);
    }

    // Add validation for feature extraction
    if (features.length == 0) {
      throw new PredictionServiceException("No features extracted - check data size and lookback periods");
    }

    return normalizeFeatures(features);
  }

  private void validateFeatureLabels(double[][] features, int[] labels) {
    if (features == null || labels == null) {
      throw new PredictionServiceException("Features and labels cannot be null");
    }

    if (features.length != labels.length) {
      throw new PredictionServiceException(
        String.format("Feature length (%d) does not match label length (%d). " +
            "Features start from index %d, Labels start from index %d",
          features.length, labels.length,
          Math.max(Collections.max(Arrays.asList(Arrays.stream(MA_PERIODS).boxed().toArray(Integer[]::new))), MAX_LOOKBACK),
          Math.max(Collections.max(Arrays.asList(Arrays.stream(MA_PERIODS).boxed().toArray(Integer[]::new))), MAX_LOOKBACK)
        )
      );
    }

    if (features.length == 0) {
      throw new PredictionServiceException("No features or labels available for training");
    }

    // Validate feature values
    for (int i = 0; i < features.length; i++) {
      for (int j = 0; j < features[i].length; j++) {
        if (Double.isNaN(features[i][j]) || Double.isInfinite(features[i][j])) {
          throw new PredictionServiceException(
            String.format("Invalid feature value at position [%d, %d]", i, j)
          );
        }
      }
    }

    // Log data sizes for debugging
    log.debug("Feature array size: {}", features.length);
    log.debug("Label array size: {}", labels.length);
    log.debug("Number of features per row: {}", features[0].length);
  }

  private double[] calculateFeatureVector(List<StockData> data, int currentIndex) {
    List<Double> featureVector = new ArrayList<>();

    // Add momentum features
    for (int period : MOMENTUM_PERIODS) {
      featureVector.add(calculateMomentum(data, currentIndex, period));
    }

    // Add volatility features
    for (int period : VOLATILITY_PERIODS) {
      featureVector.add(calculateVolatility(data, currentIndex, period));
    }

    // Add moving average features
    for (int period : MA_PERIODS) {
      featureVector.add(calculatePriceToMA(data, currentIndex, period));
    }

    // Add volume analysis
    featureVector.add(calculateRelativeVolume(data, currentIndex, 20));
    featureVector.add(calculateVolumeWeightedPrice(data, currentIndex, 5));
    featureVector.add(calculateOnBalanceVolume(data, currentIndex, 10));

    // Add technical indicators
    featureVector.add(calculateRSI(data, currentIndex, 14));
    featureVector.add(calculateMACD(data, currentIndex));
    featureVector.add(calculateBollingerBandPosition(data, currentIndex, 20));
    featureVector.add(calculateATR(data, currentIndex, 14));
    featureVector.add(calculateStochastic(data, currentIndex, 14));

    // Add market regime indicators
    featureVector.add(calculateTrendStrength(data, currentIndex));
    featureVector.add(calculateVolatilityRegime(data, currentIndex));

    return featureVector.stream().mapToDouble(Double::doubleValue).toArray();
  }
  private double calculateVolumeWeightedPrice(List<StockData> data, int currentIndex, int period) {
    double sumVolumePrice = 0;
    double sumVolume = 0;

    for (int i = currentIndex - period + 1; i <= currentIndex; i++) {
      StockData daily = data.get(i);
      double volume = daily.getVolume();
      sumVolumePrice += daily.getClosePrice() * volume;
      sumVolume += volume;
    }

    return sumVolumePrice / sumVolume;
  }
  private double calculateOnBalanceVolume(List<StockData> data, int currentIndex, int period) {
    double obv = 0;

    for (int i = currentIndex - period + 1; i <= currentIndex; i++) {
      StockData current = data.get(i);
      StockData previous = data.get(i - 1);

      if (current.getClosePrice() > previous.getClosePrice()) {
        obv += current.getVolume();
      } else if (current.getClosePrice() < previous.getClosePrice()) {
        obv -= current.getVolume();
      }
    }

    return obv;
  }
  private double calculateTrendStrength(List<StockData> data, int currentIndex) {
    double sma20 = calculateMovingAverage(data, currentIndex, 20);
    double sma50 = calculateMovingAverage(data, currentIndex, 50);
    double sma200 = calculateMovingAverage(data, currentIndex, 200);

    int trendScore = 0;
    trendScore += (sma20 > sma50) ? 1 : -1;
    trendScore += (sma50 > sma200) ? 1 : -1;
    trendScore += (data.get(currentIndex).getClosePrice() > sma20) ? 1 : -1;

    return trendScore / 3.0;
  }
  private double calculateVolatilityRegime(List<StockData> data, int currentIndex) {
    double shortTermVol = calculateVolatility(data, currentIndex, 10);
    double longTermVol = calculateVolatility(data, currentIndex, 30);

    return shortTermVol / longTermVol;
  }
  private Map<String, Double> analyzeMarketRegime(List<StockData> data) {
    int lastIndex = data.size() - 1;
    Map<String, Double> regime = new HashMap<>();

    regime.put("trendStrength", calculateTrendStrength(data, lastIndex));
    regime.put("volatilityRegime", calculateVolatilityRegime(data, lastIndex));
    regime.put("volumeProfile", calculateRelativeVolume(data, lastIndex, 20));

    return regime;
  }
  private double calculateEnsembleConfidence(double recentConfidence, double historicalConfidence) {
    return (recentConfidence * ENSEMBLE_WEIGHT_RECENT) +
      (historicalConfidence * ENSEMBLE_WEIGHT_HISTORICAL);
  }
  private boolean predictEnsemble(
    RandomForestClassifier recentModel,
    RandomForestClassifier historicalModel,
    double[] features
  ) {
    double recentProb = recentModel.predictProbability(features);
    double historicalProb = historicalModel.predictProbability(features);

    double ensembleProb = (recentProb * ENSEMBLE_WEIGHT_RECENT) +
      (historicalProb * ENSEMBLE_WEIGHT_HISTORICAL);

    return ensembleProb > 0.5;
  }
  private Map<String, Double> calculateEnhancedIndicators(List<StockData> data) {
    int lastIndex = data.size() - 1;
    Map<String, Double> indicators = new HashMap<>();
    // Technical indicators
    indicators.put("RSI", calculateRSI(data, lastIndex, 14));
    indicators.put("MACD", calculateMACD(data, lastIndex));
    indicators.put("BollingerPosition", calculateBollingerBandPosition(data, lastIndex, 20));
    indicators.put("ATR", calculateATR(data, lastIndex, 14));
    // Momentum indicators
    for (int period : MOMENTUM_PERIODS) {
      indicators.put(period + "DayMomentum", calculateMomentum(data, lastIndex, period));
    }
    // Volume indicators
    indicators.put("RelativeVolume", calculateRelativeVolume(data, lastIndex, 20));
    indicators.put("VWAP", calculateVolumeWeightedPrice(data, lastIndex, 5));
    indicators.put("OBV", calculateOnBalanceVolume(data, lastIndex, 10));
    return indicators;
  }
  private PredictionResponse buildPredictionResponse(
    PredictionRequest request,
    LocalDate currentDate,
    LocalDate targetDate,
    boolean isPriceGoingUp,
    Map<String, Double> indicators,
    double confidence
  ) {
    return new PredictionResponse(
      request.getSymbol(),
      currentDate,
      targetDate,
      isPriceGoingUp,
      isPriceGoingUp ? "Bullish" : "Bearish",
      indicators,
      confidence
    );
  }
  private void validateDataSize(List<StockData> stockData) {
    int requiredSize = Math.max(MIN_TRAINING_DAYS, MIN_TECHNICAL_ANALYSIS_DAYS);

    if (stockData == null || stockData.isEmpty()) {
      throw new PredictionServiceException("Stock data cannot be null or empty");
    }

    if (stockData.size() < requiredSize) {
      throw new PredictionServiceException(
        String.format("Insufficient historical data. Need at least %d data points, but got %d.",
          requiredSize, stockData.size())
      );
    }
// Find the maximum period needed among all calculations
    int maxPeriod = Math.max(
      Collections.max(Arrays.asList(Arrays.stream(MA_PERIODS).boxed().toArray(Integer[]::new))),
      Math.max(MIN_TRAINING_DAYS, MIN_TECHNICAL_ANALYSIS_DAYS)
    );

    if (stockData == null || stockData.isEmpty()) {
      throw new PredictionServiceException("Stock data cannot be null or empty");
    }

    if (stockData.size() < maxPeriod) {
      throw new PredictionServiceException(
        String.format("Insufficient historical data. Need at least %d data points for the longest MA period, but got %d.",
          maxPeriod, stockData.size())
      );
    }
    // Check for data continuity
    LocalDate previousDate = null;
    for (StockData data : stockData) {
      if (previousDate != null) {
        long daysBetween = ChronoUnit.DAYS.between(previousDate, data.getDate());
        if (daysBetween > 3) { // Allow for weekends and single holidays
          log.warn("Gap detected in stock data between {} and {}", previousDate, data.getDate());
        }
      }
      previousDate = data.getDate();
    }
  }
  private <T> T[] getRecentData(T[] data, int recentDays) {
    if (data == null || data.length == 0) {
      throw new PredictionServiceException("Cannot get recent data from empty array");
    }

    int startIndex = Math.max(0, data.length - recentDays);
    @SuppressWarnings("unchecked")
    T[] result = (T[]) Array.newInstance(data.getClass().getComponentType(), data.length - startIndex);
    System.arraycopy(data, startIndex, result, 0, data.length - startIndex);
    return result;
  }
  // Overloaded method for primitive int array
  private int[] getRecentData(int[] data, int recentDays) {
    if (data == null || data.length == 0) {
      throw new PredictionServiceException("Cannot get recent data from empty array");
    }

    int startIndex = Math.max(0, data.length - recentDays);
    int[] result = new int[data.length - startIndex];
    System.arraycopy(data, startIndex, result, 0, data.length - startIndex);
    return result;
  }
  // Overloaded method for 2D double array
  private double[][] getRecentData(double[][] data, int recentDays) {
    if (data == null || data.length == 0) {
      throw new PredictionServiceException("Cannot get recent data from empty array");
    }

    int startIndex = Math.max(0, data.length - recentDays);
    double[][] result = new double[data.length - startIndex][];
    System.arraycopy(data, startIndex, result, 0, data.length - startIndex);
    return result;
  }
  private void handlePredictionError(PredictionRequest request, Exception e) {
    String symbol = request != null ? request.getSymbol() : "unknown";

    if (e instanceof PredictionServiceException) {
      log.error("Prediction service error for symbol {}: {}", symbol, e.getMessage());
    } else {
      log.error("Unexpected error in prediction service for symbol: {}", symbol, e);
      // Additional error details for debugging
      log.debug("Error stack trace:", e);

      if (request != null) {
        log.debug("Request details: StartDate={}, TimeFrame={}, Unit={}",
          request.getStartDate(),
          request.getPredictionTimeframe(),
          request.getTimeframeUnit());
      }
    }
    // You might want to send alerts for critical errors
    if (!(e instanceof PredictionServiceException)) {
      // Example: alertingService.sendAlert("Prediction Service Error", e);
      log.warn("Critical error occurred in prediction service");
    }
  }
  private int calculateTotalFeatures() {
    return MOMENTUM_PERIODS.length +      // Momentum features
      VOLATILITY_PERIODS.length +    // Volatility features
      MA_PERIODS.length +            // Moving average features
      11;                            // Additional features:
    // - Relative Volume
    // - VWAP
    // - OBV
    // - RSI
    // - MACD
    // - Bollinger Band Position
    // - ATR
    // - Stochastic
    // - Trend Strength
    // - Volatility Regime
    // - Volume Profile
  }
  private double[][] normalizeFeatures(double[][] features) {
    if (features == null || features.length == 0) {
      return features;
    }

    int numFeatures = features[0].length;
    double[][] normalizedFeatures = new double[features.length][numFeatures];
    // Calculate min and max for each feature
    double[] minValues = new double[numFeatures];
    double[] maxValues = new double[numFeatures];
    Arrays.fill(minValues, Double.MAX_VALUE);
    Arrays.fill(maxValues, Double.MIN_VALUE);

    // Find min and max values
    for (double[] feature : features) {
      for (int j = 0; j < numFeatures; j++) {
        minValues[j] = Math.min(minValues[j], feature[j]);
        maxValues[j] = Math.max(maxValues[j], feature[j]);
      }
    }
    // Normalize features
    for (int i = 0; i < features.length; i++) {
      for (int j = 0; j < numFeatures; j++) {
        if (Math.abs(maxValues[j] - minValues[j]) > 1e-10) { // Avoid division by zero
          normalizedFeatures[i][j] = (features[i][j] - minValues[j]) /
            (maxValues[j] - minValues[j]);
        } else {
          normalizedFeatures[i][j] = features[i][j]; // Keep original if min == max
        }
        // Handle edge cases
        if (Double.isNaN(normalizedFeatures[i][j])) {
          normalizedFeatures[i][j] = 0.0;
          log.warn("NaN value encountered during normalization at position [{}, {}]", i, j);
        }
      }
    }

    return normalizedFeatures;
  }
  private double calculateRelativeVolume(List<StockData> data, int currentIndex, int period) {
    double avgVolume = 0;
    for (int i = currentIndex - period; i < currentIndex; i++) {
      avgVolume += data.get(i).getVolume();
    }
    avgVolume /= period;
    return data.get(currentIndex).getVolume() / avgVolume;
  }
  private double calculateStochastic(List<StockData> data, int currentIndex, int period) {
    double currentClose = data.get(currentIndex).getClosePrice();
    double lowestLow = Double.MAX_VALUE;
    double highestHigh = Double.MIN_VALUE;

    for (int i = currentIndex - period + 1; i <= currentIndex; i++) {
      lowestLow = Math.min(lowestLow, data.get(i).getLowPrice());
      highestHigh = Math.max(highestHigh, data.get(i).getHighPrice());
    }

    return ((currentClose - lowestLow) / (highestHigh - lowestLow)) * 100;
  }
  private double calculateRSI(List<StockData> data, int currentIndex, int period) {
    double[] gains = new double[period];
    double[] losses = new double[period];

    for (int i = 0; i < period; i++) {
      double change = data.get(currentIndex - i).getClosePrice() -
        data.get(currentIndex - i - 1).getClosePrice();
      if (change >= 0) {
        gains[i] = change;
        losses[i] = 0;
      } else {
        gains[i] = 0;
        losses[i] = -change;
      }
    }

    double avgGain = Arrays.stream(gains).average().orElse(0.0);
    double avgLoss = Arrays.stream(losses).average().orElse(0.0);

    if (avgLoss == 0) return 100;

    double rs = avgGain / avgLoss;
    return 100 - (100 / (1 + rs));
  }
  private double calculateMACD(List<StockData> data, int currentIndex) {
    // Ensure we have enough data points for both EMAs
    if (currentIndex < 26) {  // 26 is the longer EMA period
      throw new IllegalArgumentException("Insufficient data points for MACD calculation");
    }

    double ema12 = calculateEMA(data, currentIndex, 12);
    double ema26 = calculateEMA(data, currentIndex, 26);
    return ema12 - ema26;
  }
  private double calculateEMA(List<StockData> data, int currentIndex, int period) {
    // Validate input parameters
    if (currentIndex < period - 1) {
      throw new IllegalArgumentException("Insufficient data points for EMA calculation");
    }

    // Calculate initial SMA as the first EMA value
    double sum = 0;
    for (int i = currentIndex - period + 1; i <= currentIndex; i++) {
      sum += data.get(i).getClosePrice();
    }
    double ema = sum / period;

    // Calculate multiplier
    double multiplier = 2.0 / (period + 1);

    // Calculate EMA using the smoothing formula
    for (int i = currentIndex - period + 1; i <= currentIndex; i++) {
      ema = (data.get(i).getClosePrice() - ema) * multiplier + ema;
    }

    return ema;
  }
  // Helper method to ensure we have enough data points for feature extraction
  private void validateDataForFeatures(List<StockData> data) {
    // Find the maximum period needed among all calculations
    int maxPeriod = Math.max(
      Collections.max(Arrays.asList(Arrays.stream(MA_PERIODS).boxed().toArray(Integer[]::new))),
      Math.max(MAX_LOOKBACK,
        Math.max(
          Collections.max(Arrays.asList(Arrays.stream(MOMENTUM_PERIODS).boxed().toArray(Integer[]::new))),
          Collections.max(Arrays.asList(Arrays.stream(VOLATILITY_PERIODS).boxed().toArray(Integer[]::new)))
        )
      )
    );

    if (data == null || data.size() < maxPeriod) {
      throw new PredictionServiceException(
        String.format("Insufficient data points for feature extraction. Need at least %d data points, but got %d.",
          maxPeriod, data == null ? 0 : data.size())
      );
    }
  }
  private double calculateBollingerBandPosition(List<StockData> data, int currentIndex, int period) {
    double ma = calculateMovingAverage(data, currentIndex, period);
    double stdDev = calculateStandardDeviation(data.subList(currentIndex - period + 1, currentIndex + 1)
      .stream()
      .map(StockData::getClosePrice)
      .collect(Collectors.toList()));

    double upperBand = ma + (2 * stdDev);
    double lowerBand = ma - (2 * stdDev);
    double currentPrice = data.get(currentIndex).getClosePrice();

    return (currentPrice - lowerBand) / (upperBand - lowerBand);
  }

  private double calculateATR(List<StockData> data, int currentIndex, int period) {
    double[] trueRanges = new double[period];

    for (int i = 0; i < period; i++) {
      StockData current = data.get(currentIndex - i);
      StockData previous = data.get(currentIndex - i - 1);

      double tr1 = current.getHighPrice() - current.getLowPrice();
      double tr2 = Math.abs(current.getHighPrice() - previous.getClosePrice());
      double tr3 = Math.abs(current.getLowPrice() - previous.getClosePrice());

      trueRanges[i] = Math.max(Math.max(tr1, tr2), tr3);
    }

    return Arrays.stream(trueRanges).average().orElse(0.0);
  }
  private double calculateConfidence(RandomForestClassifier model, double[] features) {
    return model.predictProbability(features);
  }
  private int[] createLabels(List<StockData> stockData, int predictionTimeframe, int maxLookback) {
    // Calculate the same starting point as features
    int maxPeriod = Collections.max(Arrays.asList(Arrays.stream(MA_PERIODS).boxed().toArray(Integer[]::new)));
    int startIndex = Math.max(maxPeriod, maxLookback);
    int validDataPoints = stockData.size() - startIndex;

    int[] labels = new int[validDataPoints];

    // Create labels for each valid data point
    for (int i = 0; i < validDataPoints; i++) {
      int currentIdx = i + startIndex;
      // Check if we have enough future data for the prediction timeframe
      if (currentIdx + predictionTimeframe < stockData.size()) {
        double currentPrice = stockData.get(currentIdx).getClosePrice();
        double futurePrice = stockData.get(currentIdx + predictionTimeframe).getClosePrice();
        labels[i] = futurePrice > currentPrice ? 1 : 0;
      } else {
        // For the last few points where we can't look ahead
        labels[i] = (i > 0) ? labels[i-1] : 0;  // Use previous trend or default to 0
      }
    }
    // Validate label creation
    if (labels.length == 0) {
      throw new PredictionServiceException("No valid labels created - check data size and prediction timeframe");
    }
    return labels;
  }
  private double calculateMomentum(List<StockData> data, int currentIndex, int period) {
    double currentPrice = data.get(currentIndex).getClosePrice();
    double previousPrice = data.get(currentIndex - period).getClosePrice();
    return (currentPrice - previousPrice) / previousPrice;
  }
  private double calculateVolatility(List<StockData> data, int currentIndex, int period) {
    List<Double> prices = new ArrayList<>();
    for (int i = currentIndex - period + 1; i <= currentIndex; i++) {
      prices.add(data.get(i).getClosePrice());
    }
    return calculateStandardDeviation(prices);
  }

  private double calculatePriceToMA(List<StockData> data, int currentIndex, int period) {
    double ma = calculateMovingAverage(data, currentIndex, period);
    return (data.get(currentIndex).getClosePrice() - ma) / ma;
  }

  private double calculateVolumeMomentum(List<StockData> data, int currentIndex, int period) {
    double currentVolume = data.get(currentIndex).getVolume();
    double previousVolume = data.get(currentIndex - period).getVolume();
    return (currentVolume - previousVolume) / previousVolume;
  }

  private double calculateMovingAverage(List<StockData> data, int currentIndex, int period) {
    // Add validation for null or empty data
    if (data == null || data.isEmpty()) {
      throw new PredictionServiceException("Stock data cannot be null or empty");
    }

    // Validate current index is within bounds
    if (currentIndex < 0 || currentIndex >= data.size()) {
      throw new PredictionServiceException(
        String.format("Invalid currentIndex: %d. Must be between 0 and %d",
          currentIndex, data.size() - 1)
      );
    }

    // Validate we have enough data points for the requested period
    if (currentIndex < period - 1) {
      throw new PredictionServiceException(
        String.format("Insufficient data for %d-day MA calculation. Need %d points, but only have %d points available.",
          period, period, currentIndex + 1)
      );
    }

    // Calculate MA
    double sum = 0;
    for (int i = 0; i < period; i++) {
      sum += data.get(currentIndex - i).getClosePrice();
    }
    return sum / period;
  }
  private double calculateStandardDeviation(List<Double> values) {
    double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    double variance = values.stream()
      .mapToDouble(v -> Math.pow(v - mean, 2))
      .average()
      .orElse(0.0);
    return Math.sqrt(variance);
  }
  private RandomForestClassifier trainModel(double[][] features, int[] labels) {
    // Log the actual data size
    log.debug("Training model with {} data points", features.length);

    // Validate minimum required data
    if (features.length < MIN_TRAINING_DAYS) {
      throw new PredictionServiceException(
        String.format("Insufficient training data. Need at least %d days, but got %d days.",
          MIN_TRAINING_DAYS, features.length)
      );
    }
    // Warn if not using preferred amount of data
    if (features.length < PREFERRED_TRAINING_DAYS) {
      log.warn("Training with less than preferred amount of data. Using {} days instead of {} days. " +
        "Prediction accuracy may be reduced.", features.length, PREFERRED_TRAINING_DAYS);
    }
    // Adjust model parameters based on available data
    RandomForestClassifier model = new RandomForestClassifier();
    // Scale number of trees based on available data
    int numTrees = Math.min(200, Math.max(50, features.length / 2));
    model.setNumTrees(numTrees);
    // Adjust tree depth based on available data
    int maxDepth = Math.min(15, Math.max(5, (int)Math.log(features.length) * 2));
    model.setMaxDepth(maxDepth);
    // Scale feature selection
    int numFeatures = Math.min(12, Math.max(4, features[0].length / 2));
    model.setNumFeatures(numFeatures);
    // Adjust minimum samples requirements
    int minSamplesSplit = Math.min(10, Math.max(4, features.length / 50));
    int minSamplesLeaf = Math.min(5, Math.max(2, features.length / 100));
    model.setMinSamplesSplit(minSamplesSplit);
    model.setMinSamplesLeaf(minSamplesLeaf);
    model.setBootstrapSamples(true);
    log.debug("Model parameters adjusted - trees: {}, depth: {}, features: {}, " +
        "minSplit: {}, minLeaf: {}", numTrees, maxDepth, numFeatures,
      minSamplesSplit, minSamplesLeaf);
    model.train(features, labels);
    return model;
  }
}
