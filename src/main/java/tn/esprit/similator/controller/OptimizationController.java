package tn.esprit.similator.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.similator.entity.*;
import tn.esprit.similator.repository.UserRepo;
import tn.esprit.similator.service.StrategyOptimizer;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/optimization")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class OptimizationController {
  @Autowired
  private UserRepo userRepository;
  @PostMapping("/optimize")
  public ResponseEntity<OptimizationResponse> optimizeStrategy(
    @RequestBody OptimizationRequest request,
    @RequestParam Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    log.info("Received optimization request for user: {} with strategy type: {}", user.getUsername(), request.getStrategyType());
    try {
      BacktestingApplication app = new BacktestingApplication();
      List<StockData> stockDataList = app.fetchStockData(
        request.getSymbol(),
        request.getStartDate(),
        request.getEndDate()
      );
      log.info("Fetched stock data for symbol: {} from {} to {}", request.getSymbol(), request.getStartDate(), request.getEndDate());
      StrategyOptimizer optimizer = new StrategyOptimizer(
        stockDataList,
        request.getStrategyType(),
        request.getPreferences()
      );
      OptimizationResponse response = optimizer.optimize();
      log.info("Optimization completed successfully for user: {}", user.getUsername());
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error optimizing strategy for user: {}: {}", user.getUsername(), e.getMessage());
      throw new ResponseStatusException(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Failed to optimize strategy: " + e.getMessage(),
        e
      );
    }
  }
  @Bean
  public CorsFilter corsFilterOptimization() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("http://localhost:4200");
    config.addAllowedHeader("*");
    config.addAllowedMethod("*");
    source.registerCorsConfiguration("/**", config);
    return new CorsFilter(source);
  }
}


