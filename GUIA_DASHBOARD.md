# Guia del Dashboard de Monitoreo

Instrucciones para usar el dashboard de monitoreo en tiempo real.

---

## Requisito previo

La aplicacion debe estar corriendo:

```bash
mvn spring-boot:run
```

Esperar el mensaje:
```
Started SpaceWorkApplication in X.XXX seconds
```

---

## Acceder al dashboard

Abrir en el navegador:

```
http://localhost:8080/monitoring.html
```

---

## Que muestra el dashboard

El dashboard se actualiza automaticamente cada 5 segundos y muestra:

**Estado general:**
- Estado de la aplicacion (verde = UP, rojo = DOWN)
- Estado de la base de datos (prueba conexion real)
- Tiempo de actividad del servidor (uptime)

**Metricas en tiempo real:**
- Memoria JVM: MB usados, maximo y porcentaje
- CPU: porcentaje del proceso Java y del sistema
- HTTP Requests: total, velocidad por segundo y tiempo promedio de respuesta
- Threads activos en la JVM

**Graficas:**
- Linea historica de uso de memoria
- Barras de requests por intervalo
- Dona de uso de CPU

**Alertas:**
- Criticas: errores 500 en la API o memoria mayor al 80%
- Advertencias: errores 4xx o CPU o memoria elevados

**Logs:**
- Panel con los ultimos eventos del sistema en tiempo real

---

## Endpoints del Actuator

Tambien se pueden consultar directamente en el navegador:

| URL | Descripcion |
|-----|-------------|
| http://localhost:8080/actuator | Lista de endpoints disponibles |
| http://localhost:8080/actuator/health | Estado de salud de la app |
| http://localhost:8080/actuator/metrics | Todas las metricas disponibles |

---

## Auto-refresh

El dashboard tiene un checkbox "Auto-actualizar cada 5s" en la seccion de Controles. 
Si se desmarca, se puede actualizar manualmente con el boton "Actualizar Ahora".
