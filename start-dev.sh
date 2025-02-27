#!/bin/bash

# Start the application in development mode
echo "Starting application in DEVELOPMENT mode..."

# Build the application and Docker images
mvnw clean package -DskipTests

# Run the application locally
mvnw spring-boot:run -Dspring-boot.run.profiles=dev