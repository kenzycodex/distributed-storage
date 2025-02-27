package com.loadbalancer.strategy;

import com.loadbalancer.model.entity.StorageNode;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/**
 * Load balancing strategy that cycles through available nodes in a round-robin fashion.
 * This ensures equal distribution of requests across all available nodes over time.
 */
@Component("round-robin")
public class RoundRobinStrategy implements LoadBalancerStrategy {
  private final AtomicInteger counter = new AtomicInteger(0);

  private static final String NO_AVAILABLE_NODES = "No available nodes";

  @Override
  public StorageNode selectNode(List<StorageNode> nodes, long fileSize) {
    if (nodes.isEmpty()) {
      throw new IllegalStateException(NO_AVAILABLE_NODES);
    }

    int index = counter.getAndIncrement() % nodes.size();
    return nodes.get(index);
  }
}