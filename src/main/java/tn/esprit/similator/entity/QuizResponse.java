package tn.esprit.similator.entity;

public class QuizResponse {
  private boolean correct;
  private String explanation;
  private int pointsEarned;
  private int totalBonusPoints;
  private String feedback;

  public QuizResponse(boolean correct, String explanation, int pointsEarned, int totalBonusPoints) {
    this.correct = correct;
    this.explanation = explanation;
    this.pointsEarned = pointsEarned;
    this.totalBonusPoints = totalBonusPoints;
    this.feedback = generateFeedback(correct, pointsEarned);
  }

  private String generateFeedback(boolean correct, int points) {
    if (correct) {
      return String.format("Correct! You earned %d bonus points.", points);
    } else {
      return "Incorrect. Try again after reviewing the explanation.";
    }
  }

  // Getters
  public boolean isCorrect() {
    return correct;
  }

  public String getExplanation() {
    return explanation;
  }

  public int getPointsEarned() {
    return pointsEarned;
  }

  public int getTotalBonusPoints() {
    return totalBonusPoints;
  }

  public String getFeedback() {
    return feedback;
  }
}
