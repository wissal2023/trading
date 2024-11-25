package tn.esprit.similator.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import java.util.Map;

@Value
@Getter
@Setter
@AllArgsConstructor
public class OptimizationResult {
  Map<String, Object> parameters;
  double performance;
}
