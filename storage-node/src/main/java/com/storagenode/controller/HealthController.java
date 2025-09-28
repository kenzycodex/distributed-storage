package com.storagenode.controller;

import com.storagenode.service.FileStorageService;
import com.storagenode.service.NodeRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
public class HealthController {
    private final FileStorageService fileStorageService;
    private final NodeRegistrationService nodeRegistrationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("nodeId", nodeRegistrationService.getNodeId());
        health.put("usedSpace", fileStorageService.getUsedSpace());
        health.put("fileCount", fileStorageService.getFileCount());
        return ResponseEntity.ok(health);
    }

    @GetMapping("/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("OK");
    }
}