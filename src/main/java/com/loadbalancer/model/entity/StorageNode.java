package com.loadbalancer.model.entity;

import com.loadbalancer.model.enums.NodeStatus;
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
@Table(
    name = "StorageContainers",
    uniqueConstraints = {
      @UniqueConstraint(columnNames = {"container_name", "host_address", "port"})
    })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
public class StorageNode {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "container_id")
  private Long containerId;

  @NotBlank(message = "Container name cannot be blank")
  @Column(name = "container_name", unique = true, nullable = false)
  private String containerName;

  @NotBlank(message = "Host address cannot be blank")
  @Column(name = "host_address", nullable = false)
  private String hostAddress;

  @NotNull(message = "Port cannot be null")
  @Positive(message = "Port must be a positive number")
  @Column(name = "port", nullable = false)
  private Integer port;

  @NotNull(message = "Capacity cannot be null")
  @Positive(message = "Capacity must be a positive number")
  @Column(name = "capacity", nullable = false)
  private Long capacity;

  @Builder.Default
  @Column(name = "used_space", nullable = false)
  private Long usedSpace = 0L;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private NodeStatus status = NodeStatus.ACTIVE;

  @Builder.Default
  @Version
  @Column(name = "version", nullable = false)
  private Integer version = 0;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}
