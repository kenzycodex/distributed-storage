package com.loadbalancer.config;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration class to register bean aliases for strategy components.
 * This allows both camelCase (for SonarQube) and hyphenated (for existing config) bean names to work.
 */
@Configuration
public class StrategyConfig {

    @Autowired
    private BeanFactory beanFactory;

    /**
     * Register aliases for all strategy beans to maintain backward compatibility
     * with existing configuration that uses hyphenated names.
     */
    @PostConstruct
    public void registerAliases() {
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;

        // Register aliases for all strategy beans
        factory.registerAlias("roundRobin", "round-robin");
        factory.registerAlias("leastConnection", "least-connection");
        factory.registerAlias("firstComeFirstServe", "first-come-first-serve");
        factory.registerAlias("shortestJobNext", "shortest-job-next");
        factory.registerAlias("weightedRoundRobin", "weighted-round-robin");
    }
}