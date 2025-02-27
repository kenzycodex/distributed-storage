// NodeMetrics.java
package com.loadbalancer.model.dto;

import com.loadbalancer.model.enums.NodeStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;

@Getter
@Setter
@Builder
public class NodeMetrics {
    @Builder.Default
    private long totalRequests = 0;
    @Builder.Default
    private long successfulRequests = 0;
    @Builder.Default
    private long failedRequests = 0;
    @Builder.Default
    private Queue<Double> responseTimes = new LinkedList<>();
    @Builder.Default
    private LocalDateTime lastUpdated = LocalDateTime.now();
    private NodeStatus status;
    private LocalDateTime lastStatusChange;

    public void recordRequest(boolean success, double responseTime) {
        totalRequests++;
        if (success) {
            successfulRequests++;
        } else {
            failedRequests++;
        }
        responseTimes.offer(responseTime);
        lastUpdated = LocalDateTime.now();
    }

    public RequestStats getStats() {
        return RequestStats.builder()
                .totalRequests(totalRequests)
                .successfulRequests(successfulRequests)
                .failedRequests(failedRequests)
                .averageResponseTime(calculateAverageResponseTime())
                .p95ResponseTime(calculatePercentileResponseTime(95))
                .p99ResponseTime(calculatePercentileResponseTime(99))
                .status(status)
                .lastStatusChange(lastStatusChange)
                .build();
    }

    private double calculateAverageResponseTime() {
        return responseTimes.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private double calculatePercentileResponseTime(int percentile) {
        double[] sorted = responseTimes.stream()
                .mapToDouble(Double::doubleValue)
                .sorted()
                .toArray();

        if (sorted.length == 0) return 0.0;
        int index = (int) Math.ceil((percentile / 100.0) * sorted.length) - 1;
        return sorted[Math.min(index, sorted.length - 1)];
    }

    public void cleanup(LocalDateTime cutoff) {
        if (lastUpdated.isBefore(cutoff)) {
            responseTimes.clear();
            totalRequests = 0;
            successfulRequests = 0;
            failedRequests = 0;
        }

        while (responseTimes.size() > 1000) {
            responseTimes.poll();
        }
    }
}