package tn.esprit.similator.entity;

import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Value
public  class RollingMetrics {

  List<LocalDate> dates;
  List<Double> returns;
  List<Double> volatilities;
  List<Double> sharpeRatios;
  List<Double> drawdowns;
  Map<LocalDate, Double> efficiencyRatios;
}
