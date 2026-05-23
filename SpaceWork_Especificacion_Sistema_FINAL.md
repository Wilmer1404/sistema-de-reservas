# SpaceWork — Sistema de Gestión de Reservas de Espacios

> **Documento de especificación técnica para implementación.**
> Pensado para ser consumido por GitHub Copilot / agentes de codificación.
> Stack: Java 8 + Spring Boot 2.7 + Oracle 11g XE + HTML/CSS/JS + JWT.

---

## 1. Resumen ejecutivo

SpaceWork es una aplicación web para gestionar la **reserva de espacios** (salas, auditorios, oficinas) con tres procesos centrales:

1. **Reservación** — el cliente busca un espacio, elige fecha/hora y crea una reserva en estado `PENDIENTE`.
2. **Pago** — se cobra el monto (con descuento opcional + IGV 18%), y la reserva pasa a `CONFIRMADA`.
3. **Evaluación post-reserva** — al pagar, se genera un token único de 32 caracteres con vigencia de 7 días que permite al cliente calificar el espacio (1–5 estrellas).

El sistema soporta autenticación JWT, control por roles (`ADMIN`, `RECEPCIONISTA`, `GERENTE`), auditoría inmutable de operaciones críticas y notificaciones internas.

---

## 2. Stack tecnológico

| Capa | Tecnología | Notas |
|---|---|---|
| Lenguaje backend | Java SE 8 (JDK 1.8) | LTS amplio |
| Framework | Spring Boot 2.7.x | Tomcat embebido en puerto 8080 |
| Persistencia | JDBC + DAO manual | Sin JPA/Hibernate (DAO patrón explícito) |
| Base de datos | Oracle Database 11g XE | PL/SQL, triggers, secuencias |
| Driver | ojdbc8.jar | Type 4 Thin |
| Build | Maven 3.6+ | `pom.xml` |
| Frontend | HTML5 + CSS3 + JS ES6 | Sin frameworks pesados, `fetch` nativo |
| Auth | JWT (HS256) | Stateless, header `Authorization: Bearer <token>` |
| Email | JavaMail API + Gmail SMTP | Confirmaciones y tokens de evaluación |
| Hashing | SHA-256 + salt por usuario | Nunca password en texto plano |
| VCS | Git + GitHub | — |

---

## 3. Arquitectura en capas

```
[Browser HTML/JS]
       │  HTTP/JSON
       ▼
[@RestController]  ← valida payload, devuelve códigos HTTP
       │
       ▼
[Service]          ← lógica de negocio, transacciones, reglas
       │
       ▼
[DAO]              ← acceso a Oracle, CRUD, queries específicas
       │
       ▼
[Oracle 11g XE]    ← tablas, secuencias, triggers, restricciones
```

**Capas y carpetas:**

```
src/main/java/com/spacework/
├── SpaceWorkApplication.java
├── controller/api/      ← REST controllers
├── service/             ← lógica de negocio
├── dao/                 ← acceso a BD (JDBC puro)
├── model/               ← POJOs de dominio
├── config/              ← SecurityConfig, JwtFilter
└── util/                ← Conexion (Singleton), EmailUtil, HashUtil, JwtUtil
```

---

## 4. Modelo de dominio (entidades clave)

| Entidad | Rol |
|---|---|
| `Rol` | Catálogo de perfiles internos con permisos JSON |
| `Usuario` | Personal interno autenticable (ADMIN/RECEPCIONISTA/GERENTE) |
| `Cliente` | Usuario externo que reserva (identificado por DNI) |
| `Espacio` | Recurso reservable (sala, auditorio, oficina) |
| `Reserva` | Núcleo: enlaza Cliente + Espacio + rango horario |
| `Pago` | Transacción asociada 1:1 a una Reserva |
| `Descuento` | Cupón promocional con vigencia y usos limitados |
| `TokenEvaluacion` | Token único de 32 chars, vence en 7 días |
| `Evaluacion` | Calificación 1–5 + comentario |
| `HorarioBloqueado` | Rangos no disponibles (mantenimiento/eventos) |
| `Notificacion` | Mensajes internos para Usuarios |
| `Auditoria` | Registro inmutable de INSERT/UPDATE/DELETE críticos |

**Cardinalidades principales:**

- `Rol 1—N Usuario`
- `Cliente 1—N Reserva`
- `Espacio 1—N Reserva`
- `Reserva 1—1 Pago`
- `Pago N—0..1 Descuento`
- `Pago 1—1 TokenEvaluacion` (solo cuando pago = COMPLETADO)
- `Reserva 1—0..1 Evaluacion`
- `Espacio 1—N HorarioBloqueado`

---

## 5. Estructura completa de la base de datos (Oracle 11g XE)

> Todo el DDL siguiente está pensado para ejecutarse en orden. Se crean primero las tablas sin FK circular, luego las que dependen, luego secuencias, índices y triggers.

### 5.1 Usuario y schema de aplicación

```sql
-- Como SYSTEM en Oracle XE
CREATE USER spacework IDENTIFIED BY spacework;
GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE SEQUENCE,
      CREATE TRIGGER, CREATE PROCEDURE TO spacework;
ALTER USER spacework QUOTA UNLIMITED ON USERS;
```

