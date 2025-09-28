package com.storagenode.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeRegistration {
    private Long containerId;
    private String containerName;
    private String hostAddress;
    private Integer port;
    private Long capacity;
}