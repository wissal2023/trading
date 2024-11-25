package tn.esprit.similator.entity;

import lombok.Value;

@Value
public class OptimizationOutcome {
  ParameterSet parameters;
  OptimizationResult result;
  double score;
  RiskMetrics riskMetrics;
}