### 5.2 Tabla `ROLES`

```sql
CREATE TABLE ROLES (
    id_rol         NUMBER          PRIMARY KEY,
    nombre         VARCHAR2(50)    NOT NULL UNIQUE,
    descripcion    VARCHAR2(255),
    permisos       CLOB,                          -- JSON: ["RESERVA_CREATE","PAGO_READ",...]
    fecha_creacion DATE            DEFAULT SYSDATE
);
CREATE SEQUENCE seq_rol START WITH 1 INCREMENT BY 1;
```

### 5.3 Tabla `USUARIOS`

```sql
CREATE TABLE USUARIOS (
    id_usuario          NUMBER         PRIMARY KEY,
    username            VARCHAR2(50)   NOT NULL UNIQUE,
    password            VARCHAR2(255)  NOT NULL,  -- SHA-256(password + salt)
    salt                VARCHAR2(64)   NOT NULL,
    nombre              VARCHAR2(100)  NOT NULL,
    email               VARCHAR2(100)  UNIQUE,
    id_rol              NUMBER         NOT NULL,
    estado              VARCHAR2(20)   DEFAULT 'ACTIVO'
                                       CHECK (estado IN ('ACTIVO','INACTIVO','BLOQUEADO')),
    fecha_creacion      DATE           DEFAULT SYSDATE,
    fecha_actualizacion DATE           DEFAULT SYSDATE,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES ROLES(id_rol)
);
CREATE SEQUENCE seq_usuario START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_usuario_username ON USUARIOS(username);
CREATE INDEX idx_usuario_email    ON USUARIOS(email);
```

### 5.4 Tabla `CLIENTES`

```sql
CREATE TABLE CLIENTES (
    id_cliente     NUMBER         PRIMARY KEY,
    nombre         VARCHAR2(100)  NOT NULL,
    apellido       VARCHAR2(100)  NOT NULL,
    dni            VARCHAR2(20)   NOT NULL UNIQUE,
    email          VARCHAR2(100),
    telefono       VARCHAR2(20),
    estado         VARCHAR2(20)   DEFAULT 'ACTIVO'
                                  CHECK (estado IN ('ACTIVO','INACTIVO')),
    fecha_registro DATE           DEFAULT SYSDATE
);
CREATE SEQUENCE seq_cliente START WITH 1000 INCREMENT BY 1;
CREATE INDEX idx_cliente_dni   ON CLIENTES(dni);
CREATE INDEX idx_cliente_email ON CLIENTES(email);
```

### 5.5 Tabla `ESPACIOS`

```sql
CREATE TABLE ESPACIOS (
    id_espacio       NUMBER         PRIMARY KEY,
    nombre           VARCHAR2(100)  NOT NULL,
    tipo             VARCHAR2(50)   NOT NULL,    -- 'Sala de Conferencias','Auditorio','Oficina'
    capacidad        NUMBER         NOT NULL CHECK (capacidad > 0),
    ubicacion        VARCHAR2(255),
    precio_por_hora  NUMBER(10,2)   NOT NULL CHECK (precio_por_hora >= 0),
    descripcion      CLOB,
    imagen_url       VARCHAR2(500),
    estado           VARCHAR2(20)   DEFAULT 'ACTIVO'
                                    CHECK (estado IN ('ACTIVO','INACTIVO','MANTENIMIENTO')),
    fecha_creacion   DATE           DEFAULT SYSDATE
);
CREATE SEQUENCE seq_espacio START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_espacio_tipo   ON ESPACIOS(tipo);
CREATE INDEX idx_espacio_estado ON ESPACIOS(estado);
```

### 5.6 Tabla `DESCUENTOS` (debe crearse antes de `PAGOS` porque hay FK)

```sql
CREATE TABLE DESCUENTOS (
    id_descuento   NUMBER         PRIMARY KEY,
    codigo         VARCHAR2(20)   NOT NULL UNIQUE,
    descripcion    VARCHAR2(255),
    porcentaje     NUMBER(5,2)    NOT NULL
                                  CHECK (porcentaje > 0 AND porcentaje <= 100),
    monto_minimo   NUMBER(10,2)   DEFAULT 0,
    usos_actuales  NUMBER         DEFAULT 0,
    usos_maximos   NUMBER         NOT NULL CHECK (usos_maximos > 0),
    fecha_inicio   DATE           NOT NULL,
    fecha_fin      DATE           NOT NULL,
    estado         VARCHAR2(20)   DEFAULT 'ACTIVO'
                                  CHECK (estado IN ('ACTIVO','AGOTADO','EXPIRADO','INACTIVO')),
    CONSTRAINT chk_descuento_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT chk_descuento_usos   CHECK (usos_actuales <= usos_maximos)
);
CREATE SEQUENCE seq_descuento START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_descuento_codigo ON DESCUENTOS(codigo);
CREATE INDEX idx_descuento_estado ON DESCUENTOS(estado);
```

### 5.7 Tabla `RESERVAS`

