// strategy/ShortestJobNextStrategy.java
package com.loadbalancer.strategy;

import com.loadbalancer.model.entity.StorageNode;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component("shortest-job-next")
public class ShortestJobNextStrategy implements LoadBalancerStrategy {

  @Override
  public StorageNode selectNode(List<StorageNode> nodes, long fileSize) {
    if (nodes.isEmpty()) {
      throw new IllegalStateException("No available nodes");
    }

    return nodes.stream()
        .filter(node -> (node.getCapacity() - node.getUsedSpace()) >= fileSize)
        .min(Comparator.comparingLong(StorageNode::getUsedSpace))
        .orElseThrow(() -> new IllegalStateException("No node with sufficient capacity"));
  }
}
