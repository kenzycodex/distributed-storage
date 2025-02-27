# Load Balancing Strategies

DistributedStorage implements multiple load balancing strategies to optimize resource utilization and performance across storage nodes. This document provides details about each strategy and guidance on when to use them.

## Available Strategies

### Round Robin

The Round Robin strategy distributes requests sequentially across all available storage nodes, ensuring an even distribution of workload.

**Implementation:**
```java
@Component("round-robin")
public class RoundRobinStrategy implements LoadBalancerStrategy {
    private final AtomicInteger counter = new AtomicInteger(0);
    
    @Override
    public StorageNode selectNode(List<StorageNode> availableNodes, long fileSize) {
        if (availableNodes.isEmpty()) {
            throw new NoAvailableNodesException("No storage nodes available");
        }
        
        int index = counter.getAndIncrement() % availableNodes.size();
        return availableNodes.get(index);
    }
}
```

**When to use:**
- General purpose workloads
- When nodes have similar capacity and performance
- For evenly distributed file sizes
- Simple deployment scenarios

### Least Connection

The Least Connection strategy selects the node with the fewest active connections, helping to balance load dynamically.

**Implementation:**
```java
@Component("least-connection")
public class LeastConnectionStrategy implements LoadBalancerStrategy {
    private final LoadBalancerService loadBalancerService;
    
    @Autowired
    public LeastConnectionStrategy(LoadBalancerService loadBalancerService) {
        this.loadBalancerService = loadBalancerService;
    }
    
    @Override
    public StorageNode selectNode(List<StorageNode> availableNodes, long fileSize) {
        if (availableNodes.isEmpty()) {
            throw new NoAvailableNodesException("No storage nodes available");
        }
        
        return availableNodes.stream()
                .min(Comparator.comparingInt(node -> 
                    loadBalancerService.getNodeConnections(node.getContainerId().toString())))
                .orElse(availableNodes.get(0));
    }
}
```

**When to use:**
- Varied connection durations
- When some operations take longer than others
- To prevent overloading individual nodes
- For handling concurrent connections efficiently

### Shortest Job Next

The Shortest Job Next strategy selects nodes based on the file size, preferring to send smaller files to nodes that can complete them quickly.

**Implementation:**
```java
@Component("shortest-job-next")
public class ShortestJobNextStrategy implements LoadBalancerStrategy {
    private final MetricsService metricsService;
    
    @Autowired
    public ShortestJobNextStrategy(MetricsService metricsService) {
        this.metricsService = metricsService;
    }
    
    @Override
    public StorageNode selectNode(List<StorageNode> availableNodes, long fileSize) {
        if (availableNodes.isEmpty()) {
            throw new NoAvailableNodesException("No storage nodes available");
        }
        
        // Find the node with the best response time for this file size
        return availableNodes.stream()
                .min(Comparator.comparingDouble(node -> {
                    RequestStats stats = metricsService.getNodeStats(node.getContainerId().toString());
                    return stats.getAverageResponseTime() * (fileSize / 1024.0);
                }))
                .orElse(availableNodes.get(0));
    }
}
```

**When to use:**
- When file sizes vary significantly
- For optimizing overall throughput
- When processing time correlates with file size
- Systems with varied node performance

### First Come First Serve

The First Come First Serve strategy maintains a queue of requests and processes them in the order they were received.

**Implementation:**
```java
@Component("first-come-first-serve")
public class FirstComeFirstServeStrategy implements LoadBalancerStrategy {
    private final Queue<StorageNode> nodeQueue = new ConcurrentLinkedQueue<>();
    
    @PostConstruct
    public void initialize() {
        // Initialize with available nodes
        storageNodeService.getAvailableNodes().forEach(nodeQueue::offer);
    }
    
    @Override
    public synchronized StorageNode selectNode(List<StorageNode> availableNodes, long fileSize) {
        if (availableNodes.isEmpty()) {
            throw new NoAvailableNodesException("No storage nodes available");
        }
        
        // If queue is empty or contains unavailable nodes, reinitialize
        if (nodeQueue.isEmpty() || !containsOnlyAvailableNodes(availableNodes)) {
            nodeQueue.clear();
            availableNodes.forEach(nodeQueue::offer);
        }
        
        StorageNode node = nodeQueue.poll();
        nodeQueue.offer(node); // Put it back at the end of the queue
        return node;
    }
    
    private boolean containsOnlyAvailableNodes(List<StorageNode> availableNodes) {
        Set<Long> availableIds = availableNodes.stream()
                .map(StorageNode::getContainerId)
                .collect(Collectors.toSet());
                
        return nodeQueue.stream()
                .allMatch(node -> availableIds.contains(node.getContainerId()));
    }
}
```

