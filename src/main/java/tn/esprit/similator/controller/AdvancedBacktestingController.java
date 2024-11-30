package tn.esprit.similator.controller;
import java.util.*;
import java.time.LocalDate;
import java.util.stream.*;
import java.util.function.Function;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import lombok.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.similator.entity.StockData;
import tn.esprit.similator.service.AdvancedBacktestingService;

@RestController
@RequestMapping("/api/advanced-backtest")
public class AdvancedBacktestingController {
  @Autowired
  private AdvancedBacktestingService backtestingService;


  @Autowired
  private BacktestingApplication backtestingApplication;

  @PostMapping("/compare-strategies")
  public ResponseEntity<AdvancedBacktestingService.MultiStrategyResult> compareStrategies(
    @RequestParam String symbol,
    @RequestParam LocalDate startDate,
    @RequestParam LocalDate endDate,
    @RequestBody List<String> strategies
  ) {
    List<StockData> data = backtestingApplication.fetchStockData(symbol, startDate, endDate);
    AdvancedBacktestingService.MultiStrategyResult result = backtestingService.compareStrategies(data, strategies);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/walk-forward-optimization")
  public ResponseEntity<AdvancedBacktestingService.BacktestMetrics> walkForwardOptimization(
    @RequestParam String symbol,
    @RequestParam LocalDate startDate,
    @RequestParam LocalDate endDate,
    @RequestParam String strategy,
    @RequestParam int windows
  ) {
    List<StockData> data = backtestingApplication.fetchStockData(symbol, startDate, endDate);
    AdvancedBacktestingService.BacktestMetrics metrics = backtestingService.walkForwardOptimization(data, strategy, windows);
    return ResponseEntity.ok(metrics);
  }

  @PostMapping("/monte-carlo-simulation")
  public ResponseEntity<List<AdvancedBacktestingService.BacktestMetrics>> monteCarloSimulation(
    @RequestParam String symbol,
    @RequestParam LocalDate startDate,
    @RequestParam LocalDate endDate,
    @RequestParam String strategy,
    @RequestParam int simulations
  ) {
    List<StockData> data = backtestingApplication.fetchStockData(symbol, startDate, endDate);
    List<AdvancedBacktestingService.BacktestMetrics> simulationResults = backtestingService.monteCarloSimulation(data, strategy, simulations);
    return ResponseEntity.ok(simulationResults);
  }

  @PostMapping("/stress-test")
  public ResponseEntity<AdvancedBacktestingService.BacktestMetrics> stressTest(
    @RequestParam String symbol,
    @RequestParam LocalDate startDate,
    @RequestParam LocalDate endDate,
    @RequestParam String strategy
  ) {
    List<StockData> data = backtestingApplication.fetchStockData(symbol, startDate, endDate);
    AdvancedBacktestingService.BacktestMetrics stressTestResults = backtestingService.stressTest(data, strategy);
    return ResponseEntity.ok(stressTestResults);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleException(Exception e) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body("An error occurred: " + e.getMessage());
  }
}
