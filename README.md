# DistributedStorage

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.0-green.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)

A robust and scalable Java distributed storage system with an intelligent load balancer for managing file storage across multiple nodes.

## Overview

DistributedStorage is a high-performance distributed file storage system designed for reliability, scalability, and fault tolerance. The system dynamically distributes files across multiple storage nodes using advanced load balancing strategies while providing a unified API for file operations.

### Key Features

- **Dynamic Load Balancing**: Multiple algorithms including round-robin, least-connection, and weighted strategies to optimize resource utilization
- **Automatic Node Health Monitoring**: Continuous health checks with automatic failover for fault tolerance
- **Real-time Metrics**: Comprehensive performance tracking for the entire system and individual nodes
- **Horizontal Scalability**: Seamlessly add or remove storage nodes without downtime
- **File Redundancy**: Optional file replication for enhanced data durability
- **RESTful API**: Simple yet powerful API for file operations
- **Containerization Support**: Designed to work efficiently in Docker/Kubernetes environments

## Architecture

DistributedStorage consists of three main components:

1. **Load Balancer**: Receives client requests and intelligently routes them to the appropriate storage nodes
2. **Storage Nodes**: Independent servers that store and manage files
3. **Metadata Service**: Tracks file locations and system configuration

![Architecture Diagram](docs/images/architecture.png)

## Getting Started

### Prerequisites

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher
- Docker (optional, for containerized deployment)

### Installation

#### Option 1: Manual Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/kenzycodex/distributed-storage.git
   cd distributed-storage
   ```

2. Configure the database:
   ```bash
   # Create MySQL database
   mysql -u root -p
   CREATE DATABASE loadbalancer;
   CREATE USER 'loadbalancer'@'localhost' IDENTIFIED BY 'loadbalancer';
   GRANT ALL PRIVILEGES ON loadbalancer.* TO 'loadbalancer'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. Build the project:
   ```bash
   mvn clean package
   ```

4. Start the load balancer:
   ```bash
   java -jar target/loadbalancer.jar
   ```

5. Start storage nodes (in separate terminals):
   ```bash
   java -jar target/storage-node.jar --server.port=8081
   java -jar target/storage-node.jar --server.port=8082
   ```

#### Option 2: Docker Deployment

1. Build Docker images:
   ```bash
   docker-compose build
   ```

2. Start the services:
   ```bash
   docker-compose up -d
   ```

### Usage

#### File Upload

```bash
curl -X POST -H "X-User-ID: 1" -F "file=@/path/to/file.txt" http://localhost:8080/api/v1/files/upload
```

#### File Download

```bash
curl -X GET -H "X-User-ID: 1" http://localhost:8080/api/v1/files/123 --output file.txt
```

#### File Deletion

```bash
curl -X DELETE -H "X-User-ID: 1" http://localhost:8080/api/v1/files/123
```

### Configuration

All configuration options are available in `application.yml`. Key configuration sections include:

- **Load Balancing Strategies**: Configure which algorithms are available and set the default
- **Health Check Parameters**: Adjust frequency and thresholds for node health monitoring
- **Metrics Collection**: Configure retention policies and collection intervals

See [Configuration Guide](docs/configuration.md) for detailed information.

## API Documentation

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/v1/files/upload` | POST | Upload a file |
| `/api/v1/files/{fileId}` | GET | Download a file |
| `/api/v1/files/{fileId}` | DELETE | Delete a file |
| `/api/v1/nodes/register` | POST | Register a new storage node |
| `/api/v1/nodes/heartbeat` | POST | Node heartbeat update |
| `/api/v1/health/status` | GET | Get health status of all nodes |
| `/api/v1/metrics/stats` | GET | Get system metrics |

For detailed API documentation, see [API Guide](docs/api.md).

## Load Balancing Strategies

DistributedStorage supports multiple load balancing strategies:

- **Round Robin**: Distributes requests evenly across all available nodes
- **Least Connection**: Routes requests to the node with the fewest active connections
- **Shortest Job Next**: Prioritizes nodes for smaller file operations
- **Weighted Round Robin**: Distributes load based on node capacity
- **First Come First Serve**: Simple queue-based allocation

## Performance Metrics

The system collects comprehensive metrics including:

- Request counts (total, successful, failed)
- Response times (average, 95th percentile, 99th percentile)
- Node-specific statistics
- Connection counts per node
- Storage utilization

Metrics are accessible via REST endpoints and can be integrated with monitoring systems like Prometheus and Grafana.

## Development

### Project Structure

```
distributed-storage/
├── src/
│   ├── main/
│   │   ├── java/com/loadbalancer/
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── exception/        # Custom exceptions
│   │   │   ├── model/            # Data models
│   │   │   ├── repository/       # Data access layer
│   │   │   ├── service/          # Business logic
│   │   │   ├── strategy/         # Load balancing algorithms
│   │   │   └── LoadBalancerApplication.java
│   │   └── resources/
│   │       ├── application.yml   # Common configuration
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/                     # Test classes
├── docker/                       # Docker configurations
├── docs/                         # Documentation
└── scripts/                      # Utility scripts
```

### Building from Source

```bash
# Clone the repository
git clone https://github.com/kenzycodex/distributed-storage.git
cd distributed-storage

# Build with Maven
mvn clean package

# Run tests
mvn test
```

### Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Spring Boot team for the excellent framework
- The open source community for inspiration and support

## Contact

For questions or support, please open an issue on the GitHub repository.