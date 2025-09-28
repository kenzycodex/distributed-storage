# Use Eclipse Temurin's JRE Alpine as base image
FROM eclipse-temurin:17-jre-alpine

# Add curl for healthchecks and bash for scripts
RUN apk add --no-cache curl bash

# Create a non-root user and group
RUN addgroup -S storagenode && adduser -S storagenode -G storagenode

# Create necessary directories
WORKDIR /app
RUN mkdir -p /app/storage /logs /tmp
RUN chown -R storagenode:storagenode /app /logs /tmp

# Use build arg to specify JAR file location
ARG JAR_FILE=target/*.jar

# Copy the JAR file
COPY ${JAR_FILE} /app/storage-node.jar
RUN chown storagenode:storagenode /app/storage-node.jar

# Create volumes for persistence
VOLUME /app/storage
VOLUME /tmp
VOLUME /logs

# Set environment variables for JVM tuning
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expose the application port
EXPOSE 8081

# Switch to non-root user for security
USER storagenode

# Health check to ensure the application is running properly
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT:-8081}/api/v1/health/status || exit 1

# Set the entry point
ENTRYPOINT ["java", "-jar", "/app/storage-node.jar"]