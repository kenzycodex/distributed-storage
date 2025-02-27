@echo off

:: Start the application in DEVELOPMENT mode
echo Starting application in DEVELOPMENT mode...

:: Build the application and Docker images
call mvnw clean install

:: Run the application locally
call mvnw spring-boot:run -Dspring-boot.run.profiles=dev
