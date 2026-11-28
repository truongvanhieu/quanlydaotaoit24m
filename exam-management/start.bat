@echo off
chcp 65001 >nul
title He Thong Quan Ly Thi - Khao Thi
cd /d "%~dp0"
echo ======================================================
echo    HE THONG QUAN LY THI - KHAO THI
echo    Backend: http://localhost:5000
 echo    Frontend: http://localhost:3000
 echo    Kiem tra ket noi SQL Server truoc khi chay backend
 echo ======================================================
echo.
start "Exam Management - Backend" "%~dp0backend\start-backend.bat"
start "Exam Management - Frontend" "%~dp0frontend\start-frontend.bat"
echo Hai cua so da duoc mo. Xem ket qua ket noi SQL o cua so Backend.
echo Khi Backend san sang, mo http://localhost:3000
pause
