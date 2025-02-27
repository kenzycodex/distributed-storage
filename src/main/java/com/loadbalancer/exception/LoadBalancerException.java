// exception/LoadBalancerException.java
package com.loadbalancer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class LoadBalancerException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public LoadBalancerException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}