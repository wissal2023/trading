package tn.esprit.similator.entity;

import java.time.LocalDateTime;
import java.util.List;

public class QuizDTO {

  private Long id;
  private String question;
  private List<String> options;
  private String category;
  private int pointsValue;
  private boolean completed;
  private LocalDateTime completedAt;

  public QuizDTO(Quiz quiz, boolean completed) {
    this.id = quiz.getId();
    this.question = quiz.getQuestion();
    this.options = quiz.getOptions();
    this.category = quiz.getCategory();
    this.pointsValue = quiz.getPointsValue();
    this.completed = completed;
  }

  // Add completion time if the quiz was completed
  public void setCompletionData(UserQuizProgress progress) {
    if (progress != null && progress.isCompleted()) {
      this.completedAt = progress.getCompletedAt();
    }
  }

  // Getters
  public Long getId() {
    return id;
  }

  public String getQuestion() {
    return question;
  }

  public List<String> getOptions() {
    return options;
  }

  public String getCategory() {
    return category;
  }

  public int getPointsValue() {
    return pointsValue;
  }

  public boolean isCompleted() {
    return completed;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }
}
