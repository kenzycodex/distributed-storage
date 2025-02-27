// controller/LoadBalancerController.java
package com.loadbalancer.controller;

import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.service.LoadBalancerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loadbalancer")
@RequiredArgsConstructor
@Slf4j
public class LoadBalancerController {
    private final LoadBalancerService loadBalancerService;

    @GetMapping("/node")
    public
    ResponseEntity<StorageNode> getNode(
            @RequestParam(required = false) String strategy,
            @RequestParam long fileSize) {
        try {
            StorageNode node = loadBalancerService.selectNode(strategy, fileSize);
            loadBalancerService.incrementNodeConnections(node.getContainerId().toString());
            return ResponseEntity.ok(node);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/node/{nodeId}/complete")
    public
    ResponseEntity<Void> completeRequest(@PathVariable String nodeId) {
        loadBalancerService.decrementNodeConnections(nodeId);
        return ResponseEntity.ok().build();
    }
}
