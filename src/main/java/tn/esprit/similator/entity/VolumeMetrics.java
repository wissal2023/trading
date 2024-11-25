package tn.esprit.similator.entity;

import lombok.Value;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Value
public  class VolumeMetrics {
  double averageVolume;
  double volumeVolatility;
  double volumeTrend;
  List<LocalDate> volumeSpikeDates;
  double volumeMomentum;
  double priceVolumeCorrelation;
  Map<String, Double> volumeDistribution;
}
