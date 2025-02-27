package com.loadbalancer.strategy;

import com.loadbalancer.model.entity.StorageNode;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component("weighted-round-robin")
public class WeightedRoundRobinStrategy implements LoadBalancerStrategy {
  private final AtomicInteger counter = new AtomicInteger(0);

  @Override
  public StorageNode selectNode(List<StorageNode> nodes, long fileSize) {
    if (nodes.isEmpty()) {
      throw new IllegalStateException("No available nodes");
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

  private int calculateWeight(StorageNode node, long fileSize) {
    long availableSpace = node.getCapacity() - node.getUsedSpace();
    if (availableSpace < fileSize) return 0;
    return (int) (availableSpace / (1024 * 1024)); // Convert to MB for weight
  }
}
