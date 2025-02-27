@echo off
REM Load environment variables from .env file
for /F "tokens=*" %%A in (.env) do set %%A

REM Run SonarCloud analysis with explicit parameters
mvn clean verify sonar:sonar -Dsonar.host.url=https://sonarcloud.io -Dsonar.organization=%SONAR_ORGANIZATION% -Dsonar.projectKey=%SONAR_PROJECT_KEY% -Dsonar.token=%SONAR_TOKEN%