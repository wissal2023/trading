package tn.esprit.similator.service;

import tn.esprit.similator.entity.BacktestResult;
import tn.esprit.similator.entity.StockData;

import java.util.List;

public interface Strategy {
  BacktestResult execute(List<StockData> stockDataList);
}

