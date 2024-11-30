package tn.esprit.similator.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

  private int status;
  private String message;
  private String details;
  private LocalDateTime timestamp;
}
