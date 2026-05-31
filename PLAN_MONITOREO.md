# Plan de Monitoreo - SpaceWork

**Sistema:** Sistema de Gestión de Reservas de Espacios  
**Fecha:** Mayo 2026  
**Responsable:** Juan Pareja

---

## 1. Objetivos del Monitoreo

- Detectar problemas de rendimiento en tiempo real
- Verificar la disponibilidad del servicio
- Identificar cuellos de botella en base de datos y API
- Registrar auditoría de cambios críticos

---

## 2. Componentes a Monitorear

### 2.1 Aplicación (Spring Boot)

| Metrica | Herramienta | Umbral | Frecuencia |
|---------|-------------|--------|------------|
| Requests/min | Spring Actuator | > 500 req/min | Tiempo real |
| Latencia API | Spring Actuator | > 2000ms | Tiempo real |
| Errores HTTP | Logback | > 5% errores | Tiempo real |
| JVM Memory | JVM Metrics | > 85% heap | Tiempo real |
| CPU Usage | Micrometer | > 80% | 1 minuto |

### 2.2 Base de Datos (Oracle)

| Metrica | Herramienta | Umbral | Frecuencia |
|---------|-------------|--------|------------|
| Conexiones activas | Oracle Alert | > 80 conexiones | 5 min |
| Espacio tablespace | Oracle sqlplus | < 10% libre | 1 hora |
| Queries lentas | Oracle logs | > 5s ejecucion | Tiempo real |

### 2.3 Sistema Operativo

| Metrica | Herramienta | Umbral | Frecuencia |
|---------|-------------|--------|------------|
| CPU | Task Manager | > 80% | 1 minuto |
| Memoria RAM | Task Manager | > 90% | 1 minuto |
| Disco | Explorador | < 10% libre | 5 min |

---

## 3. Estrategia de Logging

### 3.1 Niveles de Log Configurados

En `application.properties`:
```properties
logging.level.root=INFO
logging.level.com.spacework=DEBUG
```

### 3.2 Rotacion de Logs

Configurado en `logback-spring.xml`:
- Logs diarios rotados
- Maximo 100MB por archivo
- Retencion: 30 dias

### 3.3 Categorias de Logs

| Categoria | Ubicacion | Retencion |
|-----------|-----------|-----------|
| Aplicacion | logs/spacework.log | 30 dias |
| Errores | logs/spacework.error.log | 90 dias |
| Auditoria | logs/spacework.audit.log | 1 año |

---

## 4. Metricas de Negocio

### Reservas por hora
```sql
SELECT TO_CHAR(fecha_reserva, 'HH24:00') AS hora, COUNT(*) AS cantidad
FROM RESERVAS
WHERE TRUNC(fecha_reserva) = TRUNC(SYSDATE)
GROUP BY TO_CHAR(fecha_reserva, 'HH24:00')
ORDER BY hora;
```

### Ingresos diarios
```sql
SELECT TRUNC(fecha_pago) AS fecha, SUM(monto) AS ingreso_total
FROM PAGOS
WHERE estado = 'COMPLETADO'
GROUP BY TRUNC(fecha_pago)
ORDER BY fecha DESC;
```

---

## 5. Alertas

### Alertas criticas (atencion inmediata)

| Alerta | Condicion | Accion |
|--------|-----------|--------|
| App caida | No responde en 2 min | Reiniciar servicio |
| BD desconectada | Connection timeout | Verificar Oracle |
| Disco lleno | < 5% disponible | Limpiar logs |
| JVM Crash | OutOfMemoryError | Aumentar heap, reiniciar |

### Alertas de advertencia (revisar en 1 hora)

| Alerta | Condicion | Accion |
|--------|-----------|--------|
| Latencia alta | > 2 segundos | Analizar queries |
| Muchos errores | > 5% en 5 min | Revisar logs |
| Memoria alta | > 85% heap | Revisar garbage collection |

---

## 6. Herramientas de Monitoreo Implementadas

- **Logback:** Logging configurado con rotacion automatica
- **Spring Boot Actuator:** Endpoints de salud y metricas
- **Dashboard web:** `http://localhost:8080/monitoring.html` con actualizacion cada 5 segundos

### Endpoints del Actuator disponibles
```
GET http://localhost:8080/actuator/health
GET http://localhost:8080/actuator/metrics
GET http://localhost:8080/actuator/metrics/jvm.memory.used
GET http://localhost:8080/actuator/metrics/http.server.requests
```

---

## 7. Dashboard de Monitoreo

El dashboard en `monitoring.html` muestra en tiempo real:

- Estado de la aplicacion y base de datos
- Uso de memoria JVM con grafica historica
- HTTP Requests: total, velocidad y tiempo promedio
- Uso de CPU del proceso y del sistema
- Threads activos y uptime del servidor
- Alertas criticas y de advertencia
- Logs en tiempo real

---

## 8. Procedimiento de Revision

### Revision diaria
```bash
# Verificar logs
tail -100 logs/spacework.log
grep ERROR logs/spacework.log
```

### Revision semanal
- Analizar queries lentas en Oracle
- Revisar espacio en disco
- Verificar rotacion de logs

---

**Ultima actualizacion:** Mayo 2026
