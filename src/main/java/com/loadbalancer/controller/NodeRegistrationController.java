package com.loadbalancer.controller;

import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.model.enums.NodeStatus;
import com.loadbalancer.service.StorageNodeService;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing storage node registration and heartbeat.
 */
@RestController
@RequestMapping("/api/v1/nodes")
@RequiredArgsConstructor
@Slf4j
public class NodeRegistrationController {
  private final StorageNodeService storageNodeService;

  // Constants for duplicated literals
  private static final String KEY_STATUS = "status";
  private static final String KEY_MESSAGE = "message";
  private static final String KEY_NODE_ID = "nodeId";
  private static final String KEY_CONTAINER_ID = "containerId";
  private static final String KEY_CONTAINER_NAME = "containerName";
  private static final String KEY_HOST_ADDRESS = "hostAddress";
  private static final String KEY_PORT = "port";
  private static final String KEY_CAPACITY = "capacity";
  private static final String KEY_USED_SPACE = "usedSpace";
  private static final String VALUE_SUCCESS = "success";
  private static final String VALUE_ERROR = "error";
  private static final String NODE_ALREADY_REGISTERED = "Node already registered and activated";
  private static final String NODE_REGISTERED_SUCCESSFULLY = "Node registered successfully";
  private static final String MISSING_REQUIRED_FIELD = "Missing required field: ";
  private static final String REGISTRATION_FAILED = "Registration failed: ";
  private static final String HEARTBEAT_SUCCESS = "Heartbeat processed successfully";
  private static final String HEARTBEAT_FAILED = "Failed to process heartbeat: ";
  private static final String REGISTRATION_REQUEST_LOG = "Received node registration request: {}";
  private static final String NODE_REGISTRATION_FAILED_LOG = "Node registration failed";
  private static final String HEARTBEAT_FAILED_LOG = "Heartbeat processing failed";
  private static final String VALUE_ACTIVE = "ACTIVE";

  /**
   * Registers a new storage node or activates an existing one.
   *
   * @param nodeData Map containing node registration information
   * @return Response entity with registration result
   */
  @PostMapping("/register")
  public ResponseEntity<Map<String, Object>> registerNode(
          @RequestBody Map<String, Object> nodeData) {
    log.info(REGISTRATION_REQUEST_LOG, nodeData);

    try {
      // Basic validation for required fields
      String[] requiredFields = {KEY_CONTAINER_ID, KEY_CONTAINER_NAME, KEY_HOST_ADDRESS, KEY_PORT, KEY_CAPACITY};
      for (String field : requiredFields) {
        if (!nodeData.containsKey(field) || nodeData.get(field) == null) {
          Map<String, Object> errorResponse = new HashMap<>();
          errorResponse.put(KEY_STATUS, VALUE_ERROR);
          errorResponse.put(KEY_MESSAGE, MISSING_REQUIRED_FIELD + field);
          return ResponseEntity.badRequest().body(errorResponse);
        }
      }

      Long containerId = Long.valueOf(nodeData.get(KEY_CONTAINER_ID).toString());

      // Check if node already exists and is active
      if (storageNodeService.isNodeRegistered(containerId)) {
        storageNodeService.activateNode(containerId);
        Map<String, Object> response = new HashMap<>();
        response.put(KEY_STATUS, VALUE_SUCCESS);
        response.put(KEY_MESSAGE, NODE_ALREADY_REGISTERED);
        response.put(KEY_NODE_ID, containerId);
        return ResponseEntity.ok(response);
      }

      StorageNode node =
              StorageNode.builder()
                      .containerId(containerId)
                      .containerName(nodeData.get(KEY_CONTAINER_NAME).toString())
                      .hostAddress(nodeData.get(KEY_HOST_ADDRESS).toString())
                      .port(Integer.valueOf(nodeData.get(KEY_PORT).toString()))
                      .capacity(Long.valueOf(nodeData.get(KEY_CAPACITY).toString()))
                      .status(NodeStatus.ACTIVE)
                      .usedSpace(0L)
                      .createdAt(LocalDateTime.now())
                      .build();

      StorageNode registeredNode = storageNodeService.registerNode(node);

      Map<String, Object> response = new HashMap<>();
      response.put(KEY_STATUS, VALUE_SUCCESS);
      response.put(KEY_MESSAGE, NODE_REGISTERED_SUCCESSFULLY);
      response.put(KEY_NODE_ID, registeredNode.getContainerId());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error(NODE_REGISTRATION_FAILED_LOG, e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put(KEY_STATUS, VALUE_ERROR);
      errorResponse.put(KEY_MESSAGE, REGISTRATION_FAILED + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }

  /**
   * Processes heartbeat messages from storage nodes.
   *
   * @param heartbeatData Map containing heartbeat information
   * @return Response entity with heartbeat processing result
   */
  @PostMapping("/heartbeat")
  public ResponseEntity<Map<String, Object>> nodeHeartbeat(
          @RequestBody Map<String, Object> heartbeatData) {
    try {
      Long containerId = Long.valueOf(heartbeatData.get(KEY_CONTAINER_ID).toString());
      String status = heartbeatData.getOrDefault(KEY_STATUS, VALUE_ACTIVE).toString();
      Long usedSpace = Long.valueOf(heartbeatData.getOrDefault(KEY_USED_SPACE, 0L).toString());

      storageNodeService.updateNodeStatus(containerId, NodeStatus.valueOf(status), usedSpace);

      Map<String, Object> response = new HashMap<>();
      response.put(KEY_STATUS, VALUE_SUCCESS);
      response.put(KEY_MESSAGE, HEARTBEAT_SUCCESS);
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error(HEARTBEAT_FAILED_LOG, e);
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put(KEY_STATUS, VALUE_ERROR);
      errorResponse.put(KEY_MESSAGE, HEARTBEAT_FAILED + e.getMessage());
      return ResponseEntity.badRequest().body(errorResponse);
    }
  }
}