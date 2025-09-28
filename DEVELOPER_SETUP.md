# Developer Setup Guide

This guide provides detailed instructions for setting up, running, testing, and contributing to the DistributedStorage project.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17** or higher
- **Maven 3.6+**
- **Docker** and **Docker Compose**
- **Git**
- **MySQL 8.0+** (for local development without Docker)

## Quick Start with Docker

The easiest way to get the entire system running:

```bash
# Clone the repository
git clone https://github.com/kenzycodex/distributed-storage.git
cd distributed-storage

# Start all services with Docker Compose
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f loadbalancer
docker-compose logs -f storage-node-1
```

### Services Available:

- **Load Balancer**: http://localhost:8080
- **Storage Node 1**: http://localhost:8081
- **Storage Node 2**: http://localhost:8082
- **Storage Node 3**: http://localhost:8083
- **MySQL Database**: localhost:3306
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

## Manual Development Setup

### 1. Database Setup

```bash
# Start MySQL (if not using Docker)
mysql -u root -p

# Create database and user
CREATE DATABASE loadbalancer;
CREATE USER 'loadbalancer'@'localhost' IDENTIFIED BY 'loadbalancer';
GRANT ALL PRIVILEGES ON loadbalancer.* TO 'loadbalancer'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 2. Build the Project

```bash
# Build load balancer
mvn clean package

# Build storage node
cd storage-node
mvn clean package
cd ..
```

### 3. Run Load Balancer

```bash
# Using Maven (development)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Using JAR (production-like)
java -jar target/load-balancer-1.0-SNAPSHOT.jar --spring.profiles.active=dev
```

### 4. Run Storage Nodes

In separate terminals:

```bash
# Storage Node 1
cd storage-node
java -jar target/storage-node-1.0-SNAPSHOT.jar --server.port=8081 --storage.node.name=storage-node-1 --storage.node.host=localhost

# Storage Node 2
java -jar target/storage-node-1.0-SNAPSHOT.jar --server.port=8082 --storage.node.name=storage-node-2 --storage.node.host=localhost

# Storage Node 3
java -jar target/storage-node-1.0-SNAPSHOT.jar --server.port=8083 --storage.node.name=storage-node-3 --storage.node.host=localhost
```

## Testing the System

### 1. Health Check

```bash
# Check load balancer health
curl http://localhost:8080/actuator/health

# Check storage node health
curl http://localhost:8081/api/v1/health
curl http://localhost:8082/api/v1/health
curl http://localhost:8083/api/v1/health
```

### 2. File Operations

```bash
# Upload a file
curl -X POST -H "X-User-ID: 1" -F "file=@test.txt" http://localhost:8080/api/v1/files/upload

# Download a file (replace 123 with actual file ID)
curl -X GET -H "X-User-ID: 1" http://localhost:8080/api/v1/files/123 --output downloaded_file.txt

# Delete a file
curl -X DELETE -H "X-User-ID: 1" http://localhost:8080/api/v1/files/123

# Check if file exists on a storage node
curl http://localhost:8081/api/v1/files/123/exists
```

### 3. Monitoring

```bash
# View system metrics
curl http://localhost:8080/api/v1/metrics/stats

# View node metrics
curl http://localhost:8080/api/v1/metrics/nodes

# View health status
curl http://localhost:8080/api/v1/health/status
```

## Running Tests

```bash
# Run all tests for load balancer
mvn test

# Run tests for storage node
cd storage-node
mvn test

# Run tests with coverage
mvn test jacoco:report

# Run integration tests (when available)
mvn verify
```

## Development Workflow

### 1. Create Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 2. Make Changes

- Follow the implementation phases in `docs/IMPLEMENTATION_PHASES.md`
- Write tests for new functionality
- Update documentation

### 3. Test Changes

```bash
# Build and test
mvn clean package
cd storage-node && mvn clean package && cd ..