```sql
CREATE TABLE RESERVAS (
    id_reserva     NUMBER         PRIMARY KEY,
    id_cliente     NUMBER         NOT NULL,
    id_espacio     NUMBER         NOT NULL,
    fecha_inicio   TIMESTAMP      NOT NULL,
    fecha_fin      TIMESTAMP      NOT NULL,
    monto_total    NUMBER(10,2)   NOT NULL,
    estado         VARCHAR2(20)   DEFAULT 'PENDIENTE'
                                  CHECK (estado IN ('PENDIENTE','CONFIRMADA',
                                                    'COMPLETADA','CANCELADA')),
    observaciones  VARCHAR2(500),
    fecha_creacion DATE           DEFAULT SYSDATE,
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente) REFERENCES CLIENTES(id_cliente),
    CONSTRAINT fk_reserva_espacio FOREIGN KEY (id_espacio) REFERENCES ESPACIOS(id_espacio),
    CONSTRAINT chk_reserva_fechas CHECK (fecha_fin > fecha_inicio)
);
CREATE SEQUENCE seq_reserva START WITH 1000 INCREMENT BY 1;
CREATE INDEX idx_reserva_cliente ON RESERVAS(id_cliente);
CREATE INDEX idx_reserva_espacio ON RESERVAS(id_espacio);
CREATE INDEX idx_reserva_fechas  ON RESERVAS(fecha_inicio, fecha_fin);
CREATE INDEX idx_reserva_estado  ON RESERVAS(estado);
```

### 5.8 Tabla `PAGOS`

```sql
CREATE TABLE PAGOS (
    id_pago             NUMBER         PRIMARY KEY,
    id_reserva          NUMBER         NOT NULL UNIQUE,    -- 1:1 con Reserva
    id_descuento        NUMBER,                            -- nullable
    monto               NUMBER(10,2)   NOT NULL CHECK (monto >= 0),  -- subtotal
    monto_final         NUMBER(10,2)   NOT NULL,                     -- después de desc + IGV
    descuento_aplicado  NUMBER(10,2)   DEFAULT 0,
    igv                 NUMBER(10,2)   DEFAULT 0,
    metodo_pago         VARCHAR2(50)   CHECK (metodo_pago IN
                                            ('EFECTIVO','TARJETA','TRANSFERENCIA')),
    estado_pago         VARCHAR2(20)   DEFAULT 'PENDIENTE'
                                       CHECK (estado_pago IN
                                            ('PENDIENTE','COMPLETADO','RECHAZADO')),
    referencia          VARCHAR2(100),                     -- nro de operación externa
    fecha_creacion      DATE           DEFAULT SYSDATE,
    fecha_pago          DATE,
    CONSTRAINT fk_pago_reserva   FOREIGN KEY (id_reserva)   REFERENCES RESERVAS(id_reserva),
    CONSTRAINT fk_pago_descuento FOREIGN KEY (id_descuento) REFERENCES DESCUENTOS(id_descuento)
);
CREATE SEQUENCE seq_pago START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_pago_estado  ON PAGOS(estado_pago);
CREATE INDEX idx_pago_reserva ON PAGOS(id_reserva);
```

### 5.9 Tabla `HORARIOS_BLOQUEADOS`

```sql
CREATE TABLE HORARIOS_BLOQUEADOS (
    id_bloqueo       NUMBER         PRIMARY KEY,
    id_espacio       NUMBER         NOT NULL,
    fecha_inicio     TIMESTAMP      NOT NULL,
    fecha_fin        TIMESTAMP      NOT NULL,
    razon            VARCHAR2(255),
    usuario_creacion VARCHAR2(100),
    fecha_creacion   DATE           DEFAULT SYSDATE,
    CONSTRAINT fk_bloqueo_espacio FOREIGN KEY (id_espacio) REFERENCES ESPACIOS(id_espacio),
    CONSTRAINT chk_bloqueo_fechas CHECK (fecha_fin > fecha_inicio)
);
CREATE SEQUENCE seq_bloqueo START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_bloqueo_espacio ON HORARIOS_BLOQUEADOS(id_espacio, fecha_inicio, fecha_fin);
```

### 5.10 Tabla `TOKENS_EVALUACION`

```sql
CREATE TABLE TOKENS_EVALUACION (
    id_token         NUMBER         PRIMARY KEY,
    id_pago          NUMBER         NOT NULL UNIQUE,
    token            VARCHAR2(255)  NOT NULL UNIQUE,   -- 32 chars alfanuméricos
    email_cliente    VARCHAR2(100)  NOT NULL,
    fecha_creacion   DATE           DEFAULT SYSDATE,
    fecha_expiracion DATE           NOT NULL,           -- +7 días desde fin de reserva
    utilizado        NUMBER(1)      DEFAULT 0 CHECK (utilizado IN (0,1)),
    CONSTRAINT fk_token_pago FOREIGN KEY (id_pago) REFERENCES PAGOS(id_pago)
);
CREATE SEQUENCE seq_token START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_token_value ON TOKENS_EVALUACION(token);
```

### 5.11 Tabla `EVALUACIONES`

