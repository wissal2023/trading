package tn.esprit.similator.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Quiz {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String question;
  private String correctAnswer;
  private int pointsValue;
  private String category;

  @ElementCollection
  private List<String> options;

  private String explanation;

}
