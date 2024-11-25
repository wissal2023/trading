package tn.esprit.similator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserQuizProgress {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private User user;

  @ManyToOne
  private Quiz quiz;

  private boolean completed;
  private LocalDateTime completedAt;
  private int pointsEarned;
  public UserQuizProgress(User user, Quiz quiz) {
    this.user = user;
    this.quiz = quiz;
    this.completed = false; // Default not completed
    this.pointsEarned = 0; // Default zero points
    this.completedAt = null; // No completion time initially
  }
}
