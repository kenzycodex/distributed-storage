#!/bin/bash

# Start the application in production mode (Dockerized)
echo "Starting application in PRODUCTION mode..."

# Build the application and Docker images
mvnw clean package -DskipTests
docker-compose -f docker-compose.yml up --build -d