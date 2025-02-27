// service/MetricsService.java
package com.loadbalancer.service;

import com.loadbalancer.config.LoadBalancerConfig;
import com.loadbalancer.model.dto.RequestStats;
import com.loadbalancer.model.dto.NodeMetrics;
import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.model.enums.NodeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class MetricsService {
    private final LoadBalancerConfig config;
    private final Map<String, NodeMetrics> nodeMetrics = new ConcurrentHashMap<>();
    private final Map<String, Integer> nodeConnections = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Double> responseTimes = new ConcurrentLinkedQueue<>();
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);

    public void recordRequest(String nodeId, boolean success, double responseTime) {
        totalRequests.incrementAndGet();
        if (success) {
            successfulRequests.incrementAndGet();
        } else {
            failedRequests.incrementAndGet();
        }

        responseTimes.offer(responseTime);
        updateNodeMetrics(nodeId, success, responseTime);
        log.debug("Recorded request for node {}: success={}, responseTime={}", nodeId, success, responseTime);
    }

    public void recordConnectionCount(String nodeId, int connections) {
        nodeConnections.put(nodeId, connections);
        log.debug("Updated connection count for node {}: {}", nodeId, connections);
    }

    public void recordStatusChange(String nodeId, NodeStatus status) {
        nodeMetrics.compute(nodeId, (key, metrics) -> {
            if (metrics == null) {
                metrics = NodeMetrics.builder().build();
            }
            metrics.setStatus(status);
            metrics.setLastStatusChange(LocalDateTime.now());
            return metrics;
        });
        log.info("Recorded status change for node {}: {}", nodeId, status);
    }

    private void updateNodeMetrics(String nodeId, boolean success, double responseTime) {
        nodeMetrics.compute(nodeId, (key, metrics) -> {
            if (metrics == null) {
                metrics = NodeMetrics.builder().build();
            }
            metrics.recordRequest(success, responseTime);
            return metrics;
        });
    }

    public RequestStats getRequestStats() {
        return RequestStats.builder()
                .totalRequests(totalRequests.get())
                .successfulRequests(successfulRequests.get())
                .failedRequests(failedRequests.get())
                .averageResponseTime(calculateAverageResponseTime())
                .p95ResponseTime(calculatePercentileResponseTime(95))
                .p99ResponseTime(calculatePercentileResponseTime(99))
                .build();
    }

    public RequestStats getNodeStats(String nodeId) {
        NodeMetrics metrics = nodeMetrics.get(nodeId);
        if (metrics == null) {
            return RequestStats.builder().build();
        }
        return metrics.getStats();
    }

    public int getNodeConnections(String nodeId) {
        return nodeConnections.getOrDefault(nodeId, 0);
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

    @Scheduled(fixedDelayString = "${loadbalancer.metrics.collection-interval}")
    public void cleanupOldMetrics() {
        LocalDateTime cutoff = LocalDateTime.now()
                .minusDays(config.getMetrics().getRetentionDays());

        nodeMetrics.values().forEach(metrics -> metrics.cleanup(cutoff));

        while (responseTimes.size() > config.getMetrics().getMaxResponseTimeEntries()) {
            responseTimes.poll();
        }

        log.debug("Cleaned up old metrics. Current response times size: {}", responseTimes.size());
    }

    public void recordNodeRegistration(StorageNode node) {
        // If you want to add specific logic for node registration
        log.info("Node registered: ID={}, Name={}", node.getContainerId(), node.getContainerName());

        // Record initial status
        recordStatusChange(node.getContainerId().toString(), node.getStatus());
    }
}
