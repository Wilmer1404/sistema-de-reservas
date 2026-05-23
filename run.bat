@echo off
REM Script para ejecutar SistemaReservas con driver Oracle incluido

cd /d "%~dp0"

REM Ejecutar con ojdbc8.jar en el classpath
java -cp "target/SistemaReservas-1.0-SNAPSHOT.jar;target/ojdbc8.jar" com.spacework.SpaceWorkApplication

pause
