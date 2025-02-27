// StrategyNotFoundException.java
package com.loadbalancer.exception;

public class StrategyNotFoundException extends RuntimeException {
  public StrategyNotFoundException(String message) {
    super(message);
  }

  public StrategyNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
