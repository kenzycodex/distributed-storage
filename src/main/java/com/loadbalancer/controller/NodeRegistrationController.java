package com.loadbalancer.controller;

import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.model.enums.NodeStatus;
import com.loadbalancer.service.StorageNodeService;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/nodes")
@RequiredArgsConstructor
@Slf4j
public class NodeRegistrationController {
  private final StorageNodeService storageNodeService;

  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> registerNode(
      @RequestBody Map<String, Object> nodeData) {
    log.info("Received node registration request: {}", nodeData);

    try {
      // Basic validation for required fields
      String[] requiredFields = {"containerId", "containerName", "hostAddress", "port", "capacity"};
      for (String field : requiredFields) {
        if (!nodeData.containsKey(field) || nodeData.get(field) == null) {
          return ResponseEntity.badRequest()
              .body(Map.of("status", "error", "message", "Missing required field: " + field));
        }
      }

      Long containerId = Long.valueOf(nodeData.get("containerId").toString());

      // Check if node already exists and is active
      if (storageNodeService.isNodeRegistered(containerId)) {
        storageNodeService.activateNode(containerId);
        return ResponseEntity.ok(
            Map.of(
                "status", "success",
                "message", "Node already registered and activated",
                "nodeId", containerId));
      }

      StorageNode node =
          StorageNode.builder()
              .containerId(containerId)
              .containerName(nodeData.get("containerName").toString())
              .hostAddress(nodeData.get("hostAddress").toString())
              .port(Integer.valueOf(nodeData.get("port").toString()))
              .capacity(Long.valueOf(nodeData.get("capacity").toString()))
              .status(NodeStatus.ACTIVE)
              .usedSpace(0L)
              .createdAt(LocalDateTime.now())
              .build();

      StorageNode registeredNode = storageNodeService.registerNode(node);

      return ResponseEntity.ok(
          Map.of(
              "status", "success",
              "message", "Node registered successfully",
              "nodeId", registeredNode.getContainerId()));

    } catch (Exception e) {
      log.error("Node registration failed", e);
      return ResponseEntity.badRequest()
          .body(Map.of("status", "error", "message", "Registration failed: " + e.getMessage()));
    }
  }

  @PostMapping("/heartbeat")
  public ResponseEntity<Map<String, Object>> nodeHeartbeat(
      @RequestBody Map<String, Object> heartbeatData) {
    try {
      Long containerId = Long.valueOf(heartbeatData.get("containerId").toString());
      String status = heartbeatData.getOrDefault("status", "ACTIVE").toString();
      Long usedSpace = Long.valueOf(heartbeatData.getOrDefault("usedSpace", 0L).toString());

      storageNodeService.updateNodeStatus(containerId, NodeStatus.valueOf(status), usedSpace);

      return ResponseEntity.ok(
          Map.of(
              "status", "success",
              "message", "Heartbeat processed successfully"));
    } catch (Exception e) {
      log.error("Heartbeat processing failed", e);
      return ResponseEntity.badRequest()
          .body(
              Map.of(
                  "status", "error", "message", "Failed to process heartbeat: " + e.getMessage()));
    }
  }
}
