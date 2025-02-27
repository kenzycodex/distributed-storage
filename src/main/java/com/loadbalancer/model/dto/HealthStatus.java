// model/dto/HealthStatus.java
package com.loadbalancer.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HealthStatus {
    private String nodeId;
    private boolean healthy;
    private double cpuUsage;
    private double memoryUsage;
    private int activeConnections;
    private LocalDateTime lastChecked;
    private String statusMessage;
    private long responseTime;
}