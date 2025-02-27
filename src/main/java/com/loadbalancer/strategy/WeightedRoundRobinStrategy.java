package com.loadbalancer.strategy;

import com.loadbalancer.model.entity.StorageNode;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * Load balancing strategy that uses a weighted round-robin approach based on node capacity.
 * Nodes with more available space receive proportionally more requests.
 */
@Component("weighted-round-robin")
public class WeightedRoundRobinStrategy implements LoadBalancerStrategy {
  private final AtomicInteger counter = new AtomicInteger(0);

  private static final String NO_AVAILABLE_NODES = "No available nodes";
  private static final int MEGABYTE = 1024 * 1024;

  @Override
  public StorageNode selectNode(List<StorageNode> nodes, long fileSize) {
    if (nodes.isEmpty()) {
      throw new IllegalStateException(NO_AVAILABLE_NODES);
    }

    int totalWeight = nodes.stream().mapToInt(node -> calculateWeight(node, fileSize)).sum();

    int position = counter.getAndIncrement() % totalWeight;
    int currentWeight = 0;

    for (StorageNode node : nodes) {
      currentWeight += calculateWeight(node, fileSize);
      if (position < currentWeight) {
        return node;
      }
    }

    return nodes.get(0);
  }

  /**
   * Calculate weight for a node based on its available space.
   *
   * @param node The storage node
   * @param fileSize The size of the file to be stored
   * @return The calculated weight value
   */
  private int calculateWeight(StorageNode node, long fileSize) {
    long availableSpace = node.getCapacity() - node.getUsedSpace();
    if (availableSpace < fileSize) {
      return 0;
    }
    // Convert to MB for weight calculation
    return (int) (availableSpace / MEGABYTE);
  }
}