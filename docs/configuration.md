# Configuration Guide

This document provides comprehensive information on configuring the DistributedStorage system.

## Configuration Files

DistributedStorage uses Spring Boot's configuration system with YAML files. The configuration is split into three files:

1. `application.yml` - Common settings for all environments
2. `application-dev.yml` - Development environment settings
3. `application-prod.yml` - Production environment settings

## Activating Profiles

To activate a specific profile, you can:

1. Use the command line argument:
   ```bash
   java -jar loadbalancer.jar --spring.profiles.active=prod
   ```

2. Set the environment variable:
   ```bash
   export SPRING_PROFILES_ACTIVE=prod
   ```

3. Configure it in your application.yml:
   ```yaml
   spring:
     profiles:
       active: dev
   ```

## Configuration Sections

### Database Configuration

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/loadbalancer
    username: loadbalancer
    password: loadbalancer
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
```

| Property | Description | Default |
|----------|-------------|---------|
| `url` | Database connection URL | - |
| `username` | Database username | - |
| `password` | Database password | - |
| `driver-class-name` | JDBC driver class | `com.mysql.cj.jdbc.Driver` |
| `hikari.maximum-pool-size` | Maximum connection pool size | 10 |
| `hikari.minimum-idle` | Minimum number of idle connections | 5 |
| `hikari.idle-timeout` | Maximum time a connection can idle (ms) | 300000 |
| `hikari.connection-timeout` | Maximum time to wait for connection (ms) | 20000 |
| `hikari.max-lifetime` | Maximum lifetime of a connection (ms) | 1200000 |

### JPA Configuration

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
        implicit-strategy: org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl
    open-in-view: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
    show-sql: false
```

| Property | Description | Default |
|----------|-------------|---------|
| `hibernate.ddl-auto` | Database schema update strategy | `update` |
| `hibernate.naming.physical-strategy` | Physical naming strategy | `PhysicalNamingStrategyStandardImpl` |
| `hibernate.naming.implicit-strategy` | Implicit naming strategy | `ImplicitNamingStrategyLegacyJpaImpl` |
| `open-in-view` | Enable open EntityManager in view pattern | `false` |
| `properties.hibernate.dialect` | Hibernate dialect | `MySQL8Dialect` |
| `show-sql` | Display SQL queries in logs | `false` |

### Server Configuration

```yaml
server:
  port: 8080
  tomcat:
    max-threads: 200
    min-spare-threads: 20
    max-connections: 10000
    accept-count: 100
```

| Property | Description | Default |
|----------|-------------|---------|
| `port` | Server HTTP port | 8080 |
| `tomcat.max-threads` | Maximum worker threads | 200 |
| `tomcat.min-spare-threads` | Minimum spare worker threads | 20 |
| `tomcat.max-connections` | Maximum connections | 10000 |
| `tomcat.accept-count` | Max pending requests when all threads are busy | 100 |

### Management Endpoints

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
    prometheus:
      enabled: true
```

| Property | Description | Default |
|----------|-------------|---------|
| `endpoints.web.exposure.include` | Exposed management endpoints | `health,metrics,prometheus` |
| `endpoint.health.show-details` | Show health check details | `always` |
| `endpoint.metrics.enabled` | Enable metrics endpoint | `true` |
| `endpoint.prometheus.enabled` | Enable Prometheus metrics endpoint | `true` |

### Load Balancer Configuration

```yaml
loadbalancer:
  strategies:
    default: round-robin
    available:
      - round-robin
      - least-connection
      - shortest-job-next
      - first-come-first-serve
      - weighted-round-robin
  health-check:
    interval: 30000
    failure-threshold: 4
    success-threshold: 1
  heartbeat:
    interval: 30000
  metrics:
    enabled: true
    collection-interval: 15000
    retention-days: 30
    max-response-time-entries: 10000
  queue:
    max-size: 10000
    worker-threads: 5
    batch-size: 100
    retry:
      max-attempts: 3
      initial-interval: 1000
      multiplier: 2
      max-interval: 10000
