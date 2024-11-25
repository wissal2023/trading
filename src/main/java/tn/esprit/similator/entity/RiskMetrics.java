package tn.esprit.similator.entity;

import lombok.Value;

@Value
public class RiskMetrics {
  double volatility;          // Annualized volatility
  double valueAtRisk;         // 95% VaR
  double maxDrawdown;         // Maximum drawdown percentage
  double beta;                // Market beta
  double sharpeRatio;         // Risk-adjusted return (vs risk-free rate)
  double sortinoRatio;        // Downside risk-adjusted return
  double cvar;
  double calmarRatio;
}
