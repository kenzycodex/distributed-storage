// util/NodeHealthChecker.java
package com.loadbalancer.util;

import com.loadbalancer.model.dto.HealthStatus;
import com.loadbalancer.model.entity.StorageNode;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class NodeHealthChecker {
  private final RestTemplate restTemplate;

  public NodeHealthChecker() {
    this.restTemplate = new RestTemplate();
  }

  public HealthStatus checkHealth(StorageNode node, long timeout) {
    try {
      String url = String.format("http://%s:%d/health", node.getHostAddress(), node.getPort());

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
      log.warn("Health check failed for node {}: {}", node.getContainerId(), e.getMessage());

      return HealthStatus.builder()
          .nodeId(node.getContainerId().toString())
          .healthy(false)
          .lastChecked(LocalDateTime.now())
          .statusMessage(e.getMessage())
          .build();
    }
  }
}
