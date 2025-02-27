package com.loadbalancer.service;

import com.loadbalancer.config.LoadBalancerConfig;
import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.model.enums.NodeStatus;
import com.loadbalancer.strategy.LoadBalancerStrategy;
import com.loadbalancer.exception.NoAvailableNodesException;
import com.loadbalancer.exception.StrategyNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoadBalancerService {
    private final Map<String, LoadBalancerStrategy> strategies;
    private final StorageNodeService storageNodeService;
    private final MetricsService metricsService;
    private final LoadBalancerConfig config;
    private final RestTemplate restTemplate;
    private final Map<String, Integer> nodeConnectionCounts = new ConcurrentHashMap<>();

    // Cache to store file-to-node mapping
    private final Map<Long, Long> fileNodeCache = new ConcurrentHashMap<>();

    public StorageNode selectNode(String strategyName, long fileSize) {
        String selectedStrategy = Optional.ofNullable(strategyName)
                .orElse(config.getStrategies().getDefaultStrategy());

        LoadBalancerStrategy strategy = Optional.ofNullable(strategies.get(selectedStrategy))
                .orElseThrow(() -> new StrategyNotFoundException("Invalid strategy: " + selectedStrategy));

        List<StorageNode> availableNodes = storageNodeService.getAvailableNodes();
        if (availableNodes.isEmpty()) {
            throw new NoAvailableNodesException("No storage nodes available");
        }

        StorageNode selectedNode = strategy.selectNode(availableNodes, fileSize);
        log.debug("Selected node {} using strategy {}", selectedNode.getContainerId(), selectedStrategy);

        return selectedNode;
    }

    public StorageNode getNodeForFile(Long fileId) {
        // First check the cache
        Long nodeId = fileNodeCache.get(fileId);
        if (nodeId != null) {
            Optional<StorageNode> cachedNode = storageNodeService.getNode(nodeId);
            if (cachedNode.isPresent() && cachedNode.get().getStatus() == NodeStatus.ACTIVE) {
                return cachedNode.get();
            }
        }

        // If not in cache or node not active, query each storage node
        List<StorageNode> activeNodes = storageNodeService.getAvailableNodes();
        for (StorageNode node : activeNodes) {
            try {
                String checkUrl = String.format("http://%s:%d/api/v1/files/%d/exists",
                        node.getHostAddress(), node.getPort(), fileId);
                Boolean exists = restTemplate.getForObject(checkUrl, Boolean.class);

                if (Boolean.TRUE.equals(exists)) {
                    // Update cache and return node
                    fileNodeCache.put(fileId, node.getContainerId());
                    return node;
                }
            } catch (Exception e) {
                log.warn("Error checking file existence on node {}: {}",
                        node.getContainerId(), e.getMessage());
            }
        }

        throw new NoAvailableNodesException("No node found containing file: " + fileId);
    }

    public void recordRequest(String nodeId, boolean success, long duration) {
        metricsService.recordRequest(nodeId, success, duration);
    }

    public void incrementNodeConnections(String nodeId) {
        nodeConnectionCounts.compute(nodeId, (key, count) -> {
            int newCount = (count == null) ? 1 : count + 1;
            metricsService.recordConnectionCount(nodeId, newCount);
            return newCount;
        });
    }

    public void decrementNodeConnections(String nodeId) {
        nodeConnectionCounts.computeIfPresent(nodeId, (key, count) -> {
            int newCount = Math.max(0, count - 1);
            metricsService.recordConnectionCount(nodeId, newCount);
            return newCount;
        });
    }

    public int getNodeConnections(String nodeId) {
        return nodeConnectionCounts.getOrDefault(nodeId, 0);
    }
}