package tn.esprit.similator.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PredictionResult {
  private boolean isPriceGoingUp;
  private String direction;
  private double confidence;
}
