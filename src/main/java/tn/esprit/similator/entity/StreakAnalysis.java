package tn.esprit.similator.entity;

import lombok.Value;

import java.util.List;

@Value
public  class StreakAnalysis {
  int maxWinStreak;
  int maxLossStreak;
  double avgWinStreak;
  double avgLossStreak;
  int currentStreak;
  List<Integer> streakDistribution;
}
