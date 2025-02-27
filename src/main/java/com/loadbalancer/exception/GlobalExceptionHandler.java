// exception/GlobalExceptionHandler.java
package com.loadbalancer.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(LoadBalancerException.class)
  public ResponseEntity<Map<String, Object>> handleLoadBalancerException(LoadBalancerException ex) {
    log.error("LoadBalancer error: {}", ex.getMessage(), ex);

    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", LocalDateTime.now());
    response.put("status", ex.getStatus().value());
    response.put("error", ex.getStatus().getReasonPhrase());
    response.put("message", ex.getMessage());
    response.put("errorCode", ex.getErrorCode());

    return new ResponseEntity<>(response, ex.getStatus());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);

    Map<String, Object> response = new HashMap<>();
    response.put("timestamp", LocalDateTime.now());
    response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    response.put("error", "Internal Server Error");
    response.put("message", "An unexpected error occurred");

    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
