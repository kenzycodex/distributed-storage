package com.loadbalancer.exception;

/**
 * Exception thrown when a file download operation fails.
 */
public class FileDownloadException extends RuntimeException {

    public FileDownloadException(String message) {
        super(message);
    }

    public FileDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
}