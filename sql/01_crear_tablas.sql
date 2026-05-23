-- ============================================================
-- 01_crear_tablas.sql
-- Sistema de Reservas SpaceWork Perú S.A.C.
-- Ejecutar conectado como usuario: spacework
-- ============================================================

-- TABLA ROLES
CREATE TABLE ROLES (
    id_rol          NUMBER          PRIMARY KEY,
    nombre          VARCHAR2(50)    NOT NULL UNIQUE,
    descripcion     VARCHAR2(255),
    permisos        CLOB,
    fecha_creacion  DATE            DEFAULT SYSDATE
);

-- TABLA USUARIOS
CREATE TABLE USUARIOS (
    id_usuario          NUMBER         PRIMARY KEY,
    username            VARCHAR2(50)   NOT NULL UNIQUE,
    password            VARCHAR2(255)  NOT NULL,
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

-- TABLA CLIENTES
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

-- TABLA ESPACIOS
CREATE TABLE ESPACIOS (
    id_espacio       NUMBER         PRIMARY KEY,
    nombre           VARCHAR2(100)  NOT NULL,
    tipo             VARCHAR2(50)   NOT NULL,
    capacidad        NUMBER         NOT NULL CHECK (capacidad > 0),
    ubicacion        VARCHAR2(255),
    precio_por_hora  NUMBER(10,2)   NOT NULL CHECK (precio_por_hora >= 0),
    descripcion      CLOB,
    imagen_url       VARCHAR2(500),
    estado           VARCHAR2(20)   DEFAULT 'ACTIVO'
                                    CHECK (estado IN ('ACTIVO','INACTIVO','MANTENIMIENTO')),
    fecha_creacion   DATE           DEFAULT SYSDATE
);

-- TABLA DESCUENTOS
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

-- TABLA RESERVAS
CREATE TABLE RESERVAS (
    id_reserva     NUMBER         PRIMARY KEY,
    id_cliente     NUMBER         NOT NULL,
    id_espacio     NUMBER         NOT NULL,
    fecha_inicio   TIMESTAMP      NOT NULL,
    fecha_fin      TIMESTAMP      NOT NULL,
    monto_total    NUMBER(10,2)   NOT NULL,
    estado         VARCHAR2(20)   DEFAULT 'PENDIENTE'
                                  CHECK (estado IN ('PENDIENTE','CONFIRMADA','COMPLETADA','CANCELADA')),
    observaciones  VARCHAR2(500),
    fecha_creacion DATE           DEFAULT SYSDATE,
    CONSTRAINT fk_reserva_cliente FOREIGN KEY (id_cliente) REFERENCES CLIENTES(id_cliente),
    CONSTRAINT fk_reserva_espacio FOREIGN KEY (id_espacio) REFERENCES ESPACIOS(id_espacio),
    CONSTRAINT chk_reserva_fechas CHECK (fecha_fin > fecha_inicio)
);

-- TABLA PAGOS
CREATE TABLE PAGOS (
    id_pago             NUMBER         PRIMARY KEY,
    id_reserva          NUMBER         NOT NULL UNIQUE,
    id_descuento        NUMBER,
    monto               NUMBER(10,2)   NOT NULL CHECK (monto >= 0),
    monto_final         NUMBER(10,2)   NOT NULL,
    descuento_aplicado  NUMBER(10,2)   DEFAULT 0,
    igv                 NUMBER(10,2)   DEFAULT 0,
    metodo_pago         VARCHAR2(50)   CHECK (metodo_pago IN ('EFECTIVO','TARJETA','TRANSFERENCIA')),
    estado_pago         VARCHAR2(20)   DEFAULT 'PENDIENTE'
                                       CHECK (estado_pago IN ('PENDIENTE','COMPLETADO','RECHAZADO')),
    referencia          VARCHAR2(100),
    fecha_creacion      DATE           DEFAULT SYSDATE,
    fecha_pago          DATE,
    CONSTRAINT fk_pago_reserva   FOREIGN KEY (id_reserva)   REFERENCES RESERVAS(id_reserva),
    CONSTRAINT fk_pago_descuento FOREIGN KEY (id_descuento) REFERENCES DESCUENTOS(id_descuento)
);

-- TABLA HORARIOS_BLOQUEADOS
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

-- TABLA TOKENS_EVALUACION
CREATE TABLE TOKENS_EVALUACION (
    id_token         NUMBER         PRIMARY KEY,
    id_pago          NUMBER         NOT NULL UNIQUE,
    token            VARCHAR2(255)  NOT NULL UNIQUE,
    email_cliente    VARCHAR2(100)  NOT NULL,
    fecha_creacion   DATE           DEFAULT SYSDATE,
    fecha_expiracion DATE           NOT NULL,
    utilizado        NUMBER(1)      DEFAULT 0 CHECK (utilizado IN (0,1)),
    CONSTRAINT fk_token_pago FOREIGN KEY (id_pago) REFERENCES PAGOS(id_pago)
);

-- TABLA EVALUACIONES
CREATE TABLE EVALUACIONES (
    id_evaluacion    NUMBER         PRIMARY KEY,
    id_reserva       NUMBER         NOT NULL UNIQUE,
    id_cliente       NUMBER         NOT NULL,
    calificacion     NUMBER(2,1)    NOT NULL
                                    CHECK (calificacion >= 1 AND calificacion <= 5),
    comentario       CLOB,
    fecha_evaluacion DATE           DEFAULT SYSDATE,
    CONSTRAINT fk_eval_reserva FOREIGN KEY (id_reserva) REFERENCES RESERVAS(id_reserva),
    CONSTRAINT fk_eval_cliente FOREIGN KEY (id_cliente) REFERENCES CLIENTES(id_cliente)
);

-- TABLA NOTIFICACIONES
CREATE TABLE NOTIFICACIONES (
    id_notificacion NUMBER         PRIMARY KEY,
    id_usuario      NUMBER         NOT NULL,
    tipo            VARCHAR2(50)   NOT NULL,
    asunto          VARCHAR2(255)  NOT NULL,
    mensaje         CLOB,
    leida           NUMBER(1)      DEFAULT 0 CHECK (leida IN (0,1)),
    fecha_creacion  DATE           DEFAULT SYSDATE,
    CONSTRAINT fk_notif_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIOS(id_usuario)
);

-- TABLA AUDITORIA
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

-- INDICES PRINCIPALES
CREATE INDEX idx_usuario_username ON USUARIOS(username);
CREATE INDEX idx_usuario_email    ON USUARIOS(email);
CREATE INDEX idx_cliente_dni      ON CLIENTES(dni);
CREATE INDEX idx_cliente_email    ON CLIENTES(email);
CREATE INDEX idx_espacio_tipo     ON ESPACIOS(tipo);
CREATE INDEX idx_espacio_estado   ON ESPACIOS(estado);
CREATE INDEX idx_descuento_codigo ON DESCUENTOS(codigo);
CREATE INDEX idx_descuento_estado ON DESCUENTOS(estado);
CREATE INDEX idx_reserva_cliente  ON RESERVAS(id_cliente);
CREATE INDEX idx_reserva_espacio  ON RESERVAS(id_espacio);
CREATE INDEX idx_reserva_fechas   ON RESERVAS(fecha_inicio, fecha_fin);
CREATE INDEX idx_reserva_estado   ON RESERVAS(estado);
CREATE INDEX idx_pago_estado      ON PAGOS(estado_pago);
CREATE INDEX idx_pago_reserva     ON PAGOS(id_reserva);
CREATE INDEX idx_bloqueo_espacio  ON HORARIOS_BLOQUEADOS(id_espacio, fecha_inicio, fecha_fin);
CREATE INDEX idx_token_value      ON TOKENS_EVALUACION(token);
CREATE INDEX idx_eval_cliente     ON EVALUACIONES(id_cliente);
CREATE INDEX idx_eval_reserva     ON EVALUACIONES(id_reserva);
CREATE INDEX idx_notif_usuario_leida ON NOTIFICACIONES(id_usuario, leida);
CREATE INDEX idx_audit_tabla      ON AUDITORIA(tabla_modificada);
CREATE INDEX idx_audit_fecha      ON AUDITORIA(fecha_operacion);

COMMIT;
