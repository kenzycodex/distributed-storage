package com.loadbalancer.service;

import com.loadbalancer.config.LoadBalancerConfig;
import com.loadbalancer.exception.NodeNotFoundException;
import com.loadbalancer.model.dto.HealthStatus;
import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.model.enums.NodeStatus;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service responsible for checking and maintaining the health status of storage nodes.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HealthCheckService {
  private final StorageNodeService storageNodeService;
  private final MetricsService metricsService;
  private final LoadBalancerConfig config;
  private final RestTemplate restTemplate;
  private final Map<Long, Integer> failureCount = new ConcurrentHashMap<>();

  // Constants
  private static final String NODE_NOT_FOUND_MESSAGE = "Node not found with ID: ";
  private static final String HEALTH_CHECK_URL_FORMAT = "http://%s:%d/api/v1/actuator/health";
  private static final String HEALTH_CHECK_FAILURE_LOG = "Health check failed for node {}: {}";
  private static final String NODE_INACTIVE_LOG = "Node {} marked as INACTIVE after {} consecutive failures";
  private static final String NODE_ACTIVE_LOG = "Node {} is healthy, marking as ACTIVE";
  private static final String HEALTH_STATUS_OK = "OK";

  /**
   * Get the health status of a specific node.
   *
   * @param nodeId ID of the node to check
   * @return Health status information for the node
   * @throws NodeNotFoundException if node with given ID doesn't exist
   */
  public HealthStatus getNodeHealth(Long nodeId) {
    StorageNode node =
            storageNodeService
                    .getNode(nodeId)
                    .orElseThrow(() -> new NodeNotFoundException(NODE_NOT_FOUND_MESSAGE + nodeId));
    return performHealthCheck(node);
  }

  /**
   * Scheduled task to check the health of all available nodes.
   */
  @Scheduled(fixedDelayString = "${loadbalancer.health-check.interval:30000}")
  public void checkNodesHealth() {
    storageNodeService.getAvailableNodes().forEach(this::checkNodeHealth);
  }

  /**
   * Check the health of a specific node and handle the result.
   *
   * @param node The node to check
   */
  private void checkNodeHealth(StorageNode node) {
    try {
      HealthStatus status = performHealthCheck(node);
      handleHealthCheckResult(node, status);
    } catch (Exception e) {
      log.warn(HEALTH_CHECK_FAILURE_LOG, node.getContainerId(), e.getMessage());
      // Increment failure count but don't immediately fail the node
      handleHealthCheckFailure(node);
    }
  }

  /**
   * Performs an actual health check by making an HTTP request to the node.
   *
   * @param node The node to check
   * @return Health status information for the node
   */
  private HealthStatus performHealthCheck(StorageNode node) {
    String url = String.format(HEALTH_CHECK_URL_FORMAT, node.getHostAddress(), node.getPort());

    long startTime = System.currentTimeMillis();
    boolean isHealthy = true;
    String statusMessage = HEALTH_STATUS_OK;

    try {
      restTemplate.getForEntity(url, String.class);
      // No cast needed as System.currentTimeMillis() returns a long, and subtraction of longs is a long
      // which can be implicitly converted to double for the recordRequest method
      double duration = System.currentTimeMillis() - startTime;
      metricsService.recordRequest(node.getContainerId().toString(), true, duration);
    } catch (Exception e) {
      isHealthy = false;
      statusMessage = e.getMessage();
      // No cast needed here either
      double duration = System.currentTimeMillis() - startTime;
      metricsService.recordRequest(node.getContainerId().toString(), false, duration);
    }

    // No cast needed here either - the subtraction of two longs results in a long
    // which can be implicitly converted to double
    double responseTime = System.currentTimeMillis() - startTime;

    return HealthStatus.builder()
            .nodeId(node.getContainerId().toString())
            .healthy(isHealthy)
            .lastChecked(LocalDateTime.now())
            .responseTime(responseTime)
            .statusMessage(statusMessage)
            .build();
  }

  /**
   * Handles the result of a health check.
   *
   * @param node The node that was checked
   * @param status The health status result
   */
  private void handleHealthCheckResult(StorageNode node, HealthStatus status) {
    if (status.isHealthy()) {
      resetFailureCount(node);
    } else {
      // Don't immediately fail the node, increment the counter
      handleHealthCheckFailure(node);
    }
  }

  /**
   * Handles a health check failure by incrementing the failure counter
   * and potentially marking the node as inactive.
   *
   * @param node The node that failed the health check
   */
  private void handleHealthCheckFailure(StorageNode node) {
    int failures = failureCount.compute(node.getContainerId(), (k, v) -> v == null ? 1 : v + 1);

    // Only mark as inactive after multiple consecutive failures
    if (failures >= config.getHealthCheck().getFailureThreshold()) {
      log.warn(NODE_INACTIVE_LOG, node.getContainerId(), failures);

      storageNodeService.updateNodeStatus(
              node.getContainerId(), NodeStatus.INACTIVE, node.getUsedSpace());

      metricsService.recordStatusChange(node.getContainerId().toString(), NodeStatus.INACTIVE);
    }
  }

  /**
   * Resets the failure counter for a node and potentially reactivates it.
   *
   * @param node The node that passed a health check
   */
  private void resetFailureCount(StorageNode node) {
    failureCount.remove(node.getContainerId());

    // If node was previously not active, reactivate it
    if (node.getStatus() != NodeStatus.ACTIVE) {
      log.info(NODE_ACTIVE_LOG, node.getContainerId());

      storageNodeService.updateNodeStatus(
              node.getContainerId(), NodeStatus.ACTIVE, node.getUsedSpace());

      metricsService.recordStatusChange(node.getContainerId().toString(), NodeStatus.ACTIVE);
    }
  }
}