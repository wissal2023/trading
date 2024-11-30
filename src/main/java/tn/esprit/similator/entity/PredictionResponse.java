package tn.esprit.similator.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {

  private String symbol;
  private LocalDate predictionDate;  // Current date
  private LocalDate targetDate;      // Future date we're predicting for
  private boolean priceGoingUp;
  private String direction;
  private Map<String, Double> technicalIndicators;
  private double confidenceScore;
}
