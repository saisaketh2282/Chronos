@echo off
echo Starting Chronos Services with Docker...
echo.

echo Checking Docker Desktop...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Docker is not running or not installed!
    echo Please start Docker Desktop and try again.
    pause
    exit /b 1
)

echo Docker is running. Starting services...
echo.

echo Starting PostgreSQL, Kafka, and Zookeeper...
docker-compose up -d postgres zookeeper kafka kafka-topics

echo.
echo Waiting for services to start...
timeout /t 30 /nobreak >nul

echo.
echo Starting Kafka UI...
docker-compose up -d kafka-ui

echo.
echo Checking service status...
docker-compose ps

echo.
echo ========================================
echo Services Status:
echo ========================================
echo PostgreSQL: localhost:5432
echo Kafka: localhost:9092
echo Kafka UI: http://localhost:8080
echo ========================================
echo.
echo To view logs: docker-compose logs
echo To stop services: docker-compose down
echo.
echo Services are starting up. Please wait 1-2 minutes for all services to be ready.
echo.
pause
