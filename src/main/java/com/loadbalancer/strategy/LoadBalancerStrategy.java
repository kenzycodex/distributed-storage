// strategy/LoadBalancerStrategy.java
package com.loadbalancer.strategy;

import com.loadbalancer.model.entity.StorageNode;
import java.util.List;

public interface LoadBalancerStrategy {
  StorageNode selectNode(List<StorageNode> nodes, long fileSize);
}
