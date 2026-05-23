#!/usr/bin/env bash
# CRUD Complete Testing - SpaceWork System
# Simple curl-based testing

BASE_URL="http://localhost:8080/api"
TOKEN=""

echo "=========================================="
echo "SPACEWORK - PRUEBAS CRUD COMPLETAS"
echo "=========================================="

# 1. LOGIN ADMIN
echo -e "\n[1] AUTENTICACIÓN - Login Admin"
LOGIN_RES=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@spacework.com","password":"admin123"}')
echo "$LOGIN_RES" | jq '.'

TOKEN=$(echo "$LOGIN_RES" | jq -r '.data.token // empty')
if [ -z "$TOKEN" ]; then
  echo "ERROR: No se obtuvo token"
  exit 1
fi
echo "TOKEN: $TOKEN"

# 2. CLIENTES CRUD
echo -e "\n=========================================="
echo "[2] CLIENTES CRUD"
echo "=========================================="

echo -e "\n[2.1] GET ALL CLIENTES"
curl -s -X GET "$BASE_URL/clientes" \
  -H "Authorization: Bearer $TOKEN" | jq '.data | length'

echo -e "\n[2.2] CREATE CLIENTE"
curl -s -X POST "$BASE_URL/clientes" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "nombre":"TestCliente",
    "apellido":"Test",
    "dni":"99999999",
    "email":"test@test.com",
    "telefono":"1234567890"
  }' | jq '.data | {idCliente, nombre, email}'

# 3. ESPACIOS CRUD
echo -e "\n=========================================="
echo "[3] ESPACIOS CRUD"
echo "=========================================="

echo -e "\n[3.1] GET ALL ESPACIOS"
ESPACIOS=$(curl -s -X GET "$BASE_URL/espacios" \
  -H "Authorization: Bearer $TOKEN")
echo "$ESPACIOS" | jq '.data | length'
ESPACIO_ID=$(echo "$ESPACIOS" | jq '.data[0].idEspacio' 2>/dev/null || echo "1")

echo -e "\n[3.2] CREATE ESPACIO"
ESPACIO_RES=$(curl -s -X POST "$BASE_URL/espacios" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "nombre":"SpaceTest",
    "tipo":"SALA_REUNION",
    "capacidad":10,
    "ubicacion":"Piso 1",
    "precioPorHora":50.0
  }')
echo "$ESPACIO_RES" | jq '.data | {idEspacio, nombre, tipo, precioPorHora}'
NEW_ESPACIO_ID=$(echo "$ESPACIO_RES" | jq '.data.idEspacio // empty')

# 4. RESERVAS CRUD
echo -e "\n=========================================="
echo "[4] RESERVAS CRUD"
echo "=========================================="

echo -e "\n[4.1] GET ALL RESERVAS"
RESERVAS=$(curl -s -X GET "$BASE_URL/reservas" \
  -H "Authorization: Bearer $TOKEN")
echo "$RESERVAS" | jq '.data | length'

echo -e "\n[4.2] CREATE RESERVA"
FECHA_INI=$(date -u +"%Y-%m-%d %H:%M")
FECHA_FIN=$(date -u -d "+2 hours" +"%Y-%m-%d %H:%M")
RESERVA_RES=$(curl -s -X POST "$BASE_URL/reservas" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"idCliente\":1,
    \"idEspacio\":$ESPACIO_ID,
    \"fechaInicio\":\"$FECHA_INI\",
    \"fechaFin\":\"$FECHA_FIN\"
  }")
echo "$RESERVA_RES" | jq '.message // .data'

# 5. DESCUENTOS CRUD
echo -e "\n=========================================="
echo "[5] DESCUENTOS CRUD"
echo "=========================================="

echo -e "\n[5.1] GET ALL DESCUENTOS"
DESCUENTOS=$(curl -s -X GET "$BASE_URL/descuentos" \
  -H "Authorization: Bearer $TOKEN")
echo "$DESCUENTOS" | jq '.data | length'

echo -e "\n[5.2] CREATE DESCUENTO"
DESC_RES=$(curl -s -X POST "$BASE_URL/descuentos" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "codigo":"TEST2024",
    "descripcion":"Test Descuento",
    "porcentaje":15,
    "montoMinimo":100,
    "usosMaximos":5,
    "fechaInicio":"2026-05-20",
    "fechaFin":"2026-06-20",
    "estado":"ACTIVO"
  }')
echo "$DESC_RES" | jq '.data | {idDescuento, codigo, porcentaje} // .message'
DESC_ID=$(echo "$DESC_RES" | jq '.data.idDescuento // empty')

if [ -n "$DESC_ID" ]; then
  echo -e "\n[5.3] UPDATE DESCUENTO ($DESC_ID)"
  curl -s -X PUT "$BASE_URL/descuentos/$DESC_ID" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
      "codigo":"TEST2024UPD",
      "descripcion":"Updated",
      "porcentaje":20
    }' | jq '.message // .data'
  
  echo -e "\n[5.4] DELETE DESCUENTO ($DESC_ID)"
  curl -s -X DELETE "$BASE_URL/descuentos/$DESC_ID" \
    -H "Authorization: Bearer $TOKEN" | jq '.message // .data'
fi

# 6. EVALUACIONES CRUD
echo -e "\n=========================================="
echo "[6] EVALUACIONES CRUD"
echo "=========================================="

echo -e "\n[6.1] GET ALL EVALUACIONES"
curl -s -X GET "$BASE_URL/evaluaciones" \
  -H "Authorization: Bearer $TOKEN" | jq '.data | length'

# 7. PAGOS CRUD
echo -e "\n=========================================="
echo "[7] PAGOS CRUD"
echo "=========================================="

echo -e "\n[7.1] GET ALL PAGOS"
PAGOS=$(curl -s -X GET "$BASE_URL/pagos" \
  -H "Authorization: Bearer $TOKEN")
echo "$PAGOS" | jq '.data | length'

echo -e "\n=========================================="
echo "✅ PRUEBAS COMPLETADAS"
echo "=========================================="
