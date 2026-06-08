@echo off
chcp 65001 >nul
title Book Management System

echo ========================================
echo     Book Management System - Quick Start
echo ========================================
echo.

echo [1/2] Starting frontend dev server...
cd /d "%~dp0frontend"
if exist "package.json" (
    start "Vue Frontend" cmd /k "echo Frontend running at http://localhost:5173 && npm run dev"
    echo   OK - Frontend started
) else (
    echo   FAIL - frontend/package.json not found
)
cd /d "%~dp0"

echo.
echo ========================================
echo     Frontend: http://localhost:5173
echo     Backend:  http://localhost:8080/webApp_project_war_exploded/api
echo.
echo     Make sure:
echo     1. MySQL is running
echo     2. Database has been initialized (run init_db.bat first)
echo     3. Tomcat is started with project deployed
echo ========================================
echo.
pause