**When to use:**
- Fair distribution of resources
- Predictable request handling
- Simple queue-based workloads
- When fairness is more important than optimization

### Weighted Round Robin

The Weighted Round Robin strategy assigns weights to nodes based on their capacity, giving more requests to nodes with higher capacity.

**Implementation:**
```java
@Component("weighted-round-robin")
public class WeightedRoundRobinStrategy implements LoadBalancerStrategy {
    private final AtomicInteger counter = new AtomicInteger(0);
    
    @Override
    public StorageNode selectNode(List<StorageNode> availableNodes, long fileSize) {
        if (availableNodes.isEmpty()) {
            throw new NoAvailableNodesException("No storage nodes available");
        }
        
        // Calculate weights based on available space
        List<StorageNode> weightedNodes = new ArrayList<>();
        
        for (StorageNode node : availableNodes) {
            long availableSpace = node.getCapacity() - node.getUsedSpace();
            int weight = Math.max(1, (int) (availableSpace / (1024 * 1024 * 10))); // 10MB units
            
            for (int i = 0; i < weight; i++) {
                weightedNodes.add(node);
            }
        }
        
        int index = counter.getAndIncrement() % weightedNodes.size();
        return weightedNodes.get(index);
    }
}
```

**When to use:**
- Nodes with varying capacity
- When you want to utilize larger nodes more heavily
- For optimizing resource utilization
- Heterogeneous infrastructure

## Custom Strategy Implementation

You can implement custom load balancing strategies by following these steps:

1. Create a class that implements the `LoadBalancerStrategy` interface:

```java
public interface LoadBalancerStrategy {
    StorageNode selectNode(List<StorageNode> availableNodes, long fileSize);
}
```

2. Add the Spring `@Component` annotation with a unique name:

```java
@Component("my-custom-strategy")
public class MyCustomStrategy implements LoadBalancerStrategy {
    // Your implementation
}
```

3. Register your strategy in the configuration:

```yaml
loadbalancer:
  strategies:
    available:
      - round-robin
      - least-connection
      - my-custom-strategy
```

## Choosing the Right Strategy

### Performance Considerations

- **CPU-bound operations**: Use Least Connection strategy to distribute processing load
- **I/O-bound operations**: Use Weighted Round Robin to consider storage capacity
- **Mixed workloads**: Consider Shortest Job Next for optimizing throughput

### Scalability Factors

- **Homogeneous nodes**: Round Robin works well for identical nodes
- **Heterogeneous nodes**: Weighted strategies accommodate different capacities
- **Dynamic scaling**: Least Connection adapts well to nodes being added or removed

### Workload Patterns

| Workload Pattern | Recommended Strategy |
|-----------------|---------------------|
| Uniform file sizes | Round Robin |
| Variable file sizes | Shortest Job Next |
| Varied node capacities | Weighted Round Robin |
| High concurrency | Least Connection |
| Consistent fairness | First Come First Serve |

## Monitoring Strategy Performance

The metrics system provides insights into how each strategy performs:

```
GET /api/v1/metrics/strategy/{strategyName}
```

Monitor these key metrics to evaluate strategy effectiveness:

- Average response time
- Request distribution across nodes
- Node resource utilization
- Error rates per strategy

## Failover Behavior

All strategies automatically exclude nodes that are marked as inactive by the health check system. If a strategy attempts to select an inactive node, it will be filtered out and an alternate node will be selected.

If no active nodes are available, a `NoAvailableNodesException` will be thrown, allowing the system to implement appropriate fallback behavior.