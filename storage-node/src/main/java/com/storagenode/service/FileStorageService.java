package com.storagenode.service;

import com.storagenode.config.StorageConfig;
import com.storagenode.model.FileMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileStorageService {
    private final StorageConfig storageConfig;
    private final Map<Long, FileMetadata> fileRegistry = new ConcurrentHashMap<>();
    private Long fileIdCounter = 1L;

    public FileMetadata storeFile(MultipartFile file, Long userId) throws IOException {
        // Ensure storage directory exists
        Path storageDir = Paths.get(storageConfig.getBasePath());
        if (!Files.exists(storageDir)) {
            Files.createDirectories(storageDir);
        }

        // Generate unique file ID and name
        Long fileId = generateFileId();
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String storedFileName = fileId + "_" + UUID.randomUUID().toString() + fileExtension;
        Path filePath = storageDir.resolve(storedFileName);

        // Store the file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create metadata
        FileMetadata metadata = FileMetadata.builder()
                .fileId(fileId)
                .fileName(storedFileName)
                .originalFileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .filePath(filePath.toString())
                .uploadTime(LocalDateTime.now())
                .userId(userId)
                .build();

        // Store in registry
        fileRegistry.put(fileId, metadata);

        log.info("Stored file {} with ID {} for user {}", file.getOriginalFilename(), fileId, userId);
        return metadata;
    }

    public byte[] retrieveFile(Long fileId) throws IOException {
        FileMetadata metadata = fileRegistry.get(fileId);
        if (metadata == null) {
            throw new IllegalArgumentException("File not found: " + fileId);
        }

        Path filePath = Paths.get(metadata.getFilePath());
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("File does not exist on disk: " + fileId);
        }

        return Files.readAllBytes(filePath);
    }

    public boolean deleteFile(Long fileId) {
        FileMetadata metadata = fileRegistry.get(fileId);
        if (metadata == null) {
            return false;
        }

        try {
            Path filePath = Paths.get(metadata.getFilePath());
            Files.deleteIfExists(filePath);
            fileRegistry.remove(fileId);
            log.info("Deleted file with ID {}", fileId);
            return true;
        } catch (IOException e) {
            log.error("Failed to delete file with ID {}", fileId, e);
            return false;
        }
    }

    public boolean fileExists(Long fileId) {
        return fileRegistry.containsKey(fileId);
    }

    public FileMetadata getFileMetadata(Long fileId) {
        return fileRegistry.get(fileId);
    }

    public long getUsedSpace() {
        return fileRegistry.values().stream()
                .mapToLong(FileMetadata::getFileSize)
                .sum();
    }

    public long getFileCount() {
        return fileRegistry.size();
    }

    private synchronized Long generateFileId() {
        return fileIdCounter++;
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}