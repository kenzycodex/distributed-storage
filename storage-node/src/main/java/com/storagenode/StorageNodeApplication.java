package com.storagenode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StorageNodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(StorageNodeApplication.class, args);
    }
}