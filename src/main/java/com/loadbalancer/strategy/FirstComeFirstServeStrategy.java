// strategy/FirstComeFirstServeStrategy.java
package com.loadbalancer.strategy;

import com.loadbalancer.model.entity.StorageNode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("first-come-first-serve")
public class FirstComeFirstServeStrategy implements LoadBalancerStrategy {

  @Override
  public StorageNode selectNode(List<StorageNode> nodes, long fileSize) {
    if (nodes.isEmpty()) {
      throw new IllegalStateException("No available nodes");
    }

    return nodes.stream()
        .filter(node -> (node.getCapacity() - node.getUsedSpace()) >= fileSize)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No node with sufficient capacity"));
  }
}
