# API Documentation

This document describes the REST APIs provided by DistributedStorage.

## Base URL

All API endpoints are relative to the base URL:

```
http://<host>:<port>/api/v1
```

## Authentication

Authentication is performed using the `X-User-ID` header, which should contain the unique identifier for the user making the request.

Example:

```
X-User-ID: 123
```

## File Operations

### Upload a File

Uploads a file to the distributed storage system.

**Endpoint:** `POST /files/upload`

**Headers:**
- `X-User-ID`: User identifier (required)
- `Content-Type`: `multipart/form-data`

**Request Parameters:**
- `file`: The file to upload (required)

**Response:**
```json
{
  "fileId": 123,
  "fileName": "example.pdf",
  "fileSize": 1024,
  "contentType": "application/pdf",
  "timestamp": "2025-02-27T14:30:45.123Z",
  "nodeId": "container-456"
}
```

**Status Codes:**
- `200 OK`: File uploaded successfully
- `400 Bad Request`: Invalid request
- `500 Internal Server Error`: Server error

### Download a File

Downloads a file from the storage system.

**Endpoint:** `GET /files/{fileId}`

**Path Parameters:**
- `fileId`: Unique identifier of the file (required)

**Headers:**
- `X-User-ID`: User identifier (required)

**Response:**
- File content with appropriate Content-Type header

**Status Codes:**
- `200 OK`: File downloaded successfully
- `404 Not Found`: File not found
- `500 Internal Server Error`: Server error

### Delete a File

Deletes a file from the storage system.

**Endpoint:** `DELETE /files/{fileId}`

**Path Parameters:**
- `fileId`: Unique identifier of the file (required)

**Headers:**
- `X-User-ID`: User identifier (required)

**Status Codes:**
- `204 No Content`: File deleted successfully
- `404 Not Found`: File not found
- `500 Internal Server Error`: Server error

## Node Management

### Register a Node

Registers a new storage node with the system.

**Endpoint:** `POST /nodes/register`

**Request Body:**
```json
{
  "containerId": 12345,
  "containerName": "storage-node-1",
  "hostAddress": "192.168.1.100",
  "port": 8081,
  "capacity": 1073741824
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Node registered successfully",
  "nodeId": 12345
}
```

**Status Codes:**
- `200 OK`: Node registered successfully
- `400 Bad Request`: Invalid request
- `500 Internal Server Error`: Server error

### Node Heartbeat

Updates the status and metrics of a registered node.

**Endpoint:** `POST /nodes/heartbeat`

**Request Body:**
```json
{
  "containerId": 12345,
  "status": "ACTIVE",
  "usedSpace": 536870912
}
```

**Response:**
```json
{
  "status": "success",
  "message": "Heartbeat processed successfully"
}
```

**Status Codes:**
- `200 OK`: Heartbeat processed successfully
- `400 Bad Request`: Invalid request
- `500 Internal Server Error`: Server error

## Health Check

### Get Node Health Status

Retrieves the health status of a specific node.

**Endpoint:** `GET /health/status/{nodeId}`

**Path Parameters:**
- `nodeId`: Node identifier (required)

**Response:**
```json
{
  "nodeId": "12345",
  "healthy": true,
  "lastChecked": "2025-02-27T14:35:22.456Z",
  "responseTime": 45,
  "statusMessage": "OK"
}
```

**Status Codes:**
- `200 OK`: Health status retrieved successfully
- `404 Not Found`: Node not found
- `500 Internal Server Error`: Server error

### Get All Nodes Health Status

Retrieves the health status of all active nodes.

**Endpoint:** `GET /health/status`

**Response:**
```json
{
  "12345": {
    "nodeId": "12345",
    "healthy": true,
    "lastChecked": "2025-02-27T14:35:22.456Z",
    "responseTime": 45,
    "statusMessage": "OK"
  },
  "67890": {
    "nodeId": "67890",
    "healthy": false,
    "lastChecked": "2025-02-27T14:35:10.123Z",
    "responseTime": 1200,
    "statusMessage": "Connection timeout"
  }
}
```

