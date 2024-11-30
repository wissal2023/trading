package tn.esprit.similator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.similator.entity.*;
import tn.esprit.similator.repository.*;
import tn.esprit.similator.service.Strategy;
import tn.esprit.similator.service.StrategyFactory;


import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class BacktestingApplication {
  private static final String ALPHA_VANTAGE_API_KEY = "8EFEBJLEMMIC6PHE";
  private static final String ALPHA_VANTAGE_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&outputsize=full&apikey=" + ALPHA_VANTAGE_API_KEY;

  @Autowired
  private BacktestRequestRepository backtestRequestRepository;

  @Autowired
  private BacktestResultRepository backtestResultRepository;

  @Autowired
  private UserRepo userRepository;


  
  @PostMapping("/backtest")
  public BacktestResult runBacktest(@RequestBody BacktestRequest request, @RequestParam Long userId) {
    try {
      User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

      request.setUser(user);
      request = backtestRequestRepository.save(request);

      List<StockData> stockDataList = fetchStockData(request.getSymbol(), request.getStartDate(), request.getEndDate());
      BacktestResult result = performBacktest(stockDataList, request.getStrategy());

      result.setBacktestRequest(request);
      return backtestResultRepository.save(result);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
  }

  @GetMapping("/user/{userId}/backtests")
  public List<BacktestRequest> getUserBacktests(@PathVariable Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    return backtestRequestRepository.findByUser(user);
  }

  @GetMapping("/backtest/{backtestId}")
  public BacktestResult getBacktestResult(@PathVariable Long backtestId, @RequestParam Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    BacktestResult result = backtestResultRepository.findById(backtestId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Backtest result not found"));

    if (!result.getBacktestRequest().getUser().getId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    return result;
  }

  @DeleteMapping("/backtest/{backtestId}")
  public ResponseEntity<?> deleteBacktest(@PathVariable Long backtestId, @RequestParam Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    BacktestRequest request = backtestRequestRepository.findById(backtestId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Backtest not found"));

    if (!request.getUser().getId().equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    backtestRequestRepository.delete(request);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/strategies")
  public List<String> getAvailableStrategies() {
    return Arrays.asList("SMAConservative", "SMAModerate", "SMAAggressive", "RSI", "VolatilityBreakout");
  }

  // Helper Methods
  public List<StockData> fetchStockData(String symbol, LocalDate startDate, LocalDate endDate) {
    RestTemplate restTemplate = new RestTemplate();
    String url = String.format(ALPHA_VANTAGE_URL, symbol);
    Map<String, Object> response;

    try {
      response = restTemplate.getForObject(url, Map.class);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch data from Alpha Vantage API", e);
    }

    if (response == null || !response.containsKey("Time Series (Daily)")) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid response from Alpha Vantage API");
    }

    Map<String, Map<String, String>> timeSeries = (Map<String, Map<String, String>>) response.get("Time Series (Daily)");
    List<StockData> stockDataList = new ArrayList<>();

    for (Map.Entry<String, Map<String, String>> entry : timeSeries.entrySet()) {
      LocalDate date = LocalDate.parse(entry.getKey());
      if (date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0) {
        Map<String, String> dailyData = entry.getValue();
        stockDataList.add(new StockData(
          date,
          Double.parseDouble(dailyData.get("1. open")),
          Double.parseDouble(dailyData.get("2. high")),
          Double.parseDouble(dailyData.get("3. low")),
          Double.parseDouble(dailyData.get("4. close")),
          Long.parseLong(dailyData.get("5. volume"))
        ));
      }
    }

    if (stockDataList.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No stock data found for the given date range");
    }

    Collections.sort(stockDataList);
    return stockDataList;
  }



  private BacktestResult performBacktest(List<StockData> stockDataList, String strategyName) {
    Strategy strategy = StrategyFactory.getStrategy(strategyName);
    return strategy.execute(stockDataList);
  }

  // Error Handling
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", ex.getReason());
    return new ResponseEntity<>(response, ex.getStatusCode());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "An unexpected error occurred");
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
