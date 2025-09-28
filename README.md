# DistributedStorage

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.0-green.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)
![Build Status](https://img.shields.io/github/workflow/status/kenzycodex/distributed-storage/Java%20CI%20with%20Maven/main)
![Issues](https://img.shields.io/github/issues/kenzycodex/distributed-storage)
![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)

A robust and scalable Java distributed storage system with an intelligent load balancer for managing file storage across multiple nodes.

## Overview

DistributedStorage is a high-performance distributed file storage system designed for reliability, scalability, and fault tolerance. The system dynamically distributes files across multiple storage nodes using advanced load balancing strategies while providing a unified API for file operations.

### Key Features

- **Dynamic Load Balancing**: Multiple algorithms including round-robin, least-connection, and weighted strategies
- **Automatic Health Monitoring**: Continuous health checks with automatic failover
- **Real-time Metrics**: Comprehensive performance tracking for system and nodes
- **Horizontal Scalability**: Seamlessly add or remove storage nodes without downtime
- **File Redundancy**: Optional file replication for enhanced data durability
- **RESTful API**: Simple yet powerful API for file operations
- **Containerization Support**: Designed for Docker/Kubernetes environments

## Architecture

DistributedStorage consists of three main components:

1. **Load Balancer**: Receives client requests and intelligently routes them to appropriate storage nodes
2. **Storage Nodes**: Independent servers that store and manage files
3. **Metadata Service**: Tracks file locations and system configuration

<div align="center">
  <img src="docs/images/architecture-diagram.png" alt="Architecture Diagram" width="700"/>
</div>

## 🚀 Quick Start

### Prerequisites

- **Java 17+**
- **Docker & Docker Compose**
- **Maven 3.6+**

### Option 1: Docker (Recommended)

```bash
# Clone the repository
git clone https://github.com/kenzycodex/distributed-storage.git
cd distributed-storage

# Start all services
docker-compose up -d

# Check service status
docker-compose ps
```

**Services Available:**
- Load Balancer: http://localhost:8080
- Storage Nodes: http://localhost:8081, :8082, :8083
- Grafana Dashboard: http://localhost:3000 (admin/admin)
- Prometheus: http://localhost:9090

### Option 2: Manual Setup

```bash
# 1. Clone and build
git clone https://github.com/kenzycodex/distributed-storage.git
cd distributed-storage
mvn clean package
cd storage-node && mvn clean package && cd ..

# 2. Setup MySQL
mysql -u root -p
CREATE DATABASE loadbalancer;
CREATE USER 'loadbalancer'@'localhost' IDENTIFIED BY 'loadbalancer';
GRANT ALL PRIVILEGES ON loadbalancer.* TO 'loadbalancer'@'localhost';

# 3. Start Load Balancer
java -jar target/load-balancer-1.0-SNAPSHOT.jar

# 4. Start Storage Nodes (in separate terminals)
cd storage-node
java -jar target/storage-node-1.0-SNAPSHOT.jar --server.port=8081 --storage.node.name=node-1
java -jar target/storage-node-1.0-SNAPSHOT.jar --server.port=8082 --storage.node.name=node-2
java -jar target/storage-node-1.0-SNAPSHOT.jar --server.port=8083 --storage.node.name=node-3
```

## 📝 Usage Examples

```bash
# Upload a file
curl -X POST -H "X-User-ID: 1" -F "file=@test.txt" http://localhost:8080/api/v1/files/upload

# Download a file (replace 123 with actual file ID from upload response)
curl -X GET -H "X-User-ID: 1" http://localhost:8080/api/v1/files/123 --output downloaded.txt

# Delete a file
curl -X DELETE -H "X-User-ID: 1" http://localhost:8080/api/v1/files/123

# Check system health
curl http://localhost:8080/actuator/health

# View metrics
curl http://localhost:8080/api/v1/metrics/stats
```

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [**Developer Setup**](DEVELOPER_SETUP.md) | Complete setup, testing, and development guide |
| [**API Reference**](docs/api.md) | Comprehensive API documentation |
| [**Configuration**](docs/configuration.md) | Configuration options and environment variables |
| [**Load Balancing**](docs/load-balancing-strategies.md) | Available load balancing algorithms |
| [**Implementation Phases**](docs/IMPLEMENTATION_PHASES.md) | Development roadmap and phases |
| [**Contributing**](CONTRIBUTING.md) | How to contribute to the project |

## ⚡ Current Status

✅ **Phase 1 Complete**: Core storage node implementation
🔄 **Phase 2 In Progress**: File metadata persistence
📋 **Next**: Enhanced node management and comprehensive testing

**What's Working:**
- ✅ Complete load balancer with 5 strategies
- ✅ Storage nodes with file operations
- ✅ Automatic node registration and heartbeat
- ✅ Health monitoring and metrics
- ✅ Docker deployment
- ✅ RESTful API

**Production Ready Features:**
- Load balancing across multiple storage nodes
- File upload, download, and deletion
- System monitoring and health checks
- Containerized deployment

## Monitoring & Metrics

The system collects comprehensive metrics including:

- Request counts (total, successful, failed)
- Response times (average, 95th percentile, 99th percentile)
- Node-specific statistics
- Connection counts per node
- Storage utilization

Metrics are accessible via REST endpoints and can be integrated with Prometheus and Grafana using the provided configurations.

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
│   │   └── resources/            # Configuration files
│   └── test/                     # Test classes
├── .github/                      # GitHub integration
│   ├── ISSUE_TEMPLATE/           # Issue templates
│   └── workflows/                # CI/CD workflows
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

## Contributing

We welcome contributions from the community! Please check our [Contributing Guidelines](CONTRIBUTING.md) before submitting issues or pull requests.

### How to Contribute

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to your branch
5. Create a pull request

For more details, please read our [Contributing Guidelines](CONTRIBUTING.md).

### Code of Conduct

Please note that this project adheres to a [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

## Community

We use GitHub Discussions to connect with our users and contributors. Check out our [Discussions page](https://github.com/kenzycodex/distributed-storage/discussions) to ask questions, share ideas, or get help. See our [discussion guidelines](DISCUSSIONS.md) for more information.

### Reporting Security Issues

For security-related issues, please refer to our [Security Policy](SECURITY.md) instead of filing a public issue.

## Versioning & Changelog

This project follows [Semantic Versioning](https://semver.org/).

See our [Changelog](CHANGELOG.md) for a detailed history of changes.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

We're grateful to all [contributors](ACKNOWLEDGEMENTS.md) who have helped shape this project.

## Contact & Support

- **Issues**: For bugs and feature requests, please [create an issue](https://github.com/kenzycodex/distributed-storage/issues/new/choose)
- **Discussions**: For questions and general discussion, use [GitHub Discussions](https://github.com/kenzycodex/distributed-storage/discussions)

For other inquiries, please open an issue on the GitHub repository.