#!/bin/bash
# Load environment variables from .env file
export $(grep -v '^#' .env | xargs)

# Run SonarCloud analysis
mvn clean verify sonar:sonar