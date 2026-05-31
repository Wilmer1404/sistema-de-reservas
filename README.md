# SpaceWork - Sistema de Reservas de Espacios

Sistema web desarrollado con Spring Boot y Oracle para gestionar la reserva de espacios, pagos y evaluaciones.

**Stack:** Java 25 · Spring Boot 2.7 · Oracle XE · REST API · JWT · HTML/CSS/JS

---

## Requisitos

- JDK 8 o superior
- Oracle Database XE 11g o superior
- Maven 3.6+

## Configuración de base de datos

Ejecutar los scripts en orden:

```sql
@sql/01_crear_tablas.sql
@sql/02_crear_secuencias.sql
@sql/03_crear_triggers.sql
@sql/04_datos_iniciales.sql
```

Editar `src/main/resources/application.properties` con los datos de conexión Oracle.

## Ejecutar el proyecto

```bash
mvn spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`

**Usuario de prueba:** admin / Admin123!

## Estructura principal

```
src/main/java/com/spacework/
    controller/   - Endpoints REST
    service/      - Lógica de negocio
    dao/          - Acceso a datos (JDBC)
    model/        - Entidades
    util/         - Conexión, JWT, Email
```

## Módulos del sistema

- Autenticación con JWT
- Gestión de espacios (con imagen)
- Reservas con verificación de disponibilidad
- Pagos con descuentos e IGV
- Evaluaciones post-reserva por email
- Reportes de ingresos y ocupación
- Monitoreo con Spring Boot Actuator
- Auditoría de cambios
