package com.storagenode.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    private Long fileId;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    private String filePath;
    private LocalDateTime uploadTime;
    private Long userId;
}