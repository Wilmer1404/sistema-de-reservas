# Pruebas del Sistema de Monitoreo

Pasos para verificar que el monitoreo funciona correctamente.

---

## 1. Verificar Logback (Logging)

Iniciar la aplicacion y observar la consola:

```bash
mvn spring-boot:run
```

Generar trafico para ver los logs:
```bash
curl http://localhost:8080/api/espacios
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usuario":"admin","contrasena":"Admin123!"}'
```

Verificar que aparecen mensajes INFO y DEBUG en consola y en `logs/spacework.log`.

---

## 2. Verificar Spring Boot Actuator

Probar cada endpoint:

```bash
# Estado de salud
curl http://localhost:8080/actuator/health

# Debe responder: {"status":"UP", ...}

# Lista de metricas
curl http://localhost:8080/actuator/metrics

# Memoria JVM
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# CPU
curl http://localhost:8080/actuator/metrics/process.cpu.usage

# HTTP Requests
curl http://localhost:8080/actuator/metrics/http.server.requests
```

---

## 3. Verificar el Dashboard Visual

1. Abrir `http://localhost:8080/monitoring.html`
2. Verificar que los indicadores de Aplicacion y Base de Datos muestren "Activo"
3. Esperar 5 segundos y verificar que los valores se actualizan
4. Hacer algunas peticiones a la API y verificar que el contador de HTTP Requests aumenta
5. Verificar que las graficas van agregando puntos

---

## 4. Verificar Alertas

### Probar deteccion de errores 500

Hacer una peticion a un endpoint inexistente:
```bash
curl http://localhost:8080/api/recurso-que-no-existe
```

En el dashboard, la seccion "Alertas Criticas" deberia mostrar el conteo de errores.

### Probar deteccion de errores 4xx

```bash
curl http://localhost:8080/api/espacios/99999
```

Deberia incrementar el contador de "Alertas Warnings".

---

## 5. Verificar Rotacion de Logs

Revisar que el archivo de log se crea en la carpeta `logs/`:

```bash
ls -la logs/
cat logs/spacework.log | tail -50
```

---

## Resultados esperados

| Prueba | Resultado esperado |
|--------|--------------------|
| `actuator/health` | `{"status":"UP"}` |
| `actuator/metrics` | Lista con 20+ metricas |
| `monitoring.html` | Dashboard con datos reales |
| Logs | Archivo creado en `logs/spacework.log` |
| Alertas | Contadores actualizados con errores reales |
