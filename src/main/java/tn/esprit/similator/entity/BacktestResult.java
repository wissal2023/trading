package tn.esprit.similator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "backtest_results")
public class BacktestResult {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private double initialCapital;
  private double finalCapital;
  private double totalReturn;
  private double winRate;
  private double maxDrawdown;
  private int totalTrades;


  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @OneToOne
  @JoinColumn(name = "backtest_request_id")
  private BacktestRequest backtestRequest;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @JoinColumn(name = "backtest_result_id")
  private List<Trade> trades;

  public BacktestResult(double initialCapital, double finalCapital, double totalReturn, double winRate, double maxDrawdown, int totalTrades, List<Trade> trades) {
    this.initialCapital = initialCapital;
    this.finalCapital = finalCapital;
    this.totalReturn = totalReturn;
    this.winRate = winRate;
    this.maxDrawdown = maxDrawdown;
    this.totalTrades = totalTrades;
    this.trades = trades;
  }

}
