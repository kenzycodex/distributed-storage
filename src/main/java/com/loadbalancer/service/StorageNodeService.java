package com.loadbalancer.service;

import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.model.enums.NodeStatus;
import com.loadbalancer.repository.StorageNodeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StorageNodeService {
  private final StorageNodeRepository storageNodeRepository;

  @Transactional(readOnly = true)
  public List<StorageNode> getAvailableNodes() {
    return storageNodeRepository.findByStatus(NodeStatus.ACTIVE);
  }

  @Transactional(readOnly = true)
  public boolean isNodeRegistered(Long nodeId) {
    return storageNodeRepository.existsById(nodeId);
  }

  @Transactional
  public void activateNode(Long nodeId) {
    storageNodeRepository
        .findById(nodeId)
        .ifPresent(
            node -> {
              node.setStatus(NodeStatus.ACTIVE);
              storageNodeRepository.save(node);
              log.info("Activated node: {}", nodeId);
            });
  }

  @Transactional
  public StorageNode registerNode(StorageNode node) {
    node.setStatus(NodeStatus.ACTIVE);
    StorageNode savedNode = storageNodeRepository.save(node);
    log.info("Registered new node: {}", savedNode.getContainerId());
    return savedNode;
  }

  @Transactional
  public void updateNodeStatus(Long nodeId, NodeStatus status, Long usedSpace) {
    StorageNode node =
        storageNodeRepository
            .findById(nodeId)
            .orElseThrow(() -> new EntityNotFoundException("Node not found: " + nodeId));

    node.setStatus(status);
    if (usedSpace != null) {
      node.setUsedSpace(usedSpace);
    }

    storageNodeRepository.save(node);
    log.debug("Updated node {} status to {} and used space to {}", nodeId, status, usedSpace);
  }

  @Transactional(readOnly = true)
  public Optional<StorageNode> getNode(Long nodeId) {
    return storageNodeRepository.findById(nodeId);
  }
}
