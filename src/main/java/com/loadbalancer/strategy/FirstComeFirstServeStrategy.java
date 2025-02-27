package com.loadbalancer.strategy;

import com.loadbalancer.model.entity.StorageNode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Load balancing strategy that selects the first available node with sufficient capacity.
 * Nodes are processed in the order they appear in the list.
 */
@Component("first-come-first-serve")
public class FirstComeFirstServeStrategy implements LoadBalancerStrategy {

  private static final String NO_AVAILABLE_NODES = "No available nodes";
  private static final String NO_SUFFICIENT_CAPACITY = "No node with sufficient capacity";

  @Override
  public StorageNode selectNode(List<StorageNode> nodes, long fileSize) {
    if (nodes.isEmpty()) {
      throw new IllegalStateException(NO_AVAILABLE_NODES);
    }

    return nodes.stream()
            .filter(node -> (node.getCapacity() - node.getUsedSpace()) >= fileSize)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(NO_SUFFICIENT_CAPACITY));
  }
}