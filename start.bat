@echo off
setlocal

echo ╔══════════════════════════════════════════╗
echo ║   SPACEWORK - Iniciando Servidor Web     ║
echo ╚══════════════════════════════════════════╝

cd /d "%~dp0"

REM Verificar si ya hay algo corriendo en el puerto 8080
netstat -ano | findstr ":8080" | findstr "LISTENING" >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [WARN] Puerto 8080 ya en uso. Deteniendo proceso anterior...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do taskkill /PID %%a /F >nul 2>&1
    timeout /t 2 >nul
)

echo [INFO] Compilando...
call mvn clean package -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Fallo en compilacion. Usando clases existentes.
)

echo [INFO] Iniciando servidor en http://localhost:8080
echo [INFO] Presiona Ctrl+C para detener.
echo.

java -jar target\SistemaReservas-1.0-SNAPSHOT.jar

if %ERRORLEVEL% NEQ 0 (
    echo [WARN] JAR sin manifest. Usando classpath manual...
    java -cp "target\classes;lib\*" com.spacework.SpaceWorkApplication
)

pause
