// util/RequestProcessor.java
package com.loadbalancer.util;

import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.service.LoadBalancerService;
import com.loadbalancer.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestProcessor {
    private final LoadBalancerService loadBalancerService;
    private final MetricsService metricsService;

    public CompletableFuture<Void> processRequest(StorageNode node, String requestId, long fileSize) {
        return CompletableFuture.runAsync(() -> {
            String nodeId = node.getContainerId().toString();
            Instant start = Instant.now();
            boolean success = false;

            try {
                // Simulate request processing
                success = executeRequest(node, requestId, fileSize);
            } catch (Exception e) {
                log.error("Error processing request {} on node {}: {}",
                        requestId, nodeId, e.getMessage(), e);
            } finally {
                double duration = calculateDuration(start);
                metricsService.recordRequest(nodeId, success, duration);
                loadBalancerService.decrementNodeConnections(nodeId);
            }
        });
    }

    private boolean executeRequest(StorageNode node, String requestId, long fileSize) {
        // Implement actual request execution logic here
        return true;
    }

    private double calculateDuration(Instant start) {
        return (Instant.now().toEpochMilli() - start.toEpochMilli()) / 1000.0;
    }
}