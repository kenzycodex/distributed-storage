// strategy/LeastConnectionStrategy.java
package com.loadbalancer.strategy;

import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.service.LoadBalancerService;
import java.util.Comparator;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("least-connection")
public class LeastConnectionStrategy implements LoadBalancerStrategy {
  private final LoadBalancerService loadBalancerService;

  @Autowired
  public LeastConnectionStrategy(@Lazy LoadBalancerService loadBalancerService) {
    this.loadBalancerService = loadBalancerService;
  }

  @Override
  public StorageNode selectNode(List<StorageNode> nodes, long fileSize) {
    if (nodes.isEmpty()) {
      throw new IllegalStateException("No available nodes");
    }

    return nodes.stream()
        .min(
            Comparator.comparingInt(
                node -> loadBalancerService.getNodeConnections(node.getContainerId().toString())))
        .orElseThrow(() -> new IllegalStateException("Failed to select node"));
  }
}
