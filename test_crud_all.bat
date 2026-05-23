@echo off
setlocal enabledelayedexpansion

echo =========================================
echo SPACEWORK - CRUD COMPLETE TEST
echo =========================================

REM Test 1: Login
echo.
echo [1] LOGIN ADMIN
curl -s -X POST "http://localhost:8080/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"admin@spacework.com\",\"password\":\"admin123\"}" > token.json

REM Extract token with PowerShell
for /f "delims=" %%i in ('powershell -Command "(Get-Content token.json | ConvertFrom-Json).data.token"') do set TOKEN=%%i
echo TOKEN: %TOKEN:~0,20%...

REM Test 2: GET CLIENTES
echo.
echo [2] CLIENTES - GET ALL
curl -s -X GET "http://localhost:8080/api/clientes" ^
  -H "Authorization: Bearer %TOKEN%" > test_output.json
for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).count"') do set COUNT=%%i
echo COUNT: !COUNT!

REM Test 3: GET ESPACIOS  
echo.
echo [3] ESPACIOS - GET ALL
curl -s -X GET "http://localhost:8080/api/espacios" ^
  -H "Authorization: Bearer %TOKEN%" > test_output.json
for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).count"') do set COUNT=%%i
echo COUNT: !COUNT!

REM Test 4: GET RESERVAS
echo.
echo [4] RESERVAS - GET ALL
curl -s -X GET "http://localhost:8080/api/reservas" ^
  -H "Authorization: Bearer %TOKEN%" > test_output.json
for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).count"') do set COUNT=%%i
echo COUNT: !COUNT!

REM Test 5: GET DESCUENTOS
echo.
echo [5] DESCUENTOS - GET ALL
curl -s -X GET "http://localhost:8080/api/descuentos" ^
  -H "Authorization: Bearer %TOKEN%" > test_output.json
for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).count"') do set COUNT=%%i
echo COUNT: !COUNT!

REM Test 6: GET EVALUACIONES
echo.
echo [6] EVALUACIONES - GET ALL
curl -s -X GET "http://localhost:8080/api/evaluaciones" ^
  -H "Authorization: Bearer %TOKEN%" > test_output.json
for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).data | measure -Line | select -exp Lines"') do set COUNT=%%i
echo COUNT: !COUNT!

REM Test 7: GET PAGOS
echo.
echo [7] PAGOS - GET ALL
curl -s -X GET "http://localhost:8080/api/pagos" ^
  -H "Authorization: Bearer %TOKEN%" > test_output.json
for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).count"') do set COUNT=%%i
echo COUNT: !COUNT!

echo.
echo =========================================
echo CREATE TESTS
echo =========================================

REM Test 8: CREATE CLIENTE
echo.
echo [8] CLIENTE - CREATE
curl -s -X POST "http://localhost:8080/api/clientes" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"nombre\":\"TestCli\",\"apellido\":\"Test\",\"dni\":\"88888888\",\"email\":\"test@test.com\",\"telefono\":\"123456\"}" > test_output.json
for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).data.idCliente"') do set NEW_CLI=%%i
echo NEW ID: !NEW_CLI!

REM Test 9: CREATE ESPACIO
echo.
echo [9] ESPACIO - CREATE
curl -s -X POST "http://localhost:8080/api/espacios" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"nombre\":\"EspacioTest\",\"tipo\":\"OFICINA\",\"capacidad\":5,\"ubicacion\":\"Piso1\",\"precioPorHora\":75.0}" > test_output.json
for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).data.idEspacio"') do set NEW_ESP=%%i
echo NEW ID: !NEW_ESP!

REM Test 10: CREATE DESCUENTO
echo.
echo [10] DESCUENTO - CREATE
curl -s -X POST "http://localhost:8080/api/descuentos" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"codigo\":\"TEST123\",\"descripcion\":\"Test\",\"porcentaje\":10,\"montoMinimo\":50,\"usosMaximos\":5,\"fechaInicio\":\"2026-05-20\",\"fechaFin\":\"2026-06-20\",\"estado\":\"ACTIVO\"}" > test_output.json
for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).data.idDescuento"') do set NEW_DESC=%%i
echo NEW ID: !NEW_DESC!

echo.
echo =========================================
echo UPDATE TESTS
echo =========================================

REM Test 11: UPDATE CLIENTE
if not "!NEW_CLI!"=="" (
  echo.
  echo [11] CLIENTE - UPDATE (!NEW_CLI!)
  curl -s -X PUT "http://localhost:8080/api/clientes/!NEW_CLI!" ^
    -H "Content-Type: application/json" ^
    -H "Authorization: Bearer %TOKEN%" ^
    -d "{\"nombre\":\"CliUpdated\",\"apellido\":\"Upd\",\"dni\":\"77777777\",\"email\":\"upd@test.com\",\"telefono\":\"999999\"}" > test_output.json
  for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).message"') do set MSG=%%i
  echo RESULT: !MSG!
)

REM Test 12: UPDATE ESPACIO
if not "!NEW_ESP!"=="" (
  echo.
  echo [12] ESPACIO - UPDATE (!NEW_ESP!)
  curl -s -X PUT "http://localhost:8080/api/espacios/!NEW_ESP!" ^
    -H "Content-Type: application/json" ^
    -H "Authorization: Bearer %TOKEN%" ^
    -d "{\"nombre\":\"EspacioUpd\",\"tipo\":\"COWORKING\",\"capacidad\":15,\"ubicacion\":\"Piso2\",\"precioPorHora\":100.0}" > test_output.json
  for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).message"') do set MSG=%%i
  echo RESULT: !MSG!
)

REM Test 13: UPDATE DESCUENTO
if not "!NEW_DESC!"=="" (
  echo.
  echo [13] DESCUENTO - UPDATE (!NEW_DESC!)
  curl -s -X PUT "http://localhost:8080/api/descuentos/!NEW_DESC!" ^
    -H "Content-Type: application/json" ^
    -H "Authorization: Bearer %TOKEN%" ^
    -d "{\"codigo\":\"TEST456\",\"descripcion\":\"Updated\",\"porcentaje\":20,\"montoMinimo\":100,\"usosMaximos\":10}" > test_output.json
  for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).message"') do set MSG=%%i
  echo RESULT: !MSG!
)

echo.
echo =========================================
echo DELETE TESTS
echo =========================================

REM Test 14: DELETE CLIENTE
if not "!NEW_CLI!"=="" (
  echo.
  echo [14] CLIENTE - DELETE (!NEW_CLI!)
  curl -s -X DELETE "http://localhost:8080/api/clientes/!NEW_CLI!" ^
    -H "Authorization: Bearer %TOKEN%" > test_output.json
  for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).message"') do set MSG=%%i
  echo RESULT: !MSG!
)

REM Test 15: DELETE DESCUENTO
if not "!NEW_DESC!"=="" (
  echo.
  echo [15] DESCUENTO - DELETE/DESACTIVAR (!NEW_DESC!)
  curl -s -X DELETE "http://localhost:8080/api/descuentos/!NEW_DESC!" ^
    -H "Authorization: Bearer %TOKEN%" > test_output.json
  for /f "delims=" %%i in ('powershell -Command "(Get-Content test_output.json | ConvertFrom-Json).message"') do set MSG=%%i
  echo RESULT: !MSG!
)

echo.
echo =========================================
echo TEST COMPLETE
echo =========================================

del token.json test_output.json 2>nul
pause
