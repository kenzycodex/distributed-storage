// controller/HealthCheckController.java
package com.loadbalancer.controller;

import com.loadbalancer.exception.NodeNotFoundException;
import com.loadbalancer.model.dto.HealthStatus;
import com.loadbalancer.service.HealthCheckService;
import com.loadbalancer.service.StorageNodeService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Slf4j
public class HealthCheckController {
  private final HealthCheckService healthCheckService;
  private final StorageNodeService storageNodeService;

  @GetMapping("/status/{nodeId}")
  public ResponseEntity<?> getNodeHealth(@PathVariable Long nodeId) {
    try {
      HealthStatus status = healthCheckService.getNodeHealth(nodeId);
      return ResponseEntity.ok(status);
    } catch (NodeNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error("Error checking node health", e);
      return ResponseEntity.internalServerError()
          .body(Map.of("error", "Failed to check node health", "message", e.getMessage()));
    }
  }

  @GetMapping("/status")
  public ResponseEntity<?> getAllNodesHealth() {
    try {
      Map<String, HealthStatus> healthStatuses = new HashMap<>();
      storageNodeService
          .getAvailableNodes()
          .forEach(
              node -> {
                try {
                  HealthStatus status = healthCheckService.getNodeHealth(node.getContainerId());
                  healthStatuses.put(node.getContainerId().toString(), status);
                } catch (Exception e) {
                  log.warn("Failed to get health for node {}", node.getContainerId(), e);
                }
              });
      return ResponseEntity.ok(healthStatuses);
    } catch (Exception e) {
      log.error("Error checking all nodes health", e);
      return ResponseEntity.internalServerError()
          .body(Map.of("error", "Failed to check nodes health", "message", e.getMessage()));
    }
  }
}
