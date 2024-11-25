package tn.esprit.similator.entity;

import lombok.Value;

@Value
public class MarketConditions {
  double volatility;
  double trend;
  double volume;
}
