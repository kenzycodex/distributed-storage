package com.loadbalancer.service;

import com.loadbalancer.model.entity.FileMetadata;
import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.repository.FileMetadataRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileMetadataService {
    private final FileMetadataRepository fileMetadataRepository;
    private final StorageNodeService storageNodeService;

    @Transactional
    public FileMetadata createFileMetadata(String originalFilename, String storedFilename,
                                          Long fileSize, String contentType, Long nodeId,
                                          Long userId, String checksum) {
        FileMetadata metadata = FileMetadata.builder()
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .fileSize(fileSize)
                .contentType(contentType)
                .nodeId(nodeId)
                .userId(userId)
                .checksum(checksum)
                .isActive(true)
                .build();

        FileMetadata savedMetadata = fileMetadataRepository.save(metadata);
        log.info("Created file metadata for file {} on node {}", savedMetadata.getFileId(), nodeId);
        return savedMetadata;
    }

    @Transactional(readOnly = true)
    public Optional<FileMetadata> getFileMetadata(Long fileId) {
        return fileMetadataRepository.findByFileIdAndIsActiveTrue(fileId);
    }

    @Transactional(readOnly = true)
    public Optional<StorageNode> getNodeForFile(Long fileId) {
        Optional<FileMetadata> metadata = getFileMetadata(fileId);
        if (metadata.isPresent()) {
            return storageNodeService.getNode(metadata.get().getNodeId());
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<FileMetadata> getFilesByNode(Long nodeId) {
        return fileMetadataRepository.findByNodeIdAndIsActiveTrue(nodeId);
    }

    @Transactional(readOnly = true)
    public List<FileMetadata> getFilesByUser(Long userId) {
        return fileMetadataRepository.findByUserIdAndIsActiveTrue(userId);
    }

    @Transactional
    public void updateLastAccessed(Long fileId) {
        Optional<FileMetadata> metadata = fileMetadataRepository.findByFileIdAndIsActiveTrue(fileId);
        if (metadata.isPresent()) {
            metadata.get().setLastAccessed(LocalDateTime.now());
            fileMetadataRepository.save(metadata.get());
        }
    }

    @Transactional
    public boolean deleteFileMetadata(Long fileId) {
        int updatedRows = fileMetadataRepository.markAsDeleted(fileId);
        if (updatedRows > 0) {
            log.info("Marked file {} as deleted", fileId);
            return true;
        }
        return false;
    }

    @Transactional
    public void markAllFilesAsDeletedForNode(Long nodeId) {
        int updatedRows = fileMetadataRepository.markAllFilesAsDeletedForNode(nodeId);
        log.info("Marked {} files as deleted for node {}", updatedRows, nodeId);
    }

    @Transactional(readOnly = true)
    public boolean fileExists(Long fileId) {
        return fileMetadataRepository.existsByFileIdAndIsActiveTrue(fileId);
    }

    @Transactional(readOnly = true)
    public Map<Long, NodeStorageStats> getNodeStorageStatistics() {
        List<Object[]> results = fileMetadataRepository.getNodeStorageStatistics();
        return results.stream().collect(Collectors.toMap(
            result -> (Long) result[0],
            result -> new NodeStorageStats((Long) result[1], (Long) result[2])
        ));
    }

    @Transactional(readOnly = true)
    public List<FileMetadata> searchFilesByName(String filename) {
        return fileMetadataRepository.findByOriginalFilenameContainingIgnoreCaseAndIsActiveTrue(filename);
    }

    @Transactional(readOnly = true)
    public List<FileMetadata> getInactiveFiles(int daysOld) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysOld);
        return fileMetadataRepository.findInactiveFiles(cutoffTime);
    }

    @Transactional(readOnly = true)
    public Long getTotalFilesForNode(Long nodeId) {
        return fileMetadataRepository.countActiveFilesByNode(nodeId);
    }

    @Transactional(readOnly = true)
    public Long getTotalSizeForNode(Long nodeId) {
        Long totalSize = fileMetadataRepository.getTotalSizeByNode(nodeId);
        return totalSize != null ? totalSize : 0L;
    }

    public static class NodeStorageStats {
        private final Long fileCount;
        private final Long totalSize;

        public NodeStorageStats(Long fileCount, Long totalSize) {
            this.fileCount = fileCount != null ? fileCount : 0L;
            this.totalSize = totalSize != null ? totalSize : 0L;
        }

        public Long getFileCount() { return fileCount; }
        public Long getTotalSize() { return totalSize; }
    }
}