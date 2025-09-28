package com.loadbalancer.controller;

import com.loadbalancer.exception.FileDownloadException;
import com.loadbalancer.exception.FileOperationException;
import com.loadbalancer.model.entity.FileMetadata;
import com.loadbalancer.model.entity.StorageNode;
import com.loadbalancer.service.LoadBalancerService;
import com.loadbalancer.service.StorageNodeService;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller handling file operations through a load balancer architecture.
 * Forwards requests to appropriate storage nodes.
 */
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
  private final LoadBalancerService loadBalancerService;
  private final StorageNodeService storageNodeService;
  private final RestTemplate restTemplate;

  // Constants for duplicated literals
  private static final String HEADER_USER_ID = "X-User-ID";
  private static final String KEY_ERROR = "error";
  private static final String KEY_MESSAGE = "message";
  private static final String KEY_TIMESTAMP = "timestamp";
  private static final String FILE_PARAM = "file";
  private static final String UPLOAD_PATH = "upload";
  private static final String UPLOAD_FAILED = "Upload failed";
  private static final String DOWNLOAD_FAILED = "Download failed: ";
  private static final String DELETION_FAILED = "Deletion failed";
  private static final String FILE_UPLOAD_FAILED_LOG = "File upload failed";
  private static final String FILE_DOWNLOAD_FAILED_LOG = "File download failed";
  private static final String FILE_DELETION_FAILED_LOG = "File deletion failed";

  // Move URI path formats to configuration
  @Value("${api.storage.path.format:http://%s:%d/api/v1/files/%s}")
  private String apiPathFormat;

  /**
   * Uploads a file to the selected storage node.
   *
   * @param file The file to upload
   * @param userId The ID of the user uploading the file
   * @return Response from the storage node
   */
  @PostMapping("/upload")
  public ResponseEntity<Map<String, Object>> uploadFile(
          @RequestParam(FILE_PARAM) MultipartFile file, @RequestHeader(HEADER_USER_ID) Long userId) {
    long startTime = System.currentTimeMillis();
    StorageNode selectedNode = null;
    try {
      selectedNode = loadBalancerService.selectNode(null, file.getSize());
      log.info(
              "Selected node {} for file upload, size: {}",
              selectedNode.getContainerId(),
              file.getSize());

      String uploadUrl = String.format(
              apiPathFormat,
              selectedNode.getHostAddress(),
              selectedNode.getPort(),
              UPLOAD_PATH);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      headers.set(HEADER_USER_ID, userId.toString());

      MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
      body.add(
              FILE_PARAM,
              createByteResource(file));

      // Use explicit HashMap type to match the return type
      @SuppressWarnings("unchecked")
      HashMap<String, Object> responseMap = restTemplate.postForObject(
              uploadUrl,
              new HttpEntity<>(body, headers),
              HashMap.class
      );

      if (responseMap != null && responseMap.containsKey("fileId")) {
        // Store metadata in database
        Long fileId = Long.valueOf(responseMap.get("fileId").toString());
        String storedFilename = responseMap.get("fileName") != null ?
                responseMap.get("fileName").toString() : file.getOriginalFilename();

        loadBalancerService.storeFileMetadata(
                file.getOriginalFilename(),
                storedFilename,
                file.getSize(),
                file.getContentType(),
                selectedNode.getContainerId(),
                userId,
                null // checksum - can be added later
        );

        // Update response with node information
        responseMap.put("nodeId", selectedNode.getContainerId());
        responseMap.put("nodeName", selectedNode.getContainerName());
      }

      long duration = System.currentTimeMillis() - startTime;
      loadBalancerService.recordRequest(selectedNode.getContainerId().toString(), true, duration);

      return ResponseEntity.ok(responseMap);
    } catch (Exception e) {
      log.error(FILE_UPLOAD_FAILED_LOG, e);
      if (selectedNode != null) {
        long duration = System.currentTimeMillis() - startTime;
        loadBalancerService.recordRequest(
                selectedNode.getContainerId().toString(), false, duration);
      }

      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put(KEY_ERROR, UPLOAD_FAILED);
      errorResponse.put(KEY_MESSAGE, e.getMessage());
      errorResponse.put(KEY_TIMESTAMP, Instant.now());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  /**
   * Creates a ByteArrayResource for file transfer.
   *
   * @param file The MultipartFile to convert
   * @return ByteArrayResource with the file's content
   * @throws FileOperationException If reading the file fails
   */
  private ByteArrayResource createByteResource(final MultipartFile file) throws FileOperationException {
    try {
      return new ByteArrayResource(file.getBytes()) {
        @Override
        public String getFilename() {
          return file.getOriginalFilename();
        }
      };
    } catch (Exception e) {
      throw new FileOperationException("Failed to read file bytes", e);
    }
  }

  /**
   * Downloads a file from the storage node containing it.
   *
   * @param fileId The ID of the file to download
   * @param userId The ID of the user downloading the file
   * @return The file content
   * @throws FileDownloadException If the download fails
   */
  @GetMapping("/{fileId}")
  public ResponseEntity<byte[]> downloadFile(
          @PathVariable Long fileId, @RequestHeader(HEADER_USER_ID) Long userId) {
    long startTime = System.currentTimeMillis();
    StorageNode node = null;
    try {
      node = loadBalancerService.getNodeForFile(fileId);
      String downloadUrl = String.format(
              apiPathFormat,
              node.getHostAddress(),
              node.getPort(),
              fileId.toString());

      HttpHeaders headers = new HttpHeaders();
      headers.set(HEADER_USER_ID, userId.toString());

      ResponseEntity<byte[]> response =
              restTemplate.exchange(
                      downloadUrl, HttpMethod.GET, new HttpEntity<>(headers), byte[].class);

      // Update file access time
      loadBalancerService.updateFileAccess(fileId);

      long duration = System.currentTimeMillis() - startTime;
      loadBalancerService.recordRequest(node.getContainerId().toString(), true, duration);

      return ResponseEntity.ok().headers(response.getHeaders()).body(response.getBody());
    } catch (Exception e) {
      log.error(FILE_DOWNLOAD_FAILED_LOG, e);
      if (node != null) {
        long duration = System.currentTimeMillis() - startTime;
        loadBalancerService.recordRequest(node.getContainerId().toString(), false, duration);
      }
      throw new FileDownloadException(DOWNLOAD_FAILED + e.getMessage(), e);
    }
  }

  /**
   * Deletes a file from the storage node containing it.
   *
   * @param fileId The ID of the file to delete
   * @param userId The ID of the user deleting the file
   * @return Success or error response
   */
  @DeleteMapping("/{fileId}")
  public ResponseEntity<Map<String, Object>> deleteFile(
          @PathVariable Long fileId, @RequestHeader(HEADER_USER_ID) Long userId) {
    long startTime = System.currentTimeMillis();
    StorageNode node = null;
    try {
      node = loadBalancerService.getNodeForFile(fileId);
      String deleteUrl = String.format(
              apiPathFormat,
              node.getHostAddress(),
              node.getPort(),
              fileId.toString());

      HttpHeaders headers = new HttpHeaders();
      headers.set(HEADER_USER_ID, userId.toString());

      restTemplate.exchange(deleteUrl, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);

      // Mark file as deleted in database
      loadBalancerService.deleteFileMetadata(fileId);

      long duration = System.currentTimeMillis() - startTime;
      loadBalancerService.recordRequest(node.getContainerId().toString(), true, duration);

      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error(FILE_DELETION_FAILED_LOG, e);
      if (node != null) {
        long duration = System.currentTimeMillis() - startTime;
        loadBalancerService.recordRequest(node.getContainerId().toString(), false, duration);
      }

      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put(KEY_ERROR, DELETION_FAILED);
      errorResponse.put(KEY_MESSAGE, e.getMessage());
      errorResponse.put(KEY_TIMESTAMP, Instant.now());

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }
}