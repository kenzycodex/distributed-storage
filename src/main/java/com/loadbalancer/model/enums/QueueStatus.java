// model/enums/QueueStatus.java
package com.loadbalancer.model.enums;

public enum QueueStatus {
  PENDING,
  PROCESSING,
  IN_PROGRESS,
  COMPLETED,
  FAILED,
  RETRY,
  CANCELLED
}
