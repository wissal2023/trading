package tn.esprit.similator.service;

public class StrategyFactory {

  public static Strategy getStrategy(String strategyName) {
    switch (strategyName) {
      case "SMAConservative":
        return new SMACrossoverStrategy(20, 50);
      case "SMAModerate":
        return new SMACrossoverStrategy(10, 30);
      case "SMAAggressive":
        return new SMACrossoverStrategy(5, 20);
      case "RSI":
        return new RSIStrategy(14, 30, 70);
      case "VolatilityBreakout":
        return new VolatilityBreakoutStrategy(20, 2);
      default:
        throw new IllegalArgumentException("Invalid strategy name: " + strategyName);
    }
  }
}
