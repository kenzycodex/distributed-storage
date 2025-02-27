@echo off

:: Start the application in production mode (Dockerized)
echo Starting application in PRODUCTION mode...

:: Build the application and Docker images
call mvnw clean package -DskipTests

:: Run the application in production mode
call docker-compose -f docker-compose.yml up --build -d