```sql
CREATE TABLE EVALUACIONES (
    id_evaluacion    NUMBER         PRIMARY KEY,
    id_reserva       NUMBER         NOT NULL UNIQUE,    -- 1 evaluación por reserva
    id_cliente       NUMBER         NOT NULL,
    calificacion     NUMBER(2,1)    NOT NULL
                                    CHECK (calificacion >= 1 AND calificacion <= 5),
    comentario       CLOB,
    fecha_evaluacion DATE           DEFAULT SYSDATE,
    CONSTRAINT fk_eval_reserva FOREIGN KEY (id_reserva) REFERENCES RESERVAS(id_reserva),
    CONSTRAINT fk_eval_cliente FOREIGN KEY (id_cliente) REFERENCES CLIENTES(id_cliente)
);
CREATE SEQUENCE seq_evaluacion START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_eval_cliente ON EVALUACIONES(id_cliente);
CREATE INDEX idx_eval_reserva ON EVALUACIONES(id_reserva);
```

### 5.12 Tabla `NOTIFICACIONES`

```sql
CREATE TABLE NOTIFICACIONES (
    id_notificacion NUMBER         PRIMARY KEY,
    id_usuario      NUMBER         NOT NULL,
    tipo            VARCHAR2(50)   NOT NULL,            -- 'RESERVA_NUEVA','PAGO_PENDIENTE','EVAL_RECIBIDA'
    asunto          VARCHAR2(255)  NOT NULL,
    mensaje         CLOB,
    leida           NUMBER(1)      DEFAULT 0 CHECK (leida IN (0,1)),
    fecha_creacion  DATE           DEFAULT SYSDATE,
    CONSTRAINT fk_notif_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIOS(id_usuario)
);
CREATE SEQUENCE seq_notificacion START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_notif_usuario_leida ON NOTIFICACIONES(id_usuario, leida);
```

### 5.13 Tabla `AUDITORIA`

```sql
CREATE TABLE AUDITORIA (
    id_auditoria      NUMBER         PRIMARY KEY,
    id_usuario        NUMBER,
    tabla_modificada  VARCHAR2(50)   NOT NULL,
    operacion         VARCHAR2(20)   NOT NULL
                                     CHECK (operacion IN ('INSERT','UPDATE','DELETE')),
    datos_antiguos    CLOB,
    datos_nuevos      CLOB,
    fecha_operacion   DATE           DEFAULT SYSDATE,
    ip_origen         VARCHAR2(45),
    CONSTRAINT fk_audit_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIOS(id_usuario)
);
CREATE SEQUENCE seq_auditoria START WITH 1 INCREMENT BY 1;
CREATE INDEX idx_audit_tabla ON AUDITORIA(tabla_modificada);
CREATE INDEX idx_audit_fecha ON AUDITORIA(fecha_operacion);
```

### 5.14 Triggers de auditoría

```sql
CREATE OR REPLACE TRIGGER trg_audit_reservas
AFTER INSERT OR UPDATE OR DELETE ON RESERVAS
FOR EACH ROW
DECLARE
    v_operacion VARCHAR2(20);
    v_datos_old CLOB;
    v_datos_new CLOB;
BEGIN
    IF INSERTING THEN
        v_operacion := 'INSERT';
        v_datos_new := 'id=' || :NEW.id_reserva || ',estado=' || :NEW.estado;
    ELSIF UPDATING THEN
        v_operacion := 'UPDATE';
        v_datos_old := 'estado=' || :OLD.estado;
        v_datos_new := 'estado=' || :NEW.estado;
    ELSIF DELETING THEN
        v_operacion := 'DELETE';
        v_datos_old := 'id=' || :OLD.id_reserva;
    END IF;

    INSERT INTO AUDITORIA (id_auditoria, tabla_modificada, operacion,
                           datos_antiguos, datos_nuevos, fecha_operacion)
    VALUES (seq_auditoria.NEXTVAL, 'RESERVAS', v_operacion,
            v_datos_old, v_datos_new, SYSDATE);
END;
/

-- Replicar el mismo patrón para PAGOS y DESCUENTOS:
-- trg_audit_pagos, trg_audit_descuentos
```

### 5.15 Trigger para recalcular calificación promedio del espacio

```sql
CREATE OR REPLACE TRIGGER trg_recalc_calificacion
AFTER INSERT ON EVALUACIONES
FOR EACH ROW
DECLARE
    v_id_espacio NUMBER;
    v_promedio   NUMBER(3,2);
BEGIN
    SELECT id_espacio INTO v_id_espacio
    FROM RESERVAS WHERE id_reserva = :NEW.id_reserva;

    SELECT AVG(e.calificacion) INTO v_promedio
    FROM EVALUACIONES e
    JOIN RESERVAS r ON e.id_reserva = r.id_reserva
    WHERE r.id_espacio = v_id_espacio;

    -- La calificación promedio se calcula on-demand vía esta query;
    -- si se quiere persistir, agregar columna calificacion_promedio a ESPACIOS y UPDATE aquí.
    NULL;
END;
/
```

### 5.16 Datos iniciales (seed)

