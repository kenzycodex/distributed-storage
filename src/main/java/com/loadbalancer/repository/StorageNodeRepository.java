// repository/StorageNodeRepository.java
package com.loadbalancer.repository;

import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.model.enums.NodeStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StorageNodeRepository extends JpaRepository<StorageNode, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT n FROM StorageNode n WHERE n.containerId = :id")
  Optional<StorageNode> findByIdWithLock(@Param("id") Long id);

  @Query("SELECT n FROM StorageNode n WHERE n.status = :status")
  List<StorageNode> findByStatus(@Param("status") NodeStatus status);

  @Query("SELECT n FROM StorageNode n WHERE n.status = 'ACTIVE' AND n.usedSpace < n.capacity")
  List<StorageNode> findAvailableNodes();

  @Query(
      "SELECT n FROM StorageNode n WHERE n.containerName = :containerName OR "
          + "(n.hostAddress = :hostAddress AND n.port = :port)")
  Optional<StorageNode> findByContainerNameOrHostAddressAndPort(
      @Param("containerName") String containerName,
      @Param("hostAddress") String hostAddress,
      @Param("port") Integer port);
}
