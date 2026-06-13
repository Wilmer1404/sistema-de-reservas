# SpaceWork - Sistema de Reservas

Sistema web para la gestion de reservas de espacios, pagos, descuentos, evaluaciones y reportes.
Implementado con Spring Boot (backend REST), Oracle XE y frontend estatico (HTML/CSS/JS).

---

## Tabla de contenido

1. [Resumen del proyecto](#resumen-del-proyecto)
2. [Arquitectura y stack](#arquitectura-y-stack)
3. [Estructura del repositorio](#estructura-del-repositorio)
4. [Modulos funcionales](#modulos-funcionales)
5. [API REST principal](#api-rest-principal)
6. [Frontend disponible](#frontend-disponible)
7. [Monitoreo y observabilidad](#monitoreo-y-observabilidad)
8. [Requisitos](#requisitos)
9. [Configuracion](#configuracion)
10. [Ejecucion](#ejecucion)
11. [Pruebas](#pruebas)
12. [Scripts de mantenimiento](#scripts-de-mantenimiento)
13. [Documentacion adicional](#documentacion-adicional)
14. [Notas de seguridad](#notas-de-seguridad)

---

## Resumen del proyecto

SpaceWork permite:

- Gestionar clientes y autenticacion.
- Registrar y administrar espacios y horarios.
- Crear y gestionar reservas.
- Procesar pagos con estrategia por metodo (efectivo, tarjeta, transferencia).
- Aplicar descuentos y validaciones.
- Enviar notificaciones y evaluaciones por correo.
- Consultar reportes de negocio.
- Monitorear salud y metricas del sistema.

---

## Arquitectura y stack

### Backend

- Java 8
- Spring Boot 2.7.14
- API REST
- JDBC manual (sin DataSource/JPA autoconfig)
- JWT para autenticacion

### Base de datos

- Oracle XE 11g o superior
- Script principal de inicializacion: `sql/spacework_database.sql`

### Frontend

- HTML, CSS, JavaScript estatico servido por Spring
- Vistas en `src/main/resources/static`

### Build y ejecucion

- Maven
- `start.bat` para arranque rapido en Windows

---

## Estructura del repositorio

```text
.
|-- pom.xml
|-- start.bat
|-- README.md
|-- INSTALACION.md
|-- CONFIGURACION_CORREO.md
|-- GUIA_DASHBOARD.md
|-- PLAN_MONITOREO.md
|-- PRUEBAS_MONITOREO.md
|-- PLAN_MANTENIMIENTO.md
|-- sql/
|   |-- spacework_database.sql
|-- scripts/
|   |-- maintenance/
|       |-- backup_bd.sh
|       |-- cleanup_logs.sh
|       |-- rotation_auditoria.sql
|       |-- README.md
|-- src/
|   |-- main/
|   |   |-- java/com/spacework/
|   |   |   |-- SpaceWorkApplication.java
|   |   |   |-- Main.java
|   |   |   |-- config/
|   |   |   |-- controller/
|   |   |   |-- dao/
|   |   |   |-- dto/
|   |   |   |-- exception/
|   |   |   |-- handler/
|   |   |   |-- interceptor/
|   |   |   |-- model/
|   |   |   |-- service/
|   |   |   |-- strategy/
|   |   |   |-- util/
|   |   |-- resources/
|   |       |-- application.properties
|   |       |-- logback-spring.xml
|   |       |-- mail.properties
|   |       |-- static/
|   |           |-- index.html
|   |           |-- login.html
|   |           |-- reserva.html
|   |           |-- pago.html
|   |           |-- descuento.html
|   |           |-- evaluacion.html
|   |           |-- horario.html
|   |           |-- reporte.html
|   |           |-- monitoring.html
|   |           |-- css/style.css
|   |           |-- js/app.js
|   |           |-- js/auth.js
|   |-- test/
|       |-- java/com/spacework/
```

---

## Modulos funcionales

- Autenticacion y seguridad
- Gestion de clientes
- Gestion de espacios
- Gestion de horarios
- Gestion de reservas
- Gestion de pagos
- Gestion de descuentos
- Evaluaciones y notificaciones por correo
- Reportes (ingresos, ocupacion, estado de reservas)
- Monitoreo tecnico del sistema

---

## API REST principal

Las rutas base identificadas en controladores REST son:

- `/api/auth`
- `/api/clientes`
- `/api/espacios`
- `/api/horarios`
- `/api/reservas`
- `/api/pagos`
- `/api/descuentos`
- `/api/evaluaciones`
- `/api/notificaciones`
- `/api/calendario`
- `/api/reportes`

### Endpoints de monitoreo (Actuator)

- `/actuator`
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/metrics/jvm.memory.used`
- `/actuator/metrics/http.server.requests`

---

## Frontend disponible

Pantallas principales servidas desde `src/main/resources/static`:

- `index.html`
- `login.html`
- `reserva.html`
- `pago.html`
- `descuento.html`
- `evaluacion.html`
- `horario.html`
- `reporte.html`
- `monitoring.html`

---

## Monitoreo y observabilidad

- Logging configurado en `src/main/resources/logback-spring.xml`
- Niveles de log en `src/main/resources/application.properties`
- Dashboard visual en `/monitoring.html`
- Actuator habilitado en `/actuator/*`

---

## Requisitos

- JDK 8 o superior
- Maven 3.6 o superior
- Oracle Database XE 11g o superior

---

## Configuracion

### 1) Base de datos

Ejecutar:

```sql
@sql/spacework_database.sql
```

Configurar credenciales de Oracle en:

- `src/main/java/com/spacework/util/Conexion.java`

### 2) Correo SMTP

Configurar en:

- `src/main/resources/mail.properties`

Referencia detallada:

- `CONFIGURACION_CORREO.md`

---

## Ejecucion

### Opcion A - Maven

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

### Opcion B - Windows

```bat
start.bat
```

Aplicacion disponible en:

- `http://localhost:8080`

---

## Pruebas

Ejecutar pruebas unitarias:

```bash
mvn test
```

Ubicacion base de tests:

- `src/test/java/com/spacework`

---

## Scripts de mantenimiento

Directorio:

- `scripts/maintenance/`

Incluye:

- `backup_bd.sh`
- `cleanup_logs.sh`
- `rotation_auditoria.sql`

Referencia de uso:

- `scripts/maintenance/README.md`
- `PLAN_MANTENIMIENTO.md`

---

## Documentacion adicional

- `INSTALACION.md`
- `CONFIGURACION_CORREO.md`
- `GUIA_DASHBOARD.md`
- `PLAN_MONITOREO.md`
- `PRUEBAS_MONITOREO.md`
- `PLAN_MANTENIMIENTO.md`

---

## Notas de seguridad

- No subir credenciales reales al repositorio.
- Se recomienda excluir `src/main/resources/mail.properties` del control de versiones o usar placeholders por entorno.
- Para uso en equipo, definir una plantilla (ejemplo: `mail.properties.example`) sin secretos.
