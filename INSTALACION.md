# Instalacion - SpaceWork

Pasos para instalar y ejecutar el sistema en una maquina local.

---

## Requisitos

- JDK 8 o superior
- Maven 3.6 o superior
- Oracle Database XE 11g o superior
- Al menos 4 GB de RAM y 5 GB de disco libre

---

## 1. Configurar la base de datos Oracle

Conectarse como administrador y ejecutar el script principal:

```sql
sqlplus system/[password]@//localhost:1521/XE

@sql/spacework_database.sql
```

Este script crea el usuario, tablas, secuencias, triggers y datos iniciales en un solo paso.

---

## 2. Configurar la conexion

Editar el archivo `src/main/java/com/spacework/util/Conexion.java` con los datos de la base de datos:

```java
private static final String URL      = "jdbc:oracle:thin:@//localhost:1521/XE";
private static final String USUARIO  = "spacework";
private static final String PASSWORD = "tu_password";
```

---

## 3. Configurar el correo (opcional)

Para el envio de emails, editar `src/main/resources/mail.properties`:

```properties
mail.usuario=tucorreo@gmail.com
mail.password=tu_app_password
```

Se necesita una contrasena de aplicacion de Gmail (no la contrasena normal).

---

## 4. Compilar y ejecutar

```bash
# Compilar
mvn clean compile

# Ejecutar
mvn spring-boot:run
```

Esperar hasta ver en consola:
```
Started SpaceWorkApplication in X.XXX seconds
```

La aplicacion queda disponible en `http://localhost:8080`

---

## 5. Acceso al sistema

Abrir el navegador en `http://localhost:8080`

**Usuario administrador por defecto:**
- Usuario: `admin`
- Contrasena: `Admin123!`

---

## Solucion de problemas comunes

**Error: "Failed to determine suitable driver class"**  
Revisar que los excludes de DataSource esten en `application.properties`.

**Error de conexion a Oracle**  
Verificar que el servicio OracleServiceXE este corriendo en Windows.

**Puerto 8080 ocupado**  
Cambiar `server.port=8080` en `application.properties`.
