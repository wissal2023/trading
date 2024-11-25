package tn.esprit.similator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trades")
public class Trade {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private LocalDate date;
  private String action;
  private int shares;
  private double price;
  public Trade(LocalDate date, String action, int shares, double price) {
    this.date = date;
    this.action = action;
    this.shares = shares;
    this.price = price;
  }
}