```sql
-- Roles
INSERT INTO ROLES (id_rol, nombre, descripcion, permisos) VALUES
  (seq_rol.NEXTVAL, 'ADMIN', 'Administrador total',
   '["USUARIO_*","ESPACIO_*","RESERVA_*","PAGO_*","DESCUENTO_*","AUDITORIA_READ"]');
INSERT INTO ROLES (id_rol, nombre, descripcion, permisos) VALUES
  (seq_rol.NEXTVAL, 'RECEPCIONISTA', 'Gestiona reservas y pagos',
   '["RESERVA_READ","RESERVA_CREATE","PAGO_READ","PAGO_UPDATE"]');
INSERT INTO ROLES (id_rol, nombre, descripcion, permisos) VALUES
  (seq_rol.NEXTVAL, 'GERENTE', 'Consulta y reportes',
   '["RESERVA_READ","PAGO_READ","DESCUENTO_READ","AUDITORIA_READ"]');

-- Usuario admin inicial (password = "Admin123!" hashed por la app antes de insertar)
INSERT INTO USUARIOS (id_usuario, username, password, salt, nombre, email, id_rol)
VALUES (seq_usuario.NEXTVAL, 'admin',
        'REEMPLAZAR_HASH_SHA256', 'REEMPLAZAR_SALT',
        'Administrador', 'admin@spacework.com', 1);

-- Espacios de ejemplo
INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, descripcion)
VALUES (seq_espacio.NEXTVAL, 'Sala Premium', 'Sala de Conferencias', 20, 'Piso 5', 150.00,
        'Sala equipada con proyector 4K, pizarra interactiva y videoconferencia.');
INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, descripcion)
VALUES (seq_espacio.NEXTVAL, 'Auditorio Central', 'Auditorio', 100, 'Piso 1', 300.00,
        'Auditorio con escenario, sonido envolvente y butacas reclinables.');
INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, descripcion)
VALUES (seq_espacio.NEXTVAL, 'Oficina Compartida 3', 'Oficina', 6, 'Piso 3', 80.00,
        'Oficina para hasta 6 personas, con WiFi y café incluido.');

-- Descuento de ejemplo
INSERT INTO DESCUENTOS (id_descuento, codigo, descripcion, porcentaje,
                        monto_minimo, usos_maximos, fecha_inicio, fecha_fin)
VALUES (seq_descuento.NEXTVAL, 'BIENVENIDA10', 'Descuento de bienvenida',
        10.0, 500.00, 100, SYSDATE, SYSDATE + 90);

COMMIT;
```

---

## 6. Procesos de negocio (BPMN resumido)

### 6.1 BPM-01 — Reservación

```
Cliente → Login → Buscar espacios (fecha/hora/tipo/capacidad)
       ↓
Sistema → Consultar disponibilidad (RESERVAS + HORARIOS_BLOQUEADOS)
       ↓
       ¿Disponible? ──NO──→ Mostrar mensaje + alternativas → FIN (EX-01)
       SI
       ↓
Cliente → Confirmar datos
       ¿Confirma? ──NO──→ Liberar bloqueo → FIN (EX-02)
       SI
       ↓
Sistema → INSERT en RESERVAS (estado=PENDIENTE) + INSERT en PAGOS (estado=PENDIENTE)
       ↓
       Continúa con BPM-02 (Pagos)
```

**Excepciones:**

| Código | Excepción | Acción |
|---|---|---|
| EX-01 | Espacio no disponible | Sugerir fechas alternativas |
| EX-02 | Cliente cancela | Liberar bloqueo |
| EX-03 | Error de pago | Reserva queda PENDIENTE 24h, reintentar por email |
| EX-04 | Falla email confirmación | Log + reintento asíncrono, reserva igual se confirma |

### 6.2 BPM-02 — Pagos

```
Reserva PENDIENTE
   ↓
Cliente → Elige método (EFECTIVO/TARJETA/TRANSFERENCIA)
   ↓
Cliente → (opcional) Ingresa código de descuento
   ↓
Sistema → Validar descuento (vigencia + usos + monto_minimo)
   ↓
Sistema → Calcular: subtotal → aplicar descuento → +IGV 18% sobre monto descontado
   ↓
Sistema → Procesar pago
   ¿Autorizado? ──NO──→ Pago RECHAZADO, reserva sigue PENDIENTE → email reintento (EX-03)
   SI
   ↓
Sistema → PAGOS.estado_pago = COMPLETADO
       → RESERVAS.estado = CONFIRMADA
       → DESCUENTOS.usos_actuales++ (si aplicó)
       → Generar token 32 chars → INSERT TOKENS_EVALUACION (fecha_exp = reserva.fecha_fin + 7)
       → Enviar email confirmación + link evaluación
```

**Reglas:**

- Descuento aplica solo si `SYSDATE BETWEEN fecha_inicio AND fecha_fin` **y** `usos_actuales < usos_maximos` **y** `monto >= monto_minimo` **y** `estado = 'ACTIVO'`.
- IGV (18%) se calcula sobre el monto **ya descontado**: `igv = (subtotal - descuento) * 0.18`.
- `monto_final = subtotal - descuento + igv`.

