package tn.esprit.similator.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tn.esprit.similator.entity.ErrorResponse;
import tn.esprit.similator.service.PredictionServiceException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(PredictionServiceException.class)
  public ResponseEntity<ErrorResponse> handlePredictionServiceException(
    PredictionServiceException ex, WebRequest request) {
    log.error("Prediction service error: ", ex);
    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.INTERNAL_SERVER_ERROR.value(),
      ex.getMessage(),
      request.getDescription(false),
      LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAllUncaughtException(
    Exception ex, WebRequest request) {
    log.error("Unexpected error occurred: ", ex);
    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.INTERNAL_SERVER_ERROR.value(),
      "An unexpected error occurred. Please try again later.",
      request.getDescription(false),
      LocalDateTime.now()
    );
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
    MethodArgumentNotValidException ex,
    HttpHeaders headers,
    HttpStatusCode status,
    WebRequest request) {
    List<String> errors = ex.getBindingResult()
      .getFieldErrors()
      .stream()
      .map(error -> error.getField() + ": " + error.getDefaultMessage())
      .collect(Collectors.toList());

    ErrorResponse errorResponse = new ErrorResponse(
      HttpStatus.BAD_REQUEST.value(),
      "Validation failed",
      errors.toString(),
      LocalDateTime.now()
    );

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }
}
