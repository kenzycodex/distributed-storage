package com.loadbalancer.service;

import com.loadbalancer.config.LoadBalancerConfig;
import com.loadbalancer.exception.NodeNotFoundException;
import com.loadbalancer.model.dto.HealthStatus;
import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.model.enums.NodeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class HealthCheckService {
    private final StorageNodeService storageNodeService;
    private final MetricsService metricsService;
    private final LoadBalancerConfig config;
    private final RestTemplate restTemplate;
    private final Map<Long, Integer> failureCount = new ConcurrentHashMap<>();

    public HealthStatus getNodeHealth(Long nodeId) {
        StorageNode node = storageNodeService.getNode(nodeId)
                .orElseThrow(() -> new NodeNotFoundException("Node not found with ID: " + nodeId));
        return performHealthCheck(node);
    }

    @Scheduled(fixedDelayString = "${loadbalancer.health-check.interval:30000}")
    public void checkNodesHealth() {
        storageNodeService.getAvailableNodes().forEach(this::checkNodeHealth);
    }

    private void checkNodeHealth(StorageNode node) {
        try {
            HealthStatus status = performHealthCheck(node);
            handleHealthCheckResult(node, status);
        } catch (Exception e) {
            log.warn("Health check failed for node {}: {}", node.getContainerId(), e.getMessage());
            // Increment failure count but don't immediately fail the node
            handleHealthCheckFailure(node);
        }
    }

    private HealthStatus performHealthCheck(StorageNode node) {
        String url = String.format("http://%s:%d/api/v1/actuator/health",
                node.getHostAddress(), node.getPort());

        long startTime = System.currentTimeMillis();
        boolean isHealthy = true;
        String statusMessage = "OK";

        try {
            restTemplate.getForEntity(url, String.class);
            metricsService.recordRequest(node.getContainerId().toString(), true,
                    System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            isHealthy = false;
            statusMessage = e.getMessage();
            metricsService.recordRequest(node.getContainerId().toString(), false,
                    System.currentTimeMillis() - startTime);
        }

        long responseTime = System.currentTimeMillis() - startTime;

        return HealthStatus.builder()
                .nodeId(node.getContainerId().toString())
                .healthy(isHealthy)
                .lastChecked(LocalDateTime.now())
                .responseTime(responseTime)
                .statusMessage(statusMessage)
                .build();
    }

    private void handleHealthCheckResult(StorageNode node, HealthStatus status) {
        if (status.isHealthy()) {
            resetFailureCount(node);
        } else {
            // Don't immediately fail the node, increment the counter
            handleHealthCheckFailure(node);
        }
    }

    private void handleHealthCheckFailure(StorageNode node) {
        int failures = failureCount.compute(node.getContainerId(),
                (k, v) -> v == null ? 1 : v + 1);

        // Only mark as inactive after multiple consecutive failures
        if (failures >= config.getHealthCheck().getFailureThreshold()) {
            log.warn("Node {} marked as INACTIVE after {} consecutive failures",
                    node.getContainerId(), failures);

            storageNodeService.updateNodeStatus(
                    node.getContainerId(),
                    NodeStatus.INACTIVE,
                    node.getUsedSpace()
            );

            metricsService.recordStatusChange(
                    node.getContainerId().toString(),
                    NodeStatus.INACTIVE
            );
        }
    }

    private void resetFailureCount(StorageNode node) {
        failureCount.remove(node.getContainerId());

        // If node was previously not active, reactivate it
        if (node.getStatus() != NodeStatus.ACTIVE) {
            log.info("Node {} is healthy, marking as ACTIVE", node.getContainerId());

            storageNodeService.updateNodeStatus(
                    node.getContainerId(),
                    NodeStatus.ACTIVE,
                    node.getUsedSpace()
            );

            metricsService.recordStatusChange(
                    node.getContainerId().toString(),
                    NodeStatus.ACTIVE
            );
        }
    }
}