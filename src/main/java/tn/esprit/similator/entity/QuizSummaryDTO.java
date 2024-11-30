package tn.esprit.similator.entity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuizSummaryDTO {

  private int totalQuizzesTaken;
  private int totalPointsEarned;
  private int correctAnswers;
  private int incorrectAnswers;
  private double accuracy;
  private Map<String, Integer> pointsByCategory;

  public QuizSummaryDTO(List<UserQuizProgress> progress) {
    this.totalQuizzesTaken = progress.size();
    this.totalPointsEarned = progress.stream()
      .mapToInt(UserQuizProgress::getPointsEarned)
      .sum();
    this.correctAnswers = (int) progress.stream()
      .filter(UserQuizProgress::isCompleted)
      .count();
    this.incorrectAnswers = totalQuizzesTaken - correctAnswers;
    this.accuracy = totalQuizzesTaken > 0
      ? (double) correctAnswers / totalQuizzesTaken * 100
      : 0.0;
    this.pointsByCategory = calculatePointsByCategory(progress);
  }

  private Map<String, Integer> calculatePointsByCategory(List<UserQuizProgress> progress) {
    return progress.stream()
      .filter(UserQuizProgress::isCompleted)
      .collect(Collectors.groupingBy(
        p -> p.getQuiz().getCategory(),
        Collectors.summingInt(UserQuizProgress::getPointsEarned)
      ));
  }

  // Getters
  public int getTotalQuizzesTaken() {
    return totalQuizzesTaken;
  }

  public int getTotalPointsEarned() {
    return totalPointsEarned;
  }

  public int getCorrectAnswers() {
    return correctAnswers;
  }

  public int getIncorrectAnswers() {
    return incorrectAnswers;
  }

  public double getAccuracy() {
    return accuracy;
  }

  public Map<String, Integer> getPointsByCategory() {
    return pointsByCategory;
  }
}
