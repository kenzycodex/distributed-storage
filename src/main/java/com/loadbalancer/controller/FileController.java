package com.loadbalancer.controller;

import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.service.LoadBalancerService;
import com.loadbalancer.service.StorageNodeService;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
  private final LoadBalancerService loadBalancerService;
  private final StorageNodeService storageNodeService;
  private final RestTemplate restTemplate;

  @PostMapping("/upload")
  public ResponseEntity<?> uploadFile(
      @RequestParam("file") MultipartFile file, @RequestHeader("X-User-ID") Long userId) {
    long startTime = System.currentTimeMillis();
    StorageNode selectedNode = null;
    try {
      selectedNode = loadBalancerService.selectNode(null, file.getSize());
      log.info(
          "Selected node {} for file upload, size: {}",
          selectedNode.getContainerId(),
          file.getSize());

      String uploadUrl =
          String.format(
              "http://%s:%d/api/v1/files/upload",
              selectedNode.getHostAddress(), selectedNode.getPort());

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers.set("X-User-ID", userId.toString());

      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add(
          "file",
          new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
              return file.getOriginalFilename();
            }
          });

      ResponseEntity<?> response =
          restTemplate.postForEntity(uploadUrl, new HttpEntity<>(body, headers), Map.class);

      long duration = System.currentTimeMillis() - startTime;
      loadBalancerService.recordRequest(selectedNode.getContainerId().toString(), true, duration);

      return response;

    } catch (Exception e) {
      log.error("File upload failed", e);
      if (selectedNode != null) {
        long duration = System.currentTimeMillis() - startTime;
        loadBalancerService.recordRequest(
            selectedNode.getContainerId().toString(), false, duration);
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "error", "Upload failed",
                  "message", e.getMessage(),
                  "timestamp", Instant.now()));
    }
  }

  @GetMapping("/{fileId}")
  public ResponseEntity<?> downloadFile(
      @PathVariable Long fileId, @RequestHeader("X-User-ID") Long userId) {
    long startTime = System.currentTimeMillis();
    StorageNode node = null;
    try {
      node = loadBalancerService.getNodeForFile(fileId);
      String downloadUrl =
          String.format(
              "http://%s:%d/api/v1/files/%d", node.getHostAddress(), node.getPort(), fileId);

      HttpHeaders headers = new HttpHeaders();
      headers.set("X-User-ID", userId.toString());

      ResponseEntity<byte[]> response =
          restTemplate.exchange(
              downloadUrl, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);

      long duration = System.currentTimeMillis() - startTime;
      loadBalancerService.recordRequest(node.getContainerId().toString(), true, duration);

      return ResponseEntity.ok().headers(response.getHeaders()).body(response.getBody());

    } catch (Exception e) {
      log.error("File download failed", e);
      if (node != null) {
        long duration = System.currentTimeMillis() - startTime;
        loadBalancerService.recordRequest(node.getContainerId().toString(), false, duration);
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "error", "Download failed",
                  "message", e.getMessage(),
                  "timestamp", Instant.now()));
    }
  }

  @DeleteMapping("/{fileId}")
  public ResponseEntity<?> deleteFile(
      @PathVariable Long fileId, @RequestHeader("X-User-ID") Long userId) {
    long startTime = System.currentTimeMillis();
    StorageNode node = null;
    try {
      node = loadBalancerService.getNodeForFile(fileId);
      String deleteUrl =
          String.format(
              "http://%s:%d/api/v1/files/%d", node.getHostAddress(), node.getPort(), fileId);

      HttpHeaders headers = new HttpHeaders();
      headers.set("X-User-ID", userId.toString());

      restTemplate.exchange(deleteUrl, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);

      long duration = System.currentTimeMillis() - startTime;
      loadBalancerService.recordRequest(node.getContainerId().toString(), true, duration);

      return ResponseEntity.noContent().build();

    } catch (Exception e) {
      log.error("File deletion failed", e);
      if (node != null) {
        long duration = System.currentTimeMillis() - startTime;
        loadBalancerService.recordRequest(node.getContainerId().toString(), false, duration);
      }
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              Map.of(
                  "error", "Deletion failed",
                  "message", e.getMessage(),
                  "timestamp", Instant.now()));
    }
  }
}
