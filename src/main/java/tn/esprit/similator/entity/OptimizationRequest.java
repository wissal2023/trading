package tn.esprit.similator.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OptimizationRequest {
  private String symbol;
  private LocalDate startDate;
  private LocalDate endDate;
  private String strategyType;
  private OptimizationPreferences preferences;


}
