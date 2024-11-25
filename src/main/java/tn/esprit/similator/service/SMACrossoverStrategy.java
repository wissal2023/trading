package tn.esprit.similator.service;

import tn.esprit.similator.entity.BacktestResult;
import tn.esprit.similator.entity.StockData;
import tn.esprit.similator.entity.Trade;

import java.util.ArrayList;
import java.util.List;

public class SMACrossoverStrategy implements Strategy {
  private final int shortTermDays;
  private final int longTermDays;

  public SMACrossoverStrategy(int shortTermDays, int longTermDays) {
    this.shortTermDays = shortTermDays;
    this.longTermDays = longTermDays;
  }

  @Override
  public BacktestResult execute(List<StockData> stockDataList) {
    List<Double> shortTermSMA = calculateSMA(stockDataList, shortTermDays);
    List<Double> longTermSMA = calculateSMA(stockDataList, longTermDays);

    double initialCapital = 10000.0;
    double cash = initialCapital;
    int shares = 0;
    List<Trade> trades = new ArrayList<>();
    double maxCapital = initialCapital;
    double minCapital = initialCapital;
    int winningTrades = 0;

    for (int i = longTermDays; i < stockDataList.size(); i++) {
      if (shortTermSMA.get(i) > longTermSMA.get(i) && shortTermSMA.get(i - 1) <= longTermSMA.get(i - 1)) {
        // Buy signal
        int sharesToBuy = (int) (cash / stockDataList.get(i).getClosePrice());
        if (sharesToBuy > 0) {
          cash -= sharesToBuy * stockDataList.get(i).getClosePrice();
          shares += sharesToBuy;
          trades.add(new Trade(stockDataList.get(i).getDate(), "BUY", sharesToBuy, stockDataList.get(i).getClosePrice()));
        }
      } else if (shortTermSMA.get(i) < longTermSMA.get(i) && shortTermSMA.get(i - 1) >= longTermSMA.get(i - 1)) {
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

  private List<Double> calculateSMA(List<StockData> stockDataList, int period) {
    List<Double> sma = new ArrayList<>();
    for (int i = 0; i < stockDataList.size(); i++) {
      if (i < period - 1) {
        sma.add(null);
      } else {
        double sum = 0;
        for (int j = i - period + 1; j <= i; j++) {
          sum += stockDataList.get(j).getClosePrice();
        }
        sma.add(sum / period);
      }
    }
    return sma;
  }
}