```

#### Load Balancing Strategies

| Property | Description | Default |
|----------|-------------|---------|
| `strategies.default` | Default load balancing strategy | `round-robin` |
| `strategies.available` | List of available strategies | *see above* |

#### Health Check Settings

| Property | Description | Default |
|----------|-------------|---------|
| `health-check.interval` | Time between health checks (ms) | 30000 |
| `health-check.failure-threshold` | Failed checks before marking node inactive | 4 |
| `health-check.success-threshold` | Successful checks before reactivating node | 1 |

#### Heartbeat Settings

| Property | Description | Default |
|----------|-------------|---------|
| `heartbeat.interval` | Expected heartbeat interval from nodes (ms) | 30000 |

#### Metrics Settings

| Property | Description | Default |
|----------|-------------|---------|
| `metrics.enabled` | Enable metrics collection | `true` |
| `metrics.collection-interval` | Metrics collection interval (ms) | 15000 |
| `metrics.retention-days` | Days to retain metrics data | 30 |
| `metrics.max-response-time-entries` | Maximum response time entries to store | 10000 |

#### Queue Settings

| Property | Description | Default |
|----------|-------------|---------|
| `queue.max-size` | Maximum queue size for pending requests | 10000 |
| `queue.worker-threads` | Number of worker threads | 5 |
| `queue.batch-size` | Batch size for processing | 100 |
| `queue.retry.max-attempts` | Maximum retry attempts | 3 |
| `queue.retry.initial-interval` | Initial retry interval (ms) | 1000 |
| `queue.retry.multiplier` | Backoff multiplier for retries | 2 |
| `queue.retry.max-interval` | Maximum retry interval (ms) | 10000 |

### Logging Configuration

```yaml
logging:
  level:
    root: INFO
    com.loadbalancer: DEBUG
    org.springframework: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/load-balancer.log
    max-size: 10MB
    max-history: 7
```

| Property | Description | Default |
|----------|-------------|---------|
| `level.root` | Root logging level | `INFO` |
| `level.com.loadbalancer` | Application logging level | `DEBUG` |
| `level.org.springframework` | Spring framework logging level | `INFO` |
| `pattern.console` | Console log pattern | *see above* |
| `pattern.file` | File log pattern | *see above* |
| `file.name` | Log file path | `logs/load-balancer.log` |
| `file.max-size` | Maximum log file size | `10MB` |
| `file.max-history` | Number of log files to keep | 7 |

## Environment-Specific Configuration

### Development Environment (application-dev.yml)

Development environment typically uses:
- Local database connections
- Smaller connection pools
- More verbose logging
- Faster health check intervals

### Production Environment (application-prod.yml)

Production environment typically uses:
- Database servers with proper credentials
- Larger connection pools
- Performance-optimized settings
- Appropriate logging levels

## Advanced Configuration

### Custom Load Balancing Strategies

To implement a custom load balancing strategy:

1. Create a class that implements the `LoadBalancerStrategy` interface
2. Add your strategy to the list of available strategies in the configuration
3. Register your implementation as a Spring bean

Example:

```java
@Component("custom-strategy")
public class CustomLoadBalancerStrategy implements LoadBalancerStrategy {
    @Override
    public StorageNode selectNode(List<StorageNode> availableNodes, long fileSize) {
        // Your custom node selection logic
    }
}
```

```yaml
loadbalancer:
  strategies:
    available:
      - round-robin
      - least-connection
      - custom-strategy
```

### Setting Up External Monitoring

The application exposes Prometheus-compatible metrics. To set up monitoring:

1. Configure Prometheus to scrape metrics from your application:
   ```yaml
   scrape_configs:
     - job_name: 'distributed-storage'
       metrics_path: '/actuator/prometheus'
       static_configs:
         - targets: ['your-host:8080']
   ```

2. Set up Grafana dashboards using the exposed metrics.