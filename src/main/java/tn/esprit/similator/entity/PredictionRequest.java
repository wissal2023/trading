package tn.esprit.similator.entity;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {
  @NotNull
  @Size(min = 1, max = 10)
  private String symbol;
  @NotNull
  @PastOrPresent
  private LocalDate startDate;  // Training data start date
  private LocalDate endDate;    // Training data end date
  @NotNull
  @Min(1)
  @Max(365)
  private int predictionTimeframe; // Number of days to predict ahead
  @NotNull
  @Pattern(regexp = "^(DAYS|WEEKS|MONTHS)$")
  private String timeframeUnit;  // "DAYS", "WEEKS", "MONTHS"
}
