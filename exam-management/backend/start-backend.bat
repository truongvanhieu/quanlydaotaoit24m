@echo off
chcp 65001 >nul
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-backend.ps1"
if errorlevel 1 (
  echo.
  echo [ERROR] Backend da dung. Doc thong bao o tren de sua SQL Server.
)
pause
