@echo off
echo Stopping Chronos Services...
echo.

echo Stopping all Docker services...
docker-compose down

echo.
echo Services stopped successfully!
echo.
echo To start services again, run: start-services.bat
echo.
pause
