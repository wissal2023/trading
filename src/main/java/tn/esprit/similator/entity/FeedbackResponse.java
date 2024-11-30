package tn.esprit.similator.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {

  private String overallAssessment;
  private List<String> recommendations;
  private Map<String, String> metricAnalysis;
  private RiskAssessment riskAssessment;
}
