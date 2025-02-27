#!/bin/bash

# Stop the application in production mode
echo "Stopping application in PRODUCTION mode..."

docker-compose -f docker-compose.yml -f docker-compose.prod.yml down