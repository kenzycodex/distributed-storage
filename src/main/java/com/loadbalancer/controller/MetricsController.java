package com.loadbalancer.controller;

import com.loadbalancer.model.dto.RequestStats;
import com.loadbalancer.service.MetricsService;
import com.loadbalancer.service.StorageNodeService;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for retrieving metrics and statistics about the load balancer system.
 */
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricsController {
  private final MetricsService metricsService;
  private final StorageNodeService storageNodeService;

  // Constants for duplicated literals
  private static final String KEY_ERROR = "error";
  private static final String KEY_MESSAGE = "message";
  private static final String KEY_GLOBAL_STATS = "globalStats";
  private static final String KEY_ACTIVE_NODES = "activeNodes";
  private static final String KEY_TOTAL_REQUESTS = "totalRequests";
  private static final String KEY_AVG_RESPONSE_TIME = "avgResponseTime";
  private static final String REQUEST_STATS_ERROR = "Error getting request stats";
  private static final String FAILED_REQUEST_STATS = "Failed to get request stats";
  private static final String NODE_STATS_ERROR = "Error getting node stats";
  private static final String FAILED_NODE_STATS = "Failed to get node stats";
  private static final String ALL_NODES_STATS_ERROR = "Error getting all nodes stats";
  private static final String FAILED_NODES_STATS = "Failed to get nodes stats";
  private static final String SYSTEM_SUMMARY_ERROR = "Error getting system summary";
  private static final String FAILED_SYSTEM_SUMMARY = "Failed to get system summary";

  /**
   * Get global request statistics.
   *
   * @return Response containing request statistics
   */
  @GetMapping("/stats")
  public ResponseEntity<Object> getRequestStats() {
    try {
      RequestStats stats = metricsService.getRequestStats();
      return ResponseEntity.ok(stats);
    } catch (Exception e) {
      log.error(REQUEST_STATS_ERROR, e);
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put(KEY_ERROR, FAILED_REQUEST_STATS);
      errorResponse.put(KEY_MESSAGE, e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  /**
   * Get statistics for a specific node.
   *
   * @param nodeId ID of the node to get statistics for
   * @return Response containing node statistics
   */
  @GetMapping("/node/{nodeId}")
  public ResponseEntity<Object> getNodeStats(@PathVariable Long nodeId) {
    try {
      if (!storageNodeService.isNodeRegistered(nodeId)) {
        return ResponseEntity.notFound().build();
      }

      RequestStats stats = metricsService.getNodeStats(nodeId.toString());
      return ResponseEntity.ok(stats);
    } catch (Exception e) {
      log.error(NODE_STATS_ERROR, e);
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put(KEY_ERROR, FAILED_NODE_STATS);
      errorResponse.put(KEY_MESSAGE, e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  /**
   * Get statistics for all available nodes.
   *
   * @return Response containing statistics for all nodes
   */
  @GetMapping("/nodes")
  public ResponseEntity<Object> getAllNodesStats() {
    try {
      Map<String, RequestStats> allStats = new HashMap<>();
      storageNodeService
              .getAvailableNodes()
              .forEach(
                      node -> {
                        RequestStats stats = metricsService.getNodeStats(node.getContainerId().toString());
                        allStats.put(node.getContainerId().toString(), stats);
                      });
      return ResponseEntity.ok(allStats);
    } catch (Exception e) {
      log.error(ALL_NODES_STATS_ERROR, e);
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put(KEY_ERROR, FAILED_NODES_STATS);
      errorResponse.put(KEY_MESSAGE, e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }

  /**
   * Get a summary of the entire system.
   *
   * @return Response containing system summary
   */
  @GetMapping("/summary")
  public ResponseEntity<Object> getSystemSummary() {
    try {
      Map<String, Object> summary = new HashMap<>();
      summary.put(KEY_GLOBAL_STATS, metricsService.getRequestStats());
      summary.put(KEY_ACTIVE_NODES, storageNodeService.getAvailableNodes().size());
      summary.put(KEY_TOTAL_REQUESTS, metricsService.getRequestStats().getTotalRequests());
      summary.put(KEY_AVG_RESPONSE_TIME, metricsService.getRequestStats().getAverageResponseTime());

      return ResponseEntity.ok(summary);
    } catch (Exception e) {
      log.error(SYSTEM_SUMMARY_ERROR, e);
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put(KEY_ERROR, FAILED_SYSTEM_SUMMARY);
      errorResponse.put(KEY_MESSAGE, e.getMessage());
      return ResponseEntity.internalServerError().body(errorResponse);
    }
  }
}