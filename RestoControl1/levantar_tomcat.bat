@echo off
title Levantar RestoControl en Apache Tomcat
rem Compila el proyecto, arma el WAR para Tomcat 10.1 y lo despliega.
rem Detalle de cada paso: scripts\levantar_tomcat.log
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\levantar_tomcat.ps1"
echo.
pause
