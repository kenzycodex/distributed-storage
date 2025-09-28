package com.loadbalancer.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "FileMetadata")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @NotBlank(message = "Original filename cannot be blank")
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @NotBlank(message = "Stored filename cannot be blank")
    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @NotNull(message = "File size cannot be null")
    @Positive(message = "File size must be positive")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @NotNull(message = "Node ID cannot be null")
    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "checksum")
    private String checksum;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "upload_time", nullable = false, updatable = false)
    private LocalDateTime uploadTime;

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed;

    @Builder.Default
    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 0;

    @PrePersist
    protected void onCreate() {
        this.uploadTime = LocalDateTime.now();
        this.lastAccessed = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastAccessed = LocalDateTime.now();
    }
}