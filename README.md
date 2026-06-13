# SpaceWork - Sistema de Reservas de Espacios

Sistema web desarrollado con Spring Boot y Oracle para gestionar la reserva de espacios, pagos y evaluaciones.

**Stack:** Java 8 · Spring Boot 2.7.14 · Oracle XE · REST API · JWT · HTML/CSS/JS

---

## Requisitos

- JDK 8 o superior
- Oracle Database XE 11g o superior
- Maven 3.6+

## Configuración de base de datos

Ejecutar el script principal:

```sql
@sql/spacework_database.sql
```

Editar la conexión de base de datos en `src/main/java/com/spacework/util/Conexion.java`.

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
    config/       - Configuración web y CORS
    service/      - Lógica de negocio
    dao/          - Acceso a datos (JDBC)
    dto/          - Objetos de transferencia de datos
    exception/    - Excepciones personalizadas
    handler/      - Manejadores globales de errores
    interceptor/  - Interceptores HTTP
    model/        - Entidades
    strategy/     - Estrategias de pago
    util/         - Conexión, JWT, Email

src/main/resources/
    application.properties
    logback-spring.xml
    mail.properties
    static/       - Frontend (HTML/CSS/JS)

scripts/maintenance/
    backup_bd.sh
    cleanup_logs.sh
    rotation_auditoria.sql
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
