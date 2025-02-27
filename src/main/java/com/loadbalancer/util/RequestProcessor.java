package com.loadbalancer.util;

import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.service.LoadBalancerService;
import com.loadbalancer.service.MetricsService;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Utility for processing storage node requests asynchronously.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RequestProcessor {
    private final LoadBalancerService loadBalancerService;
    private final MetricsService metricsService;

    // Constants
    private static final String ERROR_PROCESSING_REQUEST = "Error processing request {} on node {}: {}";
    private static final double MILLISECONDS_TO_SECONDS = 1000.0;
    private static final boolean DEFAULT_REQUEST_SUCCESS = true;

    /**
     * Processes a request asynchronously.
     *
     * @param node The storage node to process the request
     * @param requestId The ID of the request
     * @return A CompletableFuture that completes when the request is processed
     */
    public CompletableFuture<Void> processRequest(StorageNode node, String requestId) {
        return CompletableFuture.runAsync(
                () -> {
                    String nodeId = node.getContainerId().toString();
                    Instant start = Instant.now();
                    boolean success = false;

                    try {
                        // Simulate request processing
                        success = executeRequest();
                    } catch (Exception e) {
                        log.error(ERROR_PROCESSING_REQUEST, requestId, nodeId, e.getMessage(), e);
                    } finally {
                        double duration = calculateDuration(start);
                        metricsService.recordRequest(nodeId, success, duration);
                        loadBalancerService.decrementNodeConnections(nodeId);
                    }
                });
    }

    /**
     * Executes the actual request.
     *
     * @return Whether the request was successful
     */
    private boolean executeRequest() {
        // Implement actual request execution logic here
        return DEFAULT_REQUEST_SUCCESS;
    }

    /**
     * Calculates the duration of a request in seconds.
     *
     * @param start The start time of the request
     * @return The duration in seconds
     */
    private double calculateDuration(Instant start) {
        return (Instant.now().toEpochMilli() - start.toEpochMilli()) / MILLISECONDS_TO_SECONDS;
    }
}