package tn.esprit.similator.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RiskAnalyzer {

  private final List<StockData> stockData;
  private final double annualRiskFreeRate;

  public RiskAnalyzer(List<StockData> stockData, double annualRiskFreeRate) {
    this.stockData = new ArrayList<>(stockData);
    Collections.sort(this.stockData); // Ensure data is sorted by date
    this.annualRiskFreeRate = annualRiskFreeRate;
  }

  public RiskMetrics calculateRiskMetrics() {
    double volatility = calculateVolatility();
    double valueAtRisk = calculateValueAtRisk();
    double maxDrawdown = calculateMaxDrawdown();
    double beta = calculateBeta();
    double sharpeRatio = calculateSharpeRatio();
    double sortinoRatio = calculateSortinoRatio();
    double cvar = calculateCVaR();
    double calmarRatio = calculateCalmarRatio();

    return new RiskMetrics(
      volatility,
      valueAtRisk,
      maxDrawdown,
      beta,
      sharpeRatio,
      sortinoRatio,
      cvar,
      calmarRatio
    );
  }

  // New method to calculate CVaR (Conditional Value at Risk)
  private double calculateCVaR() {
    List<Double> returns = calculateDailyReturns();
    Collections.sort(returns);

    // Calculate 95% CVaR (expected shortfall)
    int varIndex = (int) Math.floor(returns.size() * 0.05);
    double sum = 0;
    int count = 0;

    // Average of losses beyond VaR
    for (int i = 0; i < varIndex; i++) {
      sum += returns.get(i);
      count++;
    }

    return count > 0 ? sum / count : 0.0;
  }

  // New method to calculate Calmar Ratio
  private double calculateCalmarRatio() {
    double maxDrawdown = calculateMaxDrawdown();
    if (maxDrawdown == 0) return 0.0;

    // Calculate annualized return
    double firstPrice = stockData.get(0).getClosePrice();
    double lastPrice = stockData.get(stockData.size() - 1).getClosePrice();
    double totalReturn = (lastPrice - firstPrice) / firstPrice;

    // Convert to annualized return based on number of trading days
    double yearsHeld = stockData.size() / 252.0;
    double annualizedReturn = Math.pow(1 + totalReturn, 1 / yearsHeld) - 1;

    return annualizedReturn / maxDrawdown;
  }

  private double calculateVolatility() {
    List<Double> returns = calculateDailyReturns();
    double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    double variance = returns.stream()
      .mapToDouble(r -> Math.pow(r - mean, 2))
      .average()
      .orElse(0.0);
    return Math.sqrt(variance * 252);
  }

  private double calculateValueAtRisk() {
    List<Double> returns = calculateDailyReturns();
    Collections.sort(returns);

    // Calculate 95% VaR
    int index = (int) Math.floor(returns.size() * 0.05);
    return returns.get(index);
  }

  private double calculateMaxDrawdown() {
    double maxDrawdown = 0.0;
    double peak = stockData.get(0).getClosePrice();

    for (StockData data : stockData) {
      double price = data.getClosePrice();
      if (price > peak) {
        peak = price;
      }
      double drawdown = (peak - price) / peak;
      maxDrawdown = Math.max(maxDrawdown, drawdown);
    }

    return maxDrawdown;
  }

  private double calculateBeta() {
    // Note: This assumes market data is available. In practice, you'd need to inject market data
    List<Double> returns = calculateDailyReturns();
    double marketReturn = 0.08; // Example market return, should be calculated from actual market data
    double marketVariance = 0.04; // Example market variance, should be calculated from actual market data

    double covariance = calculateCovariance(returns, marketReturn);
    return covariance / marketVariance;
  }

  private double calculateSharpeRatio() {
    List<Double> returns = calculateDailyReturns();
    double meanReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    double volatility = Math.sqrt(returns.stream()
      .mapToDouble(r -> Math.pow(r - meanReturn, 2))
      .average()
      .orElse(0.0));

    // Annualize the ratio
    double annualizedReturn = meanReturn * 252;
    double annualizedVolatility = volatility * Math.sqrt(252);

    return (annualizedReturn - annualRiskFreeRate) / annualizedVolatility;
  }

  private double calculateSortinoRatio() {
    List<Double> returns = calculateDailyReturns();
    double meanReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

    // Calculate downside deviation (only negative returns)
    double downsideDeviation = Math.sqrt(returns.stream()
      .filter(r -> r < 0)
      .mapToDouble(r -> Math.pow(r - meanReturn, 2))
      .average()
      .orElse(0.0));

    // Annualize the ratio
    double annualizedReturn = meanReturn * 252;
    double annualizedDownsideDeviation = downsideDeviation * Math.sqrt(252);

    return (annualizedReturn - annualRiskFreeRate) / annualizedDownsideDeviation;
  }

  private List<Double> calculateDailyReturns() {
    List<Double> returns = new ArrayList<>();
    for (int i = 1; i < stockData.size(); i++) {
      double previousClose = stockData.get(i-1).getClosePrice();
      double currentClose = stockData.get(i).getClosePrice();
      double dailyReturn = Math.log(currentClose / previousClose);
      returns.add(dailyReturn);
    }
    return returns;
  }
  private double calculateCovariance(List<Double> returns, double marketReturn) {
    double meanReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    return returns.stream()
      .mapToDouble(r -> (r - meanReturn) * (marketReturn - marketReturn))
      .average()
      .orElse(0.0);
  }
}

