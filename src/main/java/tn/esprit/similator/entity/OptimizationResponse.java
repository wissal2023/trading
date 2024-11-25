package tn.esprit.similator.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.util.Map;

@Value
@Data
@AllArgsConstructor
public class OptimizationResponse {
  Map<String, Object> parameters;
  Map<String, Double> performanceMetrics;
  Map<String, Double> marketConditions;
  OptimizationResult optimizationResult;
  FeedbackResponse feedback;
  RollingMetrics rollingMetrics;
  StreakAnalysis streakAnalysis;
}

