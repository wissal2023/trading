package tn.esprit.similator.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockData implements Comparable<StockData> {
  private LocalDate date;
  private double openPrice;
  private double highPrice;
  private double lowPrice;
  private double closePrice;
  private long volume;
  @Override
  public int compareTo(StockData other) {
    return this.date.compareTo(other.date);
  }
}

