#!/bin/bash

# Stop the application in development mode
echo "Stopping application in DEVELOPMENT mode..."

# Kill the Spring Boot process
pkill -f "spring-boot:run"