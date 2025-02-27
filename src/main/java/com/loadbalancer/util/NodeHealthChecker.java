package com.loadbalancer.util;

import com.loadbalancer.model.dto.HealthStatus;
import com.loadbalancer.model.entity.StorageNode;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Utility class for checking the health status of storage nodes.
 */
@Component
@Slf4j
public class NodeHealthChecker {
  private final RestTemplate restTemplate;

  // Constants
  private static final String HEALTH_ENDPOINT_URL = "http://%s:%d/health";
  private static final String HEALTH_CHECK_FAILED_LOG = "Health check failed for node {}: {}";

  /**
   * Creates a new NodeHealthChecker with a default RestTemplate.
   */
  public NodeHealthChecker() {
    this.restTemplate = new RestTemplate();
  }

  /**
   * Checks the health of a storage node by making an HTTP request to its health endpoint.
   *
   * @param node The node to check
   * @return HealthStatus containing the result of the health check
   */
  public HealthStatus checkHealth(StorageNode node) {
    try {
      String url = String.format(HEALTH_ENDPOINT_URL, node.getHostAddress(), node.getPort());

      long startTime = System.nanoTime();
      restTemplate.getForEntity(url, String.class);
      long responseTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

      return HealthStatus.builder()
              .nodeId(node.getContainerId().toString())
              .healthy(true)
              .lastChecked(LocalDateTime.now())
              .responseTime(responseTime)
              .build();
    } catch (Exception e) {
      log.warn(HEALTH_CHECK_FAILED_LOG, node.getContainerId(), e.getMessage());

      return HealthStatus.builder()
              .nodeId(node.getContainerId().toString())
              .healthy(false)
              .lastChecked(LocalDateTime.now())
              .statusMessage(e.getMessage())
              .build();
    }
  }
}