### 6.3 BPM-03 — Evaluación post-reserva

```
Cliente abre email → click en link con ?token=xxxxx
   ↓
Sistema → SELECT * FROM TOKENS_EVALUACION WHERE token=? AND utilizado=0
   ¿Existe y no expirado y SYSDATE <= fecha_expiracion? ──NO──→ Mostrar error → FIN
   SI
   ↓
Cliente → Califica 1-5 estrellas + comentario opcional
   ↓
Sistema → INSERT EVALUACIONES
       → UPDATE TOKENS_EVALUACION SET utilizado=1
       → Trigger recalcula calificación promedio del espacio
       → Email de confirmación al cliente
```

---

## 7. Patrones de diseño aplicados

| Patrón | Aplicación |
|---|---|
| **MVC** | Estructura general (Controller-Service-DAO) |
| **DAO** | Una clase por entidad: `ClienteDAO`, `ReservaDAO`, `PagoDAO`... encapsula JDBC |
| **Singleton** | `Conexion` mantiene pool único de conexiones a Oracle |
| **Builder** | `Reserva.Builder` para construir reservas con muchos atributos opcionales |
| **Strategy** | `EstrategiaPago` con 3 implementaciones: `PagoEfectivo`, `PagoTarjeta`, `PagoTransferencia` |
| **Factory** | `EstrategiaPagoFactory.crear(metodo)` devuelve la estrategia correcta |

---

## 8. API REST

### 8.1 Reglas generales

- **Base URL:** `http://localhost:8080/api`
- **Auth:** todos los endpoints (excepto `POST /auth/login`) requieren `Authorization: Bearer <JWT>`.
- **Content-Type:** `application/json`.
- **Fechas:** ISO-8601 (`2026-05-15T09:00:00`).
- **Respuesta uniforme:**
  ```json
  { "success": true, "data": { ... }, "message": "..." }
  ```

### 8.2 Endpoints

#### Autenticación

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/auth/login` | Autentica y devuelve JWT |
| `POST` | `/auth/logout` | Invalida token actual |

**`POST /auth/login`**
```json
// Request
{ "username": "admin", "password": "Admin123!" }

// Response 200
{
  "success": true,
  "token": "eyJhbGciOi...",
  "user": { "idUsuario": 1, "username": "admin", "nombre": "Administrador",
            "email": "admin@spacework.com", "rol": "ADMIN" }
}
```

#### Espacios

| Método | Ruta | Descripción |
|---|---|---|
| `GET`  | `/espacios` | Lista espacios. Filtros: `?tipo=&capacidadMin=&estado=` |
| `GET`  | `/espacios/{id}` | Detalle de un espacio + calificación promedio |
| `GET`  | `/espacios/{id}/disponibilidad?inicio=&fin=` | Verifica disponibilidad y devuelve precio total |
| `POST` | `/espacios` | Crear espacio (rol ADMIN) |
| `PUT`  | `/espacios/{id}` | Actualizar (rol ADMIN) |
| `DELETE` | `/espacios/{id}` | Desactivar (soft delete: estado=INACTIVO) |

#### Reservas

| Método | Ruta | Descripción |
|---|---|---|
| `GET`  | `/reservas` | Lista. Filtros: `?idCliente=&estado=&desde=&hasta=` |
| `GET`  | `/reservas/{id}` | Detalle completo (incluye pago y evaluación) |
| `POST` | `/reservas` | Crear (estado=PENDIENTE, genera registro PAGOS asociado) |
| `PUT`  | `/reservas/{id}/cancelar` | Cancela si está en PENDIENTE/CONFIRMADA |
| `PUT`  | `/reservas/{id}/completar` | Marca como COMPLETADA (uso real cumplido) |

**`POST /reservas`**
```json
// Request
{ "idCliente": 1001, "idEspacio": 1,
  "fechaInicio": "2026-05-15T09:00:00", "fechaFin": "2026-05-15T17:00:00" }

// Response 201
{ "success": true, "data": { "idReserva": 1001, "estado": "PENDIENTE",
                              "montoTotal": 1200.00, "idPago": 5001 } }
```

#### Pagos

| Método | Ruta | Descripción |
|---|---|---|
| `GET`  | `/pagos/{id}` | Detalle del pago |
| `PUT`  | `/pagos/{id}/pagar` | Procesa el pago (aplica descuento + IGV, genera token) |

**`PUT /pagos/{id}/pagar`**
```json
// Request
{ "metodoPago": "TARJETA", "codigoDescuento": "BIENVENIDA10" }

// Response 200
{ "success": true, "data": {
    "idPago": 5001, "estado": "COMPLETADO",
    "subtotal": 1200.00, "descuentoAplicado": 120.00,
    "igv": 194.40, "montoFinal": 1274.40,
    "tokenEvaluacion": "a1b2c3d4e5f6..."
} }
```

#### Descuentos

| Método | Ruta | Descripción |
|---|---|---|
| `GET`  | `/descuentos` | Lista (rol ADMIN) |
| `POST` | `/descuentos` | Crear código (rol ADMIN) |
| `POST` | `/descuentos/validar` | Valida vigencia y devuelve cálculo |

**`POST /descuentos/validar`**
```json
// Request
{ "codigo": "BIENVENIDA10", "montoCompra": 1200.00 }

