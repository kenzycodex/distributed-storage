# Use OpenJDK 17 as the base image
FROM openjdk:17-slim

# Install curl for healthcheck
RUN apt-get update && \
apt-get install -y curl && \
apt-get clean && \
rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the packaged JAR file into the container
COPY target/*.jar /app/load-balancer.jar

# Expose the load balancer port
EXPOSE 8080

# Command to run the load balancer
CMD ["java", "-jar", "load-balancer.jar"]