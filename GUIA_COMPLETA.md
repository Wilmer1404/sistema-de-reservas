# SpaceWork - Sistema de Gestión de Reservas de Espacios

## 📋 Descripción

Sistema completo de gestión de reservas de espacios construido con **Spring Boot 2.7.14**, **Oracle 11g XE**, y **JWT** para autenticación. Incluye gestión de clientes, espacios disponibles, reservas, pagos con descuentos, evaluaciones y auditoría.

## 🛠️ Requisitos Previos

- **Java SE 8** (JDK 1.8+) - LTS
- **Maven 3.6+** - Para compilación
- **Oracle 11g XE** - Base de datos
- **Git** (opcional)

## 🚀 Instalación y Ejecución

### 1. Preparar Base de Datos

```bash
# Conectar a Oracle como SYSTEM
sqlplus system/oracle@localhost:1521:XE

# Ejecutar scripts en orden
@sql/00_crear_usuario.sql
@sql/01_crear_tablas.sql
@sql/02_crear_secuencias.sql
@sql/03_crear_triggers.sql
@sql/04_datos_iniciales.sql
@sql/05_agregar_roles_y_auditoria.sql
```

### 2. Compilar el Proyecto

```bash
# Desde el directorio raíz del proyecto
mvn clean package

# El JAR ejecutable se generará en: target/SistemaReservas-1.0-SNAPSHOT.jar
```

### 3. Ejecutar la Aplicación

```bash
java -jar target/SistemaReservas-1.0-SNAPSHOT.jar
```

El servidor estará disponible en: **http://localhost:8080**

## 📍 Acceso

### Usuarios de Prueba

| Username | Password | Rol |
|----------|----------|-----|
| admin | admin123 | ADMIN |
| recepcionista | rece123 | RECEPCIONISTA |
| gerente | gere123 | GERENTE |

### Acceso a Interfaces

- **Login**: `http://localhost:8080/login.html`
- **Dashboard**: `http://localhost:8080/dashboard.html` (requiere autenticación)
- **API REST**: `http://localhost:8080/api/`

## 🏗️ Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/spacework/
│   │   ├── Main.java                    # Punto de entrada
│   │   ├── SpaceWorkApplication.java    # Configuración Spring Boot
│   │   ├── controller/                  # Controllers REST
│   │   ├── service/                     # Lógica de negocio
│   │   ├── dao/                         # Acceso a datos
│   │   ├── model/                       # Entidades POJO
│   │   ├── strategy/                    # Pattern Strategy (Pagos)
│   │   ├── util/                        # Utilidades
│   │   ├── handler/                     # Manejo de excepciones
│   │   ├── interceptor/                 # JWT Interceptor
│   │   ├── config/                      # Configuraciones
│   │   └── exception/                   # Excepciones custom
│   └── resources/
│       ├── application.properties       # Configuración app
│       ├── mail.properties              # Configuración email
│       └── static/
│           ├── index.html               # Landing page
│           ├── login.html               # Formulario login
│           ├── dashboard.html           # Panel de control
│           ├── reserva.html             # Formulario reserva
│           ├── pago.html                # Procesamiento de pago
│           ├── evaluacion.html          # Formulario evaluación
│           ├── css/style.css            # Estilos
│           └── js/auth.js               # Utilidades autenticación
├── test/
│   └── java/com/spacework/              # Tests unitarios
└── sql/
    ├── 00_crear_usuario.sql             # Crear usuario Oracle
    ├── 01_crear_tablas.sql              # Crear tablas
    ├── 02_crear_secuencias.sql          # Crear secuencias
    ├── 03_crear_triggers.sql            # Crear triggers
    └── 04_datos_iniciales.sql           # Datos de prueba
