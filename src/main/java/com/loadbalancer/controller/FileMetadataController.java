package com.loadbalancer.controller;

import com.loadbalancer.model.entity.FileMetadata;
import com.loadbalancer.service.FileMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/metadata")
@RequiredArgsConstructor
@Slf4j
public class FileMetadataController {
    private final FileMetadataService fileMetadataService;

    @GetMapping("/files/{fileId}")
    public ResponseEntity<FileMetadata> getFileMetadata(@PathVariable Long fileId) {
        Optional<FileMetadata> metadata = fileMetadataService.getFileMetadata(fileId);
        return metadata.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/files/user/{userId}")
    public ResponseEntity<List<FileMetadata>> getUserFiles(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<FileMetadata> files = fileMetadataService.getFilesByUser(userId);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/files/node/{nodeId}")
    public ResponseEntity<List<FileMetadata>> getNodeFiles(@PathVariable Long nodeId) {
        List<FileMetadata> files = fileMetadataService.getFilesByNode(nodeId);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/files/search")
    public ResponseEntity<List<FileMetadata>> searchFiles(
            @RequestParam String filename) {
        List<FileMetadata> files = fileMetadataService.searchFilesByName(filename);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/storage/statistics")
    public ResponseEntity<Map<Long, FileMetadataService.NodeStorageStats>> getStorageStatistics() {
        Map<Long, FileMetadataService.NodeStorageStats> stats =
            fileMetadataService.getNodeStorageStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/files/inactive")
    public ResponseEntity<List<FileMetadata>> getInactiveFiles(
            @RequestParam(defaultValue = "30") int daysOld) {
        List<FileMetadata> files = fileMetadataService.getInactiveFiles(daysOld);
        return ResponseEntity.ok(files);
    }

    @PostMapping("/files/{fileId}/access")
    public ResponseEntity<Void> updateFileAccess(@PathVariable Long fileId) {
        fileMetadataService.updateLastAccessed(fileId);
        return ResponseEntity.ok().build();
    }
}