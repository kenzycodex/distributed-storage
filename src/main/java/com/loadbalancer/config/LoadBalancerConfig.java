// config/LoadBalancerConfig.java
package com.loadbalancer.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "loadbalancer")
@Getter
@Setter
public class LoadBalancerConfig {
  private Strategies strategies;
  private HealthCheck healthCheck;
  private Metrics metrics;
  private Queue queue;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Getter
  @Setter
  public static class Strategies {
    private String defaultStrategy;
    private List<String> available;
  }

  @Getter
  @Setter
  public static class HealthCheck {
    private long interval;
    private long timeout;
    private int failureThreshold;
    private int successThreshold;
  }

  @Getter
  @Setter
  public static class Metrics {
    private boolean enabled;
    private long collectionInterval;
    private int retentionDays;
    private int maxResponseTimeEntries;
  }

  @Getter
  @Setter
  public static class Queue {
    private int maxSize;
    private int workerThreads;
    private int batchSize;
    private Retry retry;

    @Getter
    @Setter
    public static class Retry {
      private int maxAttempts;
      private long initialInterval;
      private double multiplier;
      private long maxInterval;
    }
  }
}