// Response 200
{ "valido": true, "porcentaje": 10.0, "descuento": 120.00, "montoFinal": 1080.00 }
```

#### Evaluaciones

| Método | Ruta | Descripción |
|---|---|---|
| `GET`  | `/evaluaciones/validar-token?token=` | Verifica token sin consumirlo |
| `POST` | `/evaluaciones` | Registra evaluación (consume token) |
| `GET`  | `/espacios/{id}/evaluaciones` | Lista evaluaciones de un espacio |

**`POST /evaluaciones`**
```json
// Request
{ "token": "a1b2c3d4...", "calificacion": 5, "comentario": "Excelente espacio." }

// Response 201
{ "success": true, "data": { "idEvaluacion": 201, "calificacionPromedioActualizada": 4.85 } }
```

#### Clientes

| Método | Ruta | Descripción |
|---|---|---|
| `GET`  | `/clientes` | Lista |
| `GET`  | `/clientes/{id}` | Detalle |
| `GET`  | `/clientes/dni/{dni}` | Buscar por DNI |
| `POST` | `/clientes` | Registrar nuevo cliente |
| `PUT`  | `/clientes/{id}` | Actualizar |

### 8.3 Códigos de error estándar

| Código | Significado | Cuándo |
|---|---|---|
| 200 | OK | Operación exitosa |
| 201 | Created | Reserva/Pago/Evaluación creado |
| 400 | Bad Request | JSON inválido, fechas incoherentes, parámetros faltantes |
| 401 | Unauthorized | JWT ausente, expirado o inválido |
| 403 | Forbidden | Rol sin permiso para esa operación |
| 404 | Not Found | ID no existe |
| 409 | Conflict | Espacio no disponible, descuento agotado |
| 500 | Server Error | Falla BD o email |

---

## 9. Estructura del proyecto Maven

```
SistemaReservas/
├── src/
│   ├── main/
│   │   ├── java/com/spacework/
│   │   │   ├── SpaceWorkApplication.java
│   │   │   ├── controller/api/
│   │   │   │   ├── AuthRestController.java
│   │   │   │   ├── EspacioRestController.java
│   │   │   │   ├── ReservaRestController.java
│   │   │   │   ├── PagoRestController.java
│   │   │   │   ├── DescuentoRestController.java
│   │   │   │   ├── EvaluacionRestController.java
│   │   │   │   └── ClienteRestController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── ReservaService.java
│   │   │   │   ├── PagoService.java
│   │   │   │   ├── EspacioService.java
│   │   │   │   ├── DescuentoService.java
│   │   │   │   ├── EvaluacionService.java
│   │   │   │   └── EmailService.java
│   │   │   ├── dao/
│   │   │   │   ├── UsuarioDAO.java
│   │   │   │   ├── ClienteDAO.java
│   │   │   │   ├── EspacioDAO.java
│   │   │   │   ├── ReservaDAO.java
│   │   │   │   ├── PagoDAO.java
│   │   │   │   ├── DescuentoDAO.java
│   │   │   │   ├── TokenEvaluacionDAO.java
│   │   │   │   ├── EvaluacionDAO.java
│   │   │   │   └── AuditoriaDAO.java
│   │   │   ├── model/
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Rol.java
│   │   │   │   ├── Cliente.java
│   │   │   │   ├── Espacio.java
│   │   │   │   ├── Reserva.java
│   │   │   │   ├── Pago.java
│   │   │   │   ├── Descuento.java
│   │   │   │   ├── TokenEvaluacion.java
│   │   │   │   ├── Evaluacion.java
│   │   │   │   └── enums/  (EstadoReserva, EstadoPago, MetodoPago...)
│   │   │   ├── strategy/
│   │   │   │   ├── EstrategiaPago.java          (interface)
│   │   │   │   ├── PagoEfectivo.java
│   │   │   │   ├── PagoTarjeta.java
│   │   │   │   ├── PagoTransferencia.java
│   │   │   │   └── EstrategiaPagoFactory.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── JwtFilter.java
│   │   │   │   └── CorsConfig.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── ConflictException.java
│   │   │   │   └── BusinessException.java
│   │   │   └── util/
│   │   │       ├── Conexion.java       (Singleton)
│   │   │       ├── EmailUtil.java
│   │   │       ├── HashUtil.java       (SHA-256 + salt)
│   │   │       ├── JwtUtil.java
│   │   │       └── TokenGenerator.java (32 chars alfanuméricos)
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── mail.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── login.html
│   │           ├── dashboard.html
│   │           ├── espacio-detalle.html
│   │           ├── pago.html
│   │           ├── evaluacion.html
│   │           ├── css/style.css
│   │           └── js/
│   │               ├── app.js
│   │               ├── auth.js
│   │               ├── reservas.js
│   │               ├── pagos.js
│   │               └── evaluacion.js
│   └── test/
│       └── java/com/spacework/
│           ├── service/  (pruebas unitarias con Mockito)
│           └── dao/      (pruebas de integración con H2)
├── sql/
│   ├── 00_crear_usuario.sql
│   ├── 01_crear_tablas.sql
│   ├── 02_crear_secuencias.sql       (incluido en 01)
│   ├── 03_crear_triggers.sql
│   └── 04_datos_iniciales.sql
├── lib/
│   └── ojdbc8.jar
├── docs/
│   ├── arquitectura.md
│   └── api-spec.yaml
├── pom.xml
└── README.md
```

---

## 10. Configuración

### 10.1 `application.properties`

```properties
# Servidor
server.port=${SERVER_PORT:8080}
server.servlet.context-path=/

# Base de datos Oracle
spring.datasource.url=jdbc:oracle:thin:@${DB_HOST:localhost}:${DB_PORT:1521}:${DB_SID:XE}
spring.datasource.username=${DB_USER:spacework}
spring.datasource.password=${DB_PASSWORD:spacework}
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# Pool de conexiones (HikariCP)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000

# JWT
jwt.secret=${JWT_SECRET:CHANGE_ME_IN_PROD_MIN_256_BITS}
jwt.expiration-ms=3600000

# Logging
logging.level.root=${LOG_LEVEL:INFO}
logging.level.com.spacework=DEBUG

# Mail (importado desde mail.properties)
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USER}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 10.2 `pom.xml` — dependencias clave

```xml
<dependencies>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
  </dependency>
  <dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc8</artifactId>
    <version>19.3.0.0</version>
  </dependency>
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
  </dependency>
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
  </dependency>
</dependencies>
```

---

## 11. Plan de implementación sugerido (orden para Copilot)

1. **Base de datos**: ejecutar `sql/00_crear_usuario.sql` → `01_crear_tablas.sql` → `03_crear_triggers.sql` → `04_datos_iniciales.sql`.
2. **Esqueleto Maven + Spring Boot**: `pom.xml`, `SpaceWorkApplication.java`, `application.properties`.
3. **Utilidades base**: `Conexion` (Singleton), `HashUtil`, `JwtUtil`, `TokenGenerator`, `EmailUtil`.
4. **Modelos POJO** (`model/`): uno por entidad, getters/setters/constructores.
5. **DAOs** (`dao/`): JDBC puro con `PreparedStatement`. Empezar por `UsuarioDAO` y `ClienteDAO`.
6. **Strategy de pagos**: interface + 3 implementaciones + Factory.
7. **Services**: `AuthService`, `EspacioService`, `ReservaService`, `PagoService` (orquesta strategy + descuento + token + email), `EvaluacionService`.
8. **Config seguridad**: `SecurityConfig`, `JwtFilter`, `GlobalExceptionHandler`.
9. **Controllers REST**: uno por dominio, validar input con `@Valid`.
10. **Frontend estático**: `login.html` → `dashboard.html` → `espacio-detalle.html` → `pago.html` → `evaluacion.html`. Todo con `fetch` contra `/api`.
11. **Tests**: unitarios de Services con Mockito, integración de DAOs.

---

## 12. Identidad visual

- **Color primario:** rojo institucional UTP (`#C8102E` aprox).
- **Color secundario:** gris claro (`#F5F5F5` fondo), blanco para cards.
- **Tipografía:** sans-serif moderna (Inter, Roboto o system-ui).
- **Componentes:** estilo Material-like (cards con sombra ligera, botones con radio 4–8px).
- **Badges de estado:**
  - `CONFIRMADA` → verde
  - `COMPLETADA` → azul
  - `PENDIENTE` / `PAGADA` → amarillo
  - `CANCELADA` → rojo
- **Accesibilidad:** cumplir WCAG 2.1 AA — contrastes mínimos, labels en inputs, jerarquía semántica de headings.

---

## 13. Reglas críticas que Copilot no debe romper

1. **Nunca guardar passwords en texto plano.** Siempre `SHA-256(password + salt)` con salt único por usuario.
2. **Toda mutación de RESERVAS, PAGOS, DESCUENTOS pasa por trigger de auditoría.** No bypassear.
3. **El token de evaluación es de un solo uso.** Una vez `utilizado=1`, no se permite editar la evaluación.
4. **Una reserva tiene exactamente un pago** (UNIQUE en `PAGOS.id_reserva`).
5. **IGV se calcula sobre el monto descontado, no sobre el subtotal original.**
6. **`fecha_fin > fecha_inicio` siempre** (constraint a nivel BD).
7. **El JWT secret debe leerse de variable de entorno.** Nunca commitear el valor real.
8. **Verificar disponibilidad antes de crear reserva**: comprobar solapamiento contra `RESERVAS` (estados PENDIENTE/CONFIRMADA) **y** `HORARIOS_BLOQUEADOS`.
9. **Strategy pattern obligatorio para métodos de pago.** Agregar un nuevo método = nueva clase, no modificar `Pago`.
10. **DAO devuelve `Optional<T>` o `List<T>`, nunca `null`.**

---

**FIN DEL DOCUMENTO**
