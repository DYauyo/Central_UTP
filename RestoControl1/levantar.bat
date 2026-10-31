@echo off
title Levantar RestoControl
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\levantar.ps1"
echo.
pause
