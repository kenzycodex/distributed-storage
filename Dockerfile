# Use Eclipse Temurin's JRE Alpine as base image (smaller than openjdk:slim)
FROM eclipse-temurin:17-jre-alpine

# Add curl for healthchecks and bash for scripts
RUN apk add --no-cache curl bash

# Create a non-root user and group
RUN addgroup -S loadbalancer && adduser -S loadbalancer -G loadbalancer

# Create necessary directories
WORKDIR /app
RUN mkdir -p /logs /tmp
RUN chown -R loadbalancer:loadbalancer /app /logs /tmp

# Use build arg to specify JAR file location
# This allows GitHub Actions to override the location
ARG JAR_FILE=target/*.jar

# Copy the JAR file
COPY ${JAR_FILE} /app/load-balancer.jar
RUN chown loadbalancer:loadbalancer /app/load-balancer.jar

# Create volumes for persistence
VOLUME /tmp
VOLUME /logs

# Set environment variables for JVM tuning
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Expose the application port
EXPOSE 8080

# Switch to non-root user for security
USER loadbalancer

# Health check to ensure the application is running properly
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set the entry point
ENTRYPOINT ["java", "-jar", "/app/load-balancer.jar"]
