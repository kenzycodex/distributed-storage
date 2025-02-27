// LoadBalancerApplication.java
package com.loadbalancer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class LoadBalancerApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoadBalancerApplication.class, args);
    }
}