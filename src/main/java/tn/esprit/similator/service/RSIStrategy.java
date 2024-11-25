package tn.esprit.similator.service;

import tn.esprit.similator.entity.BacktestResult;
import tn.esprit.similator.entity.StockData;
import tn.esprit.similator.entity.Trade;

import java.util.ArrayList;
import java.util.List;

class RSIStrategy implements Strategy {
  private final int period;
  private final double oversoldThreshold;
  private final double overboughtThreshold;

  public RSIStrategy(int period, double oversoldThreshold, double overboughtThreshold) {
    this.period = period;
    this.oversoldThreshold = oversoldThreshold;
    this.overboughtThreshold = overboughtThreshold;
  }

  @Override
  public BacktestResult execute(List<StockData> stockDataList) {
    List<Double> rsi = calculateRSI(stockDataList);

    double initialCapital = 10000.0;
    double cash = initialCapital;
    int shares = 0;
    List<Trade> trades = new ArrayList<>();
    double maxCapital = initialCapital;
    double minCapital = initialCapital;
    int winningTrades = 0;

    for (int i = period; i < stockDataList.size(); i++) {
      if (rsi.get(i) < oversoldThreshold) {
        // Buy signal
        int sharesToBuy = (int) (cash / stockDataList.get(i).getClosePrice());
        if (sharesToBuy > 0) {
          cash -= sharesToBuy * stockDataList.get(i).getClosePrice();
          shares += sharesToBuy;
          trades.add(new Trade(stockDataList.get(i).getDate(), "BUY", sharesToBuy, stockDataList.get(i).getClosePrice()));
        }
      } else if (rsi.get(i) > overboughtThreshold) {
        // Sell signal
        if (shares > 0) {
          double saleProceeds = shares * stockDataList.get(i).getClosePrice();
          cash += saleProceeds;
          if (saleProceeds > trades.get(trades.size() - 1).getShares() * trades.get(trades.size() - 1).getPrice()) {
            winningTrades++;
          }
          trades.add(new Trade(stockDataList.get(i).getDate(), "SELL", shares, stockDataList.get(i).getClosePrice()));
          shares = 0;
        }
      }

      double currentCapital = cash + shares * stockDataList.get(i).getClosePrice();
      maxCapital = Math.max(maxCapital, currentCapital);
      minCapital = Math.min(minCapital, currentCapital);
    }

    double finalCapital = cash + shares * stockDataList.get(stockDataList.size() - 1).getClosePrice();
    double totalReturn = (finalCapital - initialCapital) / initialCapital * 100;
    double winRate = (double) winningTrades / (trades.size() / 2) * 100;
    double maxDrawdown = (maxCapital - minCapital) / maxCapital * 100;

    return new BacktestResult(initialCapital, finalCapital, totalReturn, winRate, maxDrawdown, trades.size(), trades);
  }

  private List<Double> calculateRSI(List<StockData> stockDataList) {
    List<Double> rsi = new ArrayList<>();
    List<Double> gains = new ArrayList<>();
    List<Double> losses = new ArrayList<>();

    for (int i = 1; i < stockDataList.size(); i++) {
      double change = stockDataList.get(i).getClosePrice() - stockDataList.get(i - 1).getClosePrice();
      gains.add(Math.max(change, 0));
      losses.add(Math.max(-change, 0));
    }

    double avgGain = gains.subList(0, period).stream().mapToDouble(Double::doubleValue).average().orElse(0);
    double avgLoss = losses.subList(0, period).stream().mapToDouble(Double::doubleValue).average().orElse(0);

    for (int i = 0; i < period; i++) {
      rsi.add(null);
    }

    for (int i = period; i < stockDataList.size(); i++) {
      avgGain = (avgGain * (period - 1) + gains.get(i - 1)) / period;
      avgLoss = (avgLoss * (period - 1) + losses.get(i - 1)) / period;

      double rs = avgGain / avgLoss;
      double rsiValue = 100 - (100 / (1 + rs));
      rsi.add(rsiValue);
    }

    return rsi;
  }
}
