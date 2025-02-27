// strategy/RoundRobinStrategy.java
package com.loadbalancer.strategy;

import com.loadbalancer.model.entity.StorageNode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component("round-robin")
public class RoundRobinStrategy implements LoadBalancerStrategy {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public StorageNode selectNode(List<StorageNode> nodes, long fileSize) {
        if (nodes.isEmpty()) {
            throw new IllegalStateException("No available nodes");
        }

        int index = counter.getAndIncrement() % nodes.size();
        return nodes.get(index);
    }
}
