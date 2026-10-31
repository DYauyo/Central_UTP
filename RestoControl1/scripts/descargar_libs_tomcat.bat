@echo off
title Descargar librerias JSF/CDI para Tomcat
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0descargar_libs_tomcat.ps1"
timeout /t 5
