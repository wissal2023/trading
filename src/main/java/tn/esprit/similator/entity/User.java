package tn.esprit.similator.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true)
  private String username;
  private String password;
  private int bonusPoints;
  @OneToMany(mappedBy = "user")
  private List<BacktestResult> backtestResults;


  @OneToMany(mappedBy = "user")
  private List<UserQuizProgress> quizProgress;

  public User(String username, String password) {
    this.username = username;
    this.password = password;
    this.backtestResults = new ArrayList<>();
  }

}

