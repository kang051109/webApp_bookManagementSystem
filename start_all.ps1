Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Book Management System - Start" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# 1. Check MySQL
$mysql = Get-Process mysqld -ErrorAction SilentlyContinue
if ($mysql) {
    Write-Host "[OK] MySQL is running" -ForegroundColor Green
} else {
    Write-Host "[WARN] MySQL is not running" -ForegroundColor Yellow
    Write-Host "  Start MySQL manually or run: net start MySQL" -ForegroundColor Gray
}

# 2. Start Frontend
$frontend = "$PSScriptRoot\frontend"
if (Test-Path "$frontend\package.json") {
    Write-Host "[START] Starting frontend dev server..." -ForegroundColor Green
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontend'; npm run dev"
} else {
    Write-Host "[FAIL] frontend\package.json not found" -ForegroundColor Red
}

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "  Started!" -ForegroundColor Cyan
Write-Host "  Frontend: http://localhost:5173" -ForegroundColor White
Write-Host "  Backend:  http://localhost:8080/webApp_project_war_exploded/api" -ForegroundColor White
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Make sure:" -ForegroundColor Yellow
Write-Host "  1. MySQL is running" -ForegroundColor Gray
Write-Host "  2. Tomcat is started (via IDEA)" -ForegroundColor Gray
Write-Host "  3. Database initialized (run init_db.bat once)" -ForegroundColor Gray
Write-Host ""

Read-Host "Press Enter to exit"
