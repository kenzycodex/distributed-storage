package com.loadbalancer.strategy;

import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.service.LoadBalancerService;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Load balancing strategy that selects the node with the fewest active connections.
 * This helps distribute load evenly across nodes based on their current workload.
 */
@Component("least-connection")
public class LeastConnectionStrategy implements LoadBalancerStrategy {
  private final LoadBalancerService loadBalancerService;

  private static final String NO_AVAILABLE_NODES = "No available nodes";
  private static final String FAILED_TO_SELECT = "Failed to select node";

  @Autowired
  public LeastConnectionStrategy(@Lazy LoadBalancerService loadBalancerService) {
    this.loadBalancerService = loadBalancerService;
  }

  @Override
  public StorageNode selectNode(List<StorageNode> nodes, long fileSize) {
    if (nodes.isEmpty()) {
      throw new IllegalStateException(NO_AVAILABLE_NODES);
    }

    return nodes.stream()
            .min(
                    Comparator.comparingInt(
                            node -> loadBalancerService.getNodeConnections(node.getContainerId().toString())))
            .orElseThrow(() -> new IllegalStateException(FAILED_TO_SELECT));
  }
}