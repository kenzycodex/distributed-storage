package com.storagenode.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeHeartbeat {
    private Long containerId;
    private String status;
    private Long usedSpace;
}