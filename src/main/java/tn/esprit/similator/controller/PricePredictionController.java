package tn.esprit.similator.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tn.esprit.similator.entity.PredictionRequest;
import tn.esprit.similator.entity.PredictionResponse;
import tn.esprit.similator.service.PredictionServiceException;
import tn.esprit.similator.service.PricePredictionService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/predictions")
@Slf4j
public class PricePredictionController {


  @Autowired
  private PricePredictionService pricePredictionService;

  @PostMapping("/direction")
  public ResponseEntity<PredictionResponse> predictPriceDirection(
    @Valid @RequestBody PredictionRequest request) {
    log.info("Received prediction request for symbol: {}", request.getSymbol());
    try {
      validateRequest(request);
      PredictionResponse response = pricePredictionService.predictDirection(request);
      log.info("Successfully generated prediction for symbol: {}", request.getSymbol());
      return ResponseEntity.ok(response);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid request parameters: {}", e.getMessage());
      throw new PredictionServiceException("Invalid request parameters: " + e.getMessage());
    } catch (Exception e) {
      log.error("Error processing prediction request for symbol: {}", request.getSymbol(), e);
      throw new PredictionServiceException("Error processing prediction request", e);
    }
  }

  private void validateRequest(PredictionRequest request) {
    if (request.getStartDate().isAfter(LocalDate.now())) {
      throw new IllegalArgumentException("Start date cannot be in the future");
    }
    if (request.getPredictionTimeframe() <= 0) {
      throw new IllegalArgumentException("Prediction timeframe must be positive");
    }
    // Add more validation as needed
  }
}
