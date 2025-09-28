package com.storagenode.controller;

import com.storagenode.model.FileMetadata;
import com.storagenode.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-ID") Long userId) {
        try {
            FileMetadata metadata = fileStorageService.storeFile(file, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("fileId", metadata.getFileId());
            response.put("fileName", metadata.getOriginalFileName());
            response.put("fileSize", metadata.getFileSize());
            response.put("contentType", metadata.getContentType());
            response.put("timestamp", Instant.now());
            response.put("nodeId", "storage-node"); // Will be updated with actual node ID

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("File upload failed", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Upload failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", Instant.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long fileId,
            @RequestHeader("X-User-ID") Long userId) {
        try {
            FileMetadata metadata = fileStorageService.getFileMetadata(fileId);
            if (metadata == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] fileContent = fileStorageService.retrieveFile(fileId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(metadata.getContentType()));
            headers.setContentDispositionFormData("attachment", metadata.getOriginalFileName());
            headers.setContentLength(fileContent.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileContent);
        } catch (Exception e) {
            log.error("File download failed for file ID: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long fileId,
            @RequestHeader("X-User-ID") Long userId) {
        try {
            boolean deleted = fileStorageService.deleteFile(fileId);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("File deletion failed for file ID: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{fileId}/exists")
    public ResponseEntity<Boolean> fileExists(@PathVariable Long fileId) {
        boolean exists = fileStorageService.fileExists(fileId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{fileId}/metadata")
    public ResponseEntity<FileMetadata> getFileMetadata(@PathVariable Long fileId) {
        FileMetadata metadata = fileStorageService.getFileMetadata(fileId);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(metadata);
    }
}