**Status Codes:**
- `200 OK`: Health statuses retrieved successfully
- `500 Internal Server Error`: Server error

## Metrics

### Get System Metrics

Retrieves system-wide performance metrics.

**Endpoint:** `GET /metrics/stats`

**Response:**
```json
{
  "totalRequests": 15240,
  "successfulRequests": 15180,
  "failedRequests": 60,
  "averageResponseTime": 156.7,
  "p95ResponseTime": 325.4,
  "p99ResponseTime": 496.2
}
```

**Status Codes:**
- `200 OK`: Metrics retrieved successfully
- `500 Internal Server Error`: Server error

### Get Node Metrics

Retrieves performance metrics for a specific node.

**Endpoint:** `GET /metrics/node/{nodeId}`

**Path Parameters:**
- `nodeId`: Node identifier (required)

**Response:**
```json
{
  "totalRequests": 5240,
  "successfulRequests": 5228,
  "failedRequests": 12,
  "averageResponseTime": 145.3,
  "p95ResponseTime": 312.8,
  "p99ResponseTime": 476.5
}
```

**Status Codes:**
- `200 OK`: Metrics retrieved successfully
- `404 Not Found`: Node not found
- `500 Internal Server Error`: Server error

### Get All Nodes Metrics

Retrieves performance metrics for all active nodes.

**Endpoint:** `GET /metrics/nodes`

**Response:**
```json
{
  "12345": {
    "totalRequests": 5240,
    "successfulRequests": 5228,
    "failedRequests": 12,
    "averageResponseTime": 145.3,
    "p95ResponseTime": 312.8,
    "p99ResponseTime": 476.5
  },
  "67890": {
    "totalRequests": 6120,
    "successfulRequests": 6105,
    "failedRequests": 15,
    "averageResponseTime": 162.1,
    "p95ResponseTime": 338.5,
    "p99ResponseTime": 510.7
  }
}
```

**Status Codes:**
- `200 OK`: Metrics retrieved successfully
- `500 Internal Server Error`: Server error

### Get System Summary

Retrieves a summary of the entire system's state and performance.

**Endpoint:** `GET /metrics/summary`

**Response:**
```json
{
  "globalStats": {
    "totalRequests": 15240,
    "successfulRequests": 15180,
    "failedRequests": 60,
    "averageResponseTime": 156.7,
    "p95ResponseTime": 325.4,
    "p99ResponseTime": 496.2
  },
  "activeNodes": 8,
  "totalRequests": 15240,
  "avgResponseTime": 156.7
}
```

**Status Codes:**
- `200 OK`: Summary retrieved successfully
- `500 Internal Server Error`: Server error

## Load Balancer

### Get Node for Request

Selects an appropriate node for a file operation based on the specified strategy.

**Endpoint:** `GET /loadbalancer/node`

**Query Parameters:**
- `strategy`: Load balancing strategy to use (optional)
- `fileSize`: Size of the file in bytes (required)

**Response:**
```json
{
  "containerId": 12345,
  "containerName": "storage-node-1",
  "hostAddress": "192.168.1.100",
  "port": 8081,
  "capacity": 1073741824,
  "usedSpace": 536870912,
  "status": "ACTIVE",
  "createdAt": "2025-01-15T10:30:00.000Z"
}
```

**Status Codes:**
- `200 OK`: Node selected successfully
- `404 Not Found`: No suitable node found
- `500 Internal Server Error`: Server error

### Complete Request

Notifies the load balancer that a request has been completed.

**Endpoint:** `POST /loadbalancer/node/{nodeId}/complete`

**Path Parameters:**
- `nodeId`: Node identifier (required)

**Status Codes:**
- `200 OK`: Notification successful
- `500 Internal Server Error`: Server error