# Test with Docker
docker-compose down
docker-compose build
docker-compose up -d

# Verify functionality
./test-script.sh  # Create custom test scripts
```

### 4. Code Quality

```bash
# Format code
mvn spotless:apply

# Run static analysis
mvn sonar:sonar  # If SonarQube is configured

# Check style
mvn checkstyle:check
```

## Debugging

### 1. Enable Debug Logging

Add to `application.yml`:

```yaml
logging:
  level:
    com.loadbalancer: DEBUG
    com.storagenode: DEBUG
```

### 2. Remote Debugging

```bash
# Start with remote debugging enabled
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar target/load-balancer-1.0-SNAPSHOT.jar
```

### 3. Docker Debugging

```bash
# View container logs
docker logs distributed-storage-loadbalancer
docker logs storage-node-1

# Execute commands in container
docker exec -it distributed-storage-loadbalancer bash
docker exec -it storage-node-1 bash

# View container resource usage
docker stats
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `SERVER_PORT` | Application port | `8080` (LB), `8081` (Node) |
| `NODE_NAME` | Storage node name | `storage-node-1` |
| `NODE_HOST` | Storage node hostname | `localhost` |
| `NODE_CAPACITY` | Storage capacity in bytes | `10737418240` (10GB) |
| `LOADBALANCER_HOST` | Load balancer hostname | `localhost` |
| `LOADBALANCER_PORT` | Load balancer port | `8080` |

### Profiles

- **dev**: Development profile with debug logging
- **prod**: Production profile with optimized settings
- **test**: Test profile for running tests

## Troubleshooting

### Common Issues

1. **Port already in use**
   ```bash
   # Find process using port
   netstat -ano | findstr :8080  # Windows
   lsof -i :8080                 # Linux/Mac

   # Kill process
   taskkill /PID <pid> /F        # Windows
   kill -9 <pid>                 # Linux/Mac
   ```

2. **Database connection issues**
   ```bash
   # Check MySQL is running
   mysql -u loadbalancer -p -h localhost

   # Reset database
   DROP DATABASE loadbalancer;
   CREATE DATABASE loadbalancer;
   ```

3. **Storage node not registering**
   ```bash
   # Check network connectivity
   curl http://localhost:8080/actuator/health

   # Check logs
   docker logs storage-node-1
   ```

4. **Docker build failures**
   ```bash
   # Clean Docker cache
   docker system prune -a

   # Rebuild without cache
   docker-compose build --no-cache
   ```

### Getting Help

- Check [Issues](https://github.com/kenzycodex/distributed-storage/issues) for known problems
- Create a new issue with detailed information
- Join our [Discussions](https://github.com/kenzycodex/distributed-storage/discussions)

## Performance Testing

### Load Testing with curl

```bash
# Create test files
echo "Test content" > test1.txt
echo "More test content" > test2.txt

# Upload multiple files
for i in {1..10}; do
  curl -X POST -H "X-User-ID: $i" -F "file=@test1.txt" http://localhost:8080/api/v1/files/upload
done

# Test different load balancing strategies
curl -X GET "http://localhost:8080/api/v1/loadbalancer/node?strategy=round-robin&fileSize=1024"
curl -X GET "http://localhost:8080/api/v1/loadbalancer/node?strategy=least-connection&fileSize=1024"
```

### Using Apache Bench (ab)

```bash
# Install ab (if not available)
# Ubuntu: sudo apt-get install apache2-utils
# Mac: brew install apache2

# Test upload performance
ab -n 100 -c 10 -p test1.txt -T 'multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' http://localhost:8080/api/v1/files/upload
```

## Next Steps

After getting the system running:

1. Review the [Implementation Phases](docs/IMPLEMENTATION_PHASES.md)
2. Check out the [API Documentation](docs/api.md)
3. Read the [Contributing Guidelines](CONTRIBUTING.md)
4. Explore the monitoring dashboards in Grafana

Happy coding! 🚀