```

## 📊 API REST Principales

### Autenticación
- `POST /api/auth/login` - Autenticar usuario
- `POST /api/auth/registrar` - Registrar nuevo usuario

### Espacios
- `GET /api/espacios` - Listar todos los espacios
- `GET /api/espacios/{id}` - Obtener espacio por ID
- `GET /api/espacios/tipo/{tipo}` - Obtener espacios por tipo

### Clientes
- `GET /api/clientes` - Listar clientes
- `GET /api/clientes/{id}` - Obtener cliente por ID
- `GET /api/clientes/dni/{dni}` - Obtener cliente por DNI
- `POST /api/clientes` - Crear nuevo cliente

### Reservas
- `POST /api/reservas` - Crear nueva reserva
- `PUT /api/reservas/{id}/cancelar` - Cancelar reserva
- `PUT /api/reservas/{id}/completar` - Completar reserva

### Pagos
- `GET /api/pagos/{id}` - Obtener detalles de pago
- `PUT /api/pagos/{id}/pagar` - Procesar pago

### Evaluaciones
- `POST /api/evaluaciones` - Crear evaluación
- `GET /api/evaluaciones/validar-token` - Validar token de evaluación

## 🔐 Seguridad

- **Autenticación**: JWT HS256
- **Encriptación de Contraseñas**: SHA-256 con salt por usuario
- **CORS**: Habilitado para desarrollo
- **Validación**: JWT Interceptor en rutas protegidas

## 💾 Base de Datos

### Tablas Principales
- **USUARIOS** - Usuarios internos (admin, recepcionista, gerente)
- **CLIENTES** - Clientes externos
- **ESPACIOS** - Espacios disponibles para reserva
- **RESERVAS** - Reservas de clientes
- **PAGOS** - Transacciones de pago
- **DESCUENTOS** - Códigos de descuento
- **EVALUACIONES** - Evaluaciones de clientes
- **AUDITORIA** - Log de cambios en el sistema

## 💳 Métodos de Pago Soportados

1. **Efectivo** - Pago inmediato
2. **Tarjeta de Crédito** - Integración simulada
3. **Transferencia Bancaria** - Integración simulada

## 🎯 Flujo de Negocio Principal

1. **Cliente** busca espacios disponibles
2. **Cliente** crea una reserva especificando fechas
3. **Sistema** valida disponibilidad
4. **Cliente** realiza pago con descuento opcional
5. **Sistema** calcula IGV (18%) y procesa pago
6. **Sistema** genera token de evaluación
7. **Cliente** recibe email de confirmación + link de evaluación
8. **Cliente** completa evaluación del espacio
9. **Sistema** actualiza calificación del espacio

## 🔄 Cálculo de Pagos

```
Fórmula: 
  Subtotal = precio_por_hora × horas
  Monto_Descuento = Subtotal × (descuento_porcentaje / 100)
  Monto_con_Descuento = Subtotal - Monto_Descuento
  IGV = Monto_con_Descuento × 0.18
  Total = Monto_con_Descuento + IGV
```

## 📧 Email

Sistema integrado con **JavaMail API** configurado para Gmail SMTP:
- Confirmación de reserva
- Link de evaluación (7 días después de completada la reserva)
- Confirmación de evaluación recibida

Configurar en `application.properties`:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=tu_email@gmail.com
spring.mail.password=tu_contraseña_app
```

## 🧪 Tests

```bash
# Ejecutar todos los tests
mvn test

# Tests específicos
mvn test -Dtest=ClienteDAOTest
mvn test -Dtest=ReservaDAOTest
```

## 📱 Respuesta API Estándar

```json
{
  "success": true,
  "data": { /* objeto retornado */ },
  "message": "Descripción de la operación"
}
```

## 🐛 Troubleshooting

### Problema: "No suitable driver found"
**Solución**: El oracle driver (ojdbc8.jar) está embebido en el JAR. Si falla, verificar:
```bash
unzip target/SistemaReservas-1.0-SNAPSHOT.jar
# Debe existir: BOOT-INF/lib/ojdbc8.jar
```

### Problema: "Connection refused" en base de datos
**Solución**: Verificar que Oracle esté corriendo:
```bash
sqlplus system/oracle@localhost:1521:XE
```

### Problema: "Token expirado"
**Solución**: Los tokens JWT expiran cada 60 minutos. Hacer login nuevamente.

## 📝 Logs

Logs en `application.properties`:
```properties
logging.level.root=INFO
logging.level.com.spacework=DEBUG
```

## 📄 Licencia

Proyecto educativo - Universidad TecnológicaPeruana (Curso Integrador)

## 👥 Desarrollado por

Sistema desarrollado con Spring Boot, Oracle JDBC y arquitectura de 3 capas (MVC + DAO).

---

**Versión**: 1.0-SNAPSHOT  
**Fecha**: 2024
