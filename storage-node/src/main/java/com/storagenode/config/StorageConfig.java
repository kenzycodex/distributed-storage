package com.storagenode.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "storage")
@Data
public class StorageConfig {
    private String basePath = "/app/storage";
    private Node node = new Node();
    private LoadBalancer loadbalancer = new LoadBalancer();

    @Data
    public static class Node {
        private String name = "storage-node-1";
        private Long capacity = 10737418240L; // 10GB
        private String host = "localhost";
        private Integer port = 8081;
    }

    @Data
    public static class LoadBalancer {
        private String host = "localhost";
        private Integer port = 8080;
        private String registrationUrl;
        private String heartbeatUrl;
    }
}