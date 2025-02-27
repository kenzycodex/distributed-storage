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

/**
 * Controller for checking the health status of storage nodes.
 */
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Slf4j
public class HealthCheckController {
  private final HealthCheckService healthCheckService;
  private final StorageNodeService storageNodeService;

  // Constants for error responses
  private static final String KEY_ERROR = "error";
  private static final String KEY_MESSAGE = "message";
  private static final String NODE_HEALTH_ERROR = "Error checking node health";
  private static final String FAILED_TO_CHECK_NODE = "Failed to check node health";
  private static final String ALL_NODES_HEALTH_ERROR = "Error checking all nodes health";
  private static final String FAILED_TO_CHECK_NODES = "Failed to check nodes health";
  private static final String FAILED_TO_GET_HEALTH_LOG = "Failed to get health for node {}";

  /**
   * Get health status for a specific node.
   *
   * @param nodeId ID of the node to check
   * @return Health status of the requested node
   */
  @GetMapping("/status/{nodeId}")
  public ResponseEntity<Object> getNodeHealth(@PathVariable Long nodeId) {
    try {
      HealthStatus status = healthCheckService.getNodeHealth(nodeId);
      return ResponseEntity.ok(status);
    } catch (NodeNotFoundException e) {
      return ResponseEntity.notFound().build();
    } catch (Exception e) {
      log.error(NODE_HEALTH_ERROR, e);
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put(KEY_ERROR, FAILED_TO_CHECK_NODE);
      errorResponse.put(KEY_MESSAGE, e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  /**
   * Get health status for all available nodes.
   *
   * @return Map of node IDs to their health status
   */
  @GetMapping("/status")
  public ResponseEntity<Object> getAllNodesHealth() {
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
                          log.warn(FAILED_TO_GET_HEALTH_LOG, node.getContainerId(), e);
                        }
                      });
      return ResponseEntity.ok(healthStatuses);
    } catch (Exception e) {
      log.error(ALL_NODES_HEALTH_ERROR, e);
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put(KEY_ERROR, FAILED_TO_CHECK_NODES);
      errorResponse.put(KEY_MESSAGE, e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }
}