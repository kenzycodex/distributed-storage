package com.storagenode.service;

import com.storagenode.config.StorageConfig;
import com.storagenode.model.NodeHeartbeat;
import com.storagenode.model.NodeRegistration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NodeRegistrationService {
    private final StorageConfig storageConfig;
    private final RestTemplate restTemplate;
    private final FileStorageService fileStorageService;
    private Long nodeId;

    @EventListener(ApplicationReadyEvent.class)
    public void registerWithLoadBalancer() {
        try {
            NodeRegistration registration = NodeRegistration.builder()
                    .containerName(storageConfig.getNode().getName())
                    .hostAddress(storageConfig.getNode().getHost())
                    .port(storageConfig.getNode().getPort())
                    .capacity(storageConfig.getNode().getCapacity())
                    .build();

            String registrationUrl = storageConfig.getLoadbalancer().getRegistrationUrl();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    registrationUrl, registration, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                this.nodeId = Long.valueOf(response.get("nodeId").toString());
                log.info("Successfully registered with load balancer. Node ID: {}", nodeId);
            } else {
                log.error("Failed to register with load balancer: {}", response);
            }
        } catch (Exception e) {
            log.error("Error registering with load balancer", e);
        }
    }

    @Scheduled(fixedDelayString = "${storage.heartbeat.interval:30000}")
    public void sendHeartbeat() {
        if (nodeId == null) {
            log.warn("Node not registered, skipping heartbeat");
            return;
        }

        try {
            NodeHeartbeat heartbeat = NodeHeartbeat.builder()
                    .containerId(nodeId)
                    .status("ACTIVE")
                    .usedSpace(fileStorageService.getUsedSpace())
                    .build();

            String heartbeatUrl = storageConfig.getLoadbalancer().getHeartbeatUrl();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    heartbeatUrl, heartbeat, Map.class);

            if (response != null && "success".equals(response.get("status"))) {
                log.debug("Heartbeat sent successfully");
            } else {
                log.warn("Heartbeat response: {}", response);
            }
        } catch (Exception e) {
            log.error("Error sending heartbeat", e);
        }
    }

    public Long getNodeId() {
        return nodeId;
    }
}