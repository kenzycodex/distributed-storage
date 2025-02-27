// model/enums/SyncStatus.java
package com.loadbalancer.model.enums;

public enum SyncStatus {
    PENDING,
    PROCESSING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    RETRY,
    CANCELLED
}