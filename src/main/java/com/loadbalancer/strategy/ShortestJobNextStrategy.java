package com.loadbalancer.strategy;

import com.loadbalancer.model.entity.StorageNode;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Load balancing strategy that selects the node with the least used space.
 * This helps distribute storage more evenly across available nodes.
 */
@Component(value = "shortestJobNext")
public class ShortestJobNextStrategy implements LoadBalancerStrategy {

  private static final String NO_AVAILABLE_NODES = "No available nodes";
  private static final String NO_SUFFICIENT_CAPACITY = "No node with sufficient capacity";

  @Override
  public StorageNode selectNode(List<StorageNode> nodes, long fileSize) {
    if (nodes.isEmpty()) {
      throw new IllegalStateException(NO_AVAILABLE_NODES);
    }

    return nodes.stream()
            .filter(node -> (node.getCapacity() - node.getUsedSpace()) >= fileSize)
            .min(Comparator.comparingLong(StorageNode::getUsedSpace))
            .orElseThrow(() -> new IllegalStateException(NO_SUFFICIENT_CAPACITY));
  }
}