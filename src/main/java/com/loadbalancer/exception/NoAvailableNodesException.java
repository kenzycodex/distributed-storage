// NoAvailableNodesException.java
package com.loadbalancer.exception;

public class NoAvailableNodesException extends RuntimeException {
  public NoAvailableNodesException(String message) {
    super(message);
  }

  public NoAvailableNodesException(String message, Throwable cause) {
    super(message, cause);
  }
}
