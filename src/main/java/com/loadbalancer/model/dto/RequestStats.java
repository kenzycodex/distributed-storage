// model/dto/RequestStats.java
package com.loadbalancer.model.dto;

import com.loadbalancer.model.enums.NodeStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestStats {
  private long totalRequests;
  private long successfulRequests;
  private long failedRequests;
  private double averageResponseTime;
  private double p95ResponseTime;
  private double p99ResponseTime;
  private NodeStatus status;
  private LocalDateTime lastStatusChange;
}
