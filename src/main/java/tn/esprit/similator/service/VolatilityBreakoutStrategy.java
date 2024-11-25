package tn.esprit.similator.service;

import tn.esprit.similator.entity.BacktestResult;
import tn.esprit.similator.entity.StockData;
import tn.esprit.similator.entity.Trade;

import java.util.ArrayList;
import java.util.List;

public class VolatilityBreakoutStrategy implements Strategy {
  private final int period;
  private final double multiplier;

  public VolatilityBreakoutStrategy(int period, double multiplier) {
    this.period = period;
    this.multiplier = multiplier;
  }

  @Override
  public BacktestResult execute(List<StockData> stockDataList) {
    if (stockDataList == null || stockDataList.size() < period) {
      throw new IllegalArgumentException("Insufficient data for analysis");
    }

    List<Double> upperBand = new ArrayList<>();
    List<Double> lowerBand = new ArrayList<>();
    calculateBollingerBands(stockDataList, upperBand, lowerBand);

    double initialCapital = 10000.0;
    double cash = initialCapital;
    int shares = 0;
    List<Trade> trades = new ArrayList<>();
    double maxCapital = initialCapital;
    double minCapital = initialCapital;
    int winningTrades = 0;

    for (int i = period; i < stockDataList.size(); i++) {
      StockData currentData = stockDataList.get(i);
      if (currentData == null) {
        continue;  // Skip null data points
      }

      double currentPrice = currentData.getClosePrice();
      Double upperBandValue = upperBand.get(i - period);
      Double lowerBandValue = lowerBand.get(i - period);

      if (upperBandValue != null && lowerBandValue != null) {
        if (currentPrice > upperBandValue) {
          // Buy signal
          int sharesToBuy = (int) (cash / currentPrice);
          if (sharesToBuy > 0) {
            cash -= sharesToBuy * currentPrice;
            shares += sharesToBuy;
            trades.add(new Trade(currentData.getDate(), "BUY", sharesToBuy, currentPrice));
          }
        } else if (currentPrice < lowerBandValue) {
          // Sell signal
          if (shares > 0 && !trades.isEmpty()) {
            double saleProceeds = shares * currentPrice;
            cash += saleProceeds;
            Trade lastTrade = trades.get(trades.size() - 1);
            if (saleProceeds > lastTrade.getShares() * lastTrade.getPrice()) {
              winningTrades++;
            }
            trades.add(new Trade(currentData.getDate(), "SELL", shares, currentPrice));
            shares = 0;
          }
        }
      }

      double currentCapital = cash + shares * currentPrice;
      maxCapital = Math.max(maxCapital, currentCapital);
      minCapital = Math.min(minCapital, currentCapital);
    }

    // Ensure there's valid data for final calculations
    if (stockDataList.isEmpty()) {
      throw new IllegalStateException("No valid data for final calculations");
    }

    double finalPrice = stockDataList.get(stockDataList.size() - 1).getClosePrice();
    double finalCapital = cash + shares * finalPrice;
    double totalReturn = (finalCapital - initialCapital) / initialCapital * 100;
    double winRate = trades.isEmpty() ? 0 : (double) winningTrades / (trades.size() / 2) * 100;
    double maxDrawdown = (maxCapital - minCapital) / maxCapital * 100;

    return new BacktestResult(initialCapital, finalCapital, totalReturn, winRate, maxDrawdown, trades.size(), trades);
  }

  private void calculateBollingerBands(List<StockData> stockDataList, List<Double> upperBand, List<Double> lowerBand) {
    // Initialize with nulls for the first 'period' elements
    for (int i = 0; i < period; i++) {
      upperBand.add(null);
      lowerBand.add(null);
    }

    for (int i = period; i < stockDataList.size(); i++) {
      List<Double> window = new ArrayList<>();
      for (int j = i - period; j < i; j++) {
        StockData data = stockDataList.get(j);
        if (data != null) {
          window.add(data.getClosePrice());
        }
      }

      if (window.size() < period) {
        upperBand.add(null);
        lowerBand.add(null);
        continue;  // Skip if we don't have enough valid data points
      }

      double sma = window.stream()
        .mapToDouble(Double::doubleValue)
        .average()
        .orElse(0.0);

      double standardDeviation = Math.sqrt(window.stream()
        .mapToDouble(price -> Math.pow(price - sma, 2))
        .average()
        .orElse(0.0));

      upperBand.add(sma + multiplier * standardDeviation);
      lowerBand.add(sma - multiplier * standardDeviation);
    }
  }
}
