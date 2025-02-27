// controller/MetricsController.java
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

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Slf4j
public class MetricsController {
  private final MetricsService metricsService;
  private final StorageNodeService storageNodeService;

  @GetMapping("/stats")
  public ResponseEntity<?> getRequestStats() {
    try {
      RequestStats stats = metricsService.getRequestStats();
      return ResponseEntity.ok(stats);
    } catch (Exception e) {
      log.error("Error getting request stats", e);
      return ResponseEntity.internalServerError()
          .body(Map.of("error", "Failed to get request stats", "message", e.getMessage()));
    }
  }

  @GetMapping("/node/{nodeId}")
  public ResponseEntity<?> getNodeStats(@PathVariable Long nodeId) {
    try {
      if (!storageNodeService.isNodeRegistered(nodeId)) {
        return ResponseEntity.notFound().build();
      }

      RequestStats stats = metricsService.getNodeStats(nodeId.toString());
      return ResponseEntity.ok(stats);
    } catch (Exception e) {
      log.error("Error getting node stats", e);
      return ResponseEntity.internalServerError()
          .body(Map.of("error", "Failed to get node stats", "message", e.getMessage()));
    }
  }

  @GetMapping("/nodes")
  public ResponseEntity<?> getAllNodesStats() {
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
      log.error("Error getting all nodes stats", e);
      return ResponseEntity.internalServerError()
          .body(Map.of("error", "Failed to get nodes stats", "message", e.getMessage()));
    }
  }

  @GetMapping("/summary")
  public ResponseEntity<?> getSystemSummary() {
    try {
      Map<String, Object> summary = new HashMap<>();
      summary.put("globalStats", metricsService.getRequestStats());
      summary.put("activeNodes", storageNodeService.getAvailableNodes().size());
      summary.put("totalRequests", metricsService.getRequestStats().getTotalRequests());
      summary.put("avgResponseTime", metricsService.getRequestStats().getAverageResponseTime());

      return ResponseEntity.ok(summary);
    } catch (Exception e) {
      log.error("Error getting system summary", e);
      return ResponseEntity.internalServerError()
          .body(Map.of("error", "Failed to get system summary", "message", e.getMessage()));
    }
  }
}
