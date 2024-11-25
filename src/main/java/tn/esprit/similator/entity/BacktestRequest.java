package tn.esprit.similator.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "backtest_requests")
public class BacktestRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String symbol;
  private LocalDate startDate;
  private LocalDate endDate;
  private String strategy;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @OneToOne(mappedBy = "backtestRequest", cascade = CascadeType.ALL)
  private BacktestResult backtestResult;
}
