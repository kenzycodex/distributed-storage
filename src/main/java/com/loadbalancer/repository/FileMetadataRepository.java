package com.loadbalancer.repository;

import com.loadbalancer.model.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByFileIdAndIsActiveTrue(Long fileId);

    List<FileMetadata> findByNodeIdAndIsActiveTrue(Long nodeId);

    List<FileMetadata> findByUserIdAndIsActiveTrue(Long userId);

    @Query("SELECT fm FROM FileMetadata fm WHERE fm.nodeId = :nodeId AND fm.isActive = true")
    List<FileMetadata> findActiveFilesByNode(@Param("nodeId") Long nodeId);

    @Query("SELECT COUNT(fm) FROM FileMetadata fm WHERE fm.nodeId = :nodeId AND fm.isActive = true")
    Long countActiveFilesByNode(@Param("nodeId") Long nodeId);

    @Query("SELECT SUM(fm.fileSize) FROM FileMetadata fm WHERE fm.nodeId = :nodeId AND fm.isActive = true")
    Long getTotalSizeByNode(@Param("nodeId") Long nodeId);

    @Query("SELECT fm FROM FileMetadata fm WHERE fm.lastAccessed < :cutoffTime AND fm.isActive = true")
    List<FileMetadata> findInactiveFiles(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Modifying
    @Query("UPDATE FileMetadata fm SET fm.isActive = false WHERE fm.fileId = :fileId")
    int markAsDeleted(@Param("fileId") Long fileId);

    @Modifying
    @Query("UPDATE FileMetadata fm SET fm.isActive = false WHERE fm.nodeId = :nodeId")
    int markAllFilesAsDeletedForNode(@Param("nodeId") Long nodeId);

    @Query("SELECT fm.nodeId, COUNT(fm) as fileCount, SUM(fm.fileSize) as totalSize " +
           "FROM FileMetadata fm WHERE fm.isActive = true GROUP BY fm.nodeId")
    List<Object[]> getNodeStorageStatistics();

    List<FileMetadata> findByOriginalFilenameContainingIgnoreCaseAndIsActiveTrue(String filename);

    boolean existsByFileIdAndIsActiveTrue(Long fileId);
}