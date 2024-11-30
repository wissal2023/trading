package tn.esprit.similator.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class OptimizationPreferences {

  private double volatilityThreshold = 0.3;
  private double maxDrawdownThreshold = 0.2;
  private double minSharpeRatio = 1.0;
  private double riskFreeRate = 0.02;
  private boolean adaptToMarketConditions = true;
  private Map<String, Double> metricWeights;

  public OptimizationPreferences() {
    this.metricWeights = new HashMap<>();
    this.metricWeights.put("return", 0.3);
    this.metricWeights.put("sharpe", 0.2);
    this.metricWeights.put("maxDrawdown", 0.2);
    this.metricWeights.put("volatility", 0.15);
    this.metricWeights.put("sortino", 0.15);
  }
}
