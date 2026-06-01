-- ============================================================
--  SpaceWork - Sistema de Gestión de Reservas
--  Script de Base de Datos - Oracle XE
--
--  Instrucciones de ejecución:
--    1. Conectarse como SYSTEM y ejecutar la sección (1) USUARIO
--    2. Conectarse como el usuario creado y ejecutar el resto
-- ============================================================


-- ============================================================
-- (1) USUARIO Y PRIVILEGIOS
--     Ejecutar conectado como SYSTEM
-- ============================================================

CREATE USER spacework IDENTIFIED BY spacework;

GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE SEQUENCE,
      CREATE TRIGGER, CREATE PROCEDURE, CREATE TABLE TO spacework;

ALTER USER spacework QUOTA UNLIMITED ON USERS;

GRANT CREATE ANY SEQUENCE TO spacework;
GRANT CREATE ANY TRIGGER  TO spacework;

COMMIT;


-- ============================================================
-- (2) TABLAS
--     Ejecutar conectado como spacework (o el usuario de la app)
-- ============================================================

-- Roles del sistema
CREATE TABLE ROLES (
    id_rol          NUMBER          PRIMARY KEY,
    nombre          VARCHAR2(50)    NOT NULL UNIQUE,
    descripcion     VARCHAR2(255),
    permisos        CLOB,
    fecha_creacion  DATE            DEFAULT SYSDATE
);

-- Usuarios administradores
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

-- Clientes del sistema
CREATE TABLE CLIENTES (
    id_cliente     NUMBER         PRIMARY KEY,
    nombre         VARCHAR2(100)  NOT NULL,
    apellido       VARCHAR2(100)  NOT NULL,
    dni            VARCHAR2(20)   NOT NULL UNIQUE,
    email          VARCHAR2(100)  UNIQUE,
    telefono       VARCHAR2(20),
    password       VARCHAR2(128),
    estado         VARCHAR2(20)   DEFAULT 'ACTIVO'
                                  CHECK (estado IN ('ACTIVO','INACTIVO')),
    fecha_registro DATE           DEFAULT SYSDATE
);

-- Espacios disponibles para reserva
CREATE TABLE ESPACIOS (
    id_espacio       NUMBER         PRIMARY KEY,
    nombre           VARCHAR2(100)  NOT NULL,
    tipo             VARCHAR2(50)   NOT NULL,
    capacidad        NUMBER         NOT NULL CHECK (capacidad > 0),
    ubicacion        VARCHAR2(255),
    precio_por_hora  NUMBER(10,2)   NOT NULL CHECK (precio_por_hora >= 0),
    descripcion      CLOB,
    imagen_url       CLOB,
    estado           VARCHAR2(20)   DEFAULT 'ACTIVO'
                                    CHECK (estado IN ('ACTIVO','INACTIVO','MANTENIMIENTO')),
    fecha_creacion   DATE           DEFAULT SYSDATE
);

-- Códigos de descuento
CREATE TABLE DESCUENTOS (
    id_descuento   NUMBER         PRIMARY KEY,
    codigo         VARCHAR2(20)   NOT NULL UNIQUE,
    descripcion    VARCHAR2(255),
    porcentaje     NUMBER(5,2)    NOT NULL CHECK (porcentaje > 0 AND porcentaje <= 100),
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

-- Reservas de espacios
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

-- Pagos asociados a reservas
CREATE TABLE PAGOS (
    id_pago             NUMBER         PRIMARY KEY,
    id_reserva          NUMBER         NOT NULL,
    id_descuento        NUMBER,
    monto               NUMBER(10,2)   NOT NULL CHECK (monto >= 0),
    monto_final         NUMBER(10,2)   DEFAULT 0,
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

-- Horarios de operación de cada espacio por día de semana
CREATE TABLE HORARIOS (
    id_horario      NUMBER         PRIMARY KEY,
    id_espacio      NUMBER         NOT NULL,
    dia_semana      NUMBER(1)      NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
    hora_apertura   VARCHAR2(5)    NOT NULL,
    hora_cierre     VARCHAR2(5)    NOT NULL,
    estado          VARCHAR2(20)   DEFAULT 'ACTIVO'
                                   CHECK (estado IN ('ACTIVO','INACTIVO')),
    CONSTRAINT fk_horario_espacio FOREIGN KEY (id_espacio) REFERENCES ESPACIOS(id_espacio),
    CONSTRAINT uq_horario_espacio_dia UNIQUE (id_espacio, dia_semana)
);

-- Horarios bloqueados por espacio
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

-- Tokens para el formulario de evaluación (enviados por email)
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

-- Evaluaciones de los clientes
CREATE TABLE EVALUACIONES (
    id_evaluacion    NUMBER         PRIMARY KEY,
    id_reserva       NUMBER         NOT NULL UNIQUE,
    id_cliente       NUMBER         NOT NULL,
    calificacion     NUMBER(2,1)    NOT NULL CHECK (calificacion >= 1 AND calificacion <= 5),
    comentario       CLOB,
    fecha_evaluacion DATE           DEFAULT SYSDATE,
    CONSTRAINT fk_eval_reserva FOREIGN KEY (id_reserva) REFERENCES RESERVAS(id_reserva),
    CONSTRAINT fk_eval_cliente FOREIGN KEY (id_cliente) REFERENCES CLIENTES(id_cliente)
);

-- Notificaciones internas para administradores
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

-- Auditoría de operaciones críticas
CREATE TABLE AUDITORIA (
    id_auditoria      NUMBER         PRIMARY KEY,
    id_usuario        NUMBER,
    tabla_modificada  VARCHAR2(50)   NOT NULL,
    operacion         VARCHAR2(20)   NOT NULL CHECK (operacion IN ('INSERT','UPDATE','DELETE')),
    datos_antiguos    CLOB,
    datos_nuevos      CLOB,
    fecha_operacion   DATE           DEFAULT SYSDATE,
    ip_origen         VARCHAR2(45),
    CONSTRAINT fk_audit_usuario FOREIGN KEY (id_usuario) REFERENCES USUARIOS(id_usuario)
);

COMMIT;


-- ============================================================
-- (3) ÍNDICES
-- ============================================================

CREATE INDEX idx_usuario_username    ON USUARIOS(username);
CREATE INDEX idx_usuario_email       ON USUARIOS(email);
CREATE INDEX idx_cliente_dni         ON CLIENTES(dni);
CREATE INDEX idx_cliente_email       ON CLIENTES(email);
CREATE INDEX idx_espacio_tipo        ON ESPACIOS(tipo);
CREATE INDEX idx_espacio_estado      ON ESPACIOS(estado);
CREATE INDEX idx_descuento_codigo    ON DESCUENTOS(codigo);
CREATE INDEX idx_descuento_estado    ON DESCUENTOS(estado);
CREATE INDEX idx_reserva_cliente     ON RESERVAS(id_cliente);
CREATE INDEX idx_reserva_espacio     ON RESERVAS(id_espacio);
CREATE INDEX idx_reserva_fechas      ON RESERVAS(fecha_inicio, fecha_fin);
CREATE INDEX idx_reserva_estado      ON RESERVAS(estado);
CREATE INDEX idx_pago_estado         ON PAGOS(estado_pago);
CREATE INDEX idx_pago_reserva        ON PAGOS(id_reserva);
CREATE INDEX idx_horario_espacio     ON HORARIOS(id_espacio);
CREATE INDEX idx_horario_dia         ON HORARIOS(dia_semana);
CREATE INDEX idx_bloqueo_espacio     ON HORARIOS_BLOQUEADOS(id_espacio, fecha_inicio, fecha_fin);
CREATE INDEX idx_token_value         ON TOKENS_EVALUACION(token);
CREATE INDEX idx_eval_cliente        ON EVALUACIONES(id_cliente);
CREATE INDEX idx_eval_reserva        ON EVALUACIONES(id_reserva);
CREATE INDEX idx_notif_usuario_leida ON NOTIFICACIONES(id_usuario, leida);
CREATE INDEX idx_audit_tabla         ON AUDITORIA(tabla_modificada);
CREATE INDEX idx_audit_fecha         ON AUDITORIA(fecha_operacion);

COMMIT;


-- ============================================================
-- (4) SECUENCIAS
-- ============================================================

CREATE SEQUENCE SEQ_ROLES        START WITH 1    INCREMENT BY 1;
CREATE SEQUENCE SEQ_USUARIOS     START WITH 1    INCREMENT BY 1;
CREATE SEQUENCE SEQ_CLIENTES     START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE SEQ_ESPACIOS     START WITH 1    INCREMENT BY 1;
CREATE SEQUENCE SEQ_DESCUENTOS   START WITH 1    INCREMENT BY 1;
CREATE SEQUENCE SEQ_RESERVAS     START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE SEQ_PAGOS        START WITH 1    INCREMENT BY 1;
CREATE SEQUENCE SEQ_HORARIOS     START WITH 1    INCREMENT BY 1;
CREATE SEQUENCE SEQ_BLOQUEOS     START WITH 1    INCREMENT BY 1;
CREATE SEQUENCE SEQ_TOKENS_EVALUACION START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE SEQ_EVALUACIONES START WITH 1    INCREMENT BY 1;
CREATE SEQUENCE SEQ_NOTIFICACIONES START WITH 1  INCREMENT BY 1;
CREATE SEQUENCE SEQ_AUDITORIA    START WITH 1    INCREMENT BY 1;

COMMIT;


-- ============================================================
-- (5) TRIGGERS DE AUDITORÍA
-- ============================================================

-- Auditoría de cambios en reservas
CREATE OR REPLACE TRIGGER trg_audit_reservas
AFTER INSERT OR UPDATE OR DELETE ON RESERVAS
FOR EACH ROW
DECLARE
    v_op  VARCHAR2(20);
    v_old CLOB;
    v_new CLOB;
BEGIN
    IF INSERTING THEN
        v_op  := 'INSERT';
        v_new := 'id=' || :NEW.id_reserva || ',cliente=' || :NEW.id_cliente
               || ',espacio=' || :NEW.id_espacio || ',estado=' || :NEW.estado
               || ',monto=' || :NEW.monto_total;
    ELSIF UPDATING THEN
        v_op  := 'UPDATE';
        v_old := 'estado=' || :OLD.estado || ',monto=' || :OLD.monto_total;
        v_new := 'estado=' || :NEW.estado || ',monto=' || :NEW.monto_total;
    ELSIF DELETING THEN
        v_op  := 'DELETE';
        v_old := 'id=' || :OLD.id_reserva || ',estado=' || :OLD.estado;
    END IF;

    INSERT INTO AUDITORIA (id_auditoria, tabla_modificada, operacion,
                           datos_antiguos, datos_nuevos, fecha_operacion)
    VALUES (SEQ_AUDITORIA.NEXTVAL, 'RESERVAS', v_op, v_old, v_new, SYSDATE);
END;
/

-- Auditoría de cambios en pagos
CREATE OR REPLACE TRIGGER trg_audit_pagos
AFTER INSERT OR UPDATE OR DELETE ON PAGOS
FOR EACH ROW
DECLARE
    v_op  VARCHAR2(20);
    v_old CLOB;
    v_new CLOB;
BEGIN
    IF INSERTING THEN
        v_op  := 'INSERT';
        v_new := 'id=' || :NEW.id_pago || ',reserva=' || :NEW.id_reserva
               || ',monto=' || :NEW.monto || ',estado=' || :NEW.estado_pago;
    ELSIF UPDATING THEN
        v_op  := 'UPDATE';
        v_old := 'estado=' || :OLD.estado_pago || ',monto_final=' || :OLD.monto_final;
        v_new := 'estado=' || :NEW.estado_pago || ',monto_final=' || :NEW.monto_final;
    ELSIF DELETING THEN
        v_op  := 'DELETE';
        v_old := 'id=' || :OLD.id_pago || ',estado=' || :OLD.estado_pago;
    END IF;

    INSERT INTO AUDITORIA (id_auditoria, tabla_modificada, operacion,
                           datos_antiguos, datos_nuevos, fecha_operacion)
    VALUES (SEQ_AUDITORIA.NEXTVAL, 'PAGOS', v_op, v_old, v_new, SYSDATE);
END;
/

-- Auditoría de cambios en descuentos
CREATE OR REPLACE TRIGGER trg_audit_descuentos
AFTER INSERT OR UPDATE OR DELETE ON DESCUENTOS
FOR EACH ROW
DECLARE
    v_op  VARCHAR2(20);
    v_old CLOB;
    v_new CLOB;
BEGIN
    IF INSERTING THEN
        v_op  := 'INSERT';
        v_new := 'codigo=' || :NEW.codigo || ',porcentaje=' || :NEW.porcentaje;
    ELSIF UPDATING THEN
        v_op  := 'UPDATE';
        v_old := 'usos=' || :OLD.usos_actuales || ',estado=' || :OLD.estado;
        v_new := 'usos=' || :NEW.usos_actuales || ',estado=' || :NEW.estado;
    ELSIF DELETING THEN
        v_op  := 'DELETE';
        v_old := 'codigo=' || :OLD.codigo;
    END IF;

    INSERT INTO AUDITORIA (id_auditoria, tabla_modificada, operacion,
                           datos_antiguos, datos_nuevos, fecha_operacion)
    VALUES (SEQ_AUDITORIA.NEXTVAL, 'DESCUENTOS', v_op, v_old, v_new, SYSDATE);
END;
/

COMMIT;


-- ============================================================
-- (6) DATOS INICIALES
-- ============================================================

-- Roles
INSERT INTO ROLES (id_rol, nombre, descripcion, permisos) VALUES
  (SEQ_ROLES.NEXTVAL, 'ADMIN', 'Administrador total del sistema',
   '["USUARIO_*","ESPACIO_*","RESERVA_*","PAGO_*","DESCUENTO_*","AUDITORIA_READ"]');

INSERT INTO ROLES (id_rol, nombre, descripcion, permisos) VALUES
  (SEQ_ROLES.NEXTVAL, 'RECEPCIONISTA', 'Gestiona reservas y pagos',
   '["RESERVA_READ","RESERVA_CREATE","PAGO_READ","PAGO_UPDATE"]');

INSERT INTO ROLES (id_rol, nombre, descripcion, permisos) VALUES
  (SEQ_ROLES.NEXTVAL, 'GERENTE', 'Consulta reportes y estadísticas',
   '["RESERVA_READ","PAGO_READ","DESCUENTO_READ","AUDITORIA_READ"]');

-- Usuario administrador por defecto
-- Contraseña: admin123  (hash SHA-256)
INSERT INTO USUARIOS (id_usuario, username, password, salt, nombre, email, id_rol) VALUES
  (SEQ_USUARIOS.NEXTVAL, 'admin',
   '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
   'spacework2026',
   'Administrador', 'admin@spacework.pe', 1);

-- Espacios de ejemplo
INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, descripcion) VALUES
  (SEQ_ESPACIOS.NEXTVAL, 'Sala de Reuniones A', 'SALA_REUNION', 10, 'Piso 1', 50.00,
   'Sala equipada con proyector y pizarra.');

INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, descripcion) VALUES
  (SEQ_ESPACIOS.NEXTVAL, 'Auditorio Principal', 'AUDITORIO', 80, 'Piso 1', 200.00,
   'Auditorio con sistema de sonido y proyección.');

INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, descripcion) VALUES
  (SEQ_ESPACIOS.NEXTVAL, 'Oficina Coworking', 'COWORKING', 15, 'Piso 2', 35.00,
   'Espacio colaborativo con WiFi de alta velocidad.');

INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, descripcion) VALUES
  (SEQ_ESPACIOS.NEXTVAL, 'Sala Ejecutiva', 'SALA_REUNION', 8, 'Piso 3', 80.00,
   'Sala ejecutiva con videoconferencia HD.');

-- Horarios de operación (dia_semana: 1=Lunes, 2=Mar, ..., 6=Sab, 7=Dom)
-- Espacio 1: Sala de Reuniones A
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 1, 1, '08:00', '20:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 1, 2, '08:00', '20:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 1, 3, '08:00', '20:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 1, 4, '08:00', '20:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 1, 5, '08:00', '20:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 1, 6, '09:00', '17:00');

-- Espacio 2: Auditorio Principal
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 2, 1, '08:00', '22:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 2, 2, '08:00', '22:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 2, 3, '08:00', '22:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 2, 4, '08:00', '22:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 2, 5, '08:00', '22:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 2, 6, '09:00', '18:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 2, 7, '09:00', '18:00');

-- Espacio 3: Oficina Coworking
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 3, 1, '07:00', '22:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 3, 2, '07:00', '22:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 3, 3, '07:00', '22:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 3, 4, '07:00', '22:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 3, 5, '07:00', '22:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 3, 6, '08:00', '18:00');

-- Espacio 4: Sala Ejecutiva
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 4, 1, '08:00', '19:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 4, 2, '08:00', '19:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 4, 3, '08:00', '19:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 4, 4, '08:00', '19:00');
INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre) VALUES (SEQ_HORARIOS.NEXTVAL, 4, 5, '08:00', '19:00');

-- Descuentos de ejemplo
INSERT INTO DESCUENTOS (id_descuento, codigo, descripcion, porcentaje, monto_minimo,
                        usos_maximos, fecha_inicio, fecha_fin) VALUES
  (SEQ_DESCUENTOS.NEXTVAL, 'BIENVENIDA10', 'Descuento de bienvenida',
   10.0, 100.00, 100, SYSDATE, SYSDATE + 90);

INSERT INTO DESCUENTOS (id_descuento, codigo, descripcion, porcentaje, monto_minimo,
                        usos_maximos, fecha_inicio, fecha_fin) VALUES
  (SEQ_DESCUENTOS.NEXTVAL, 'PROMO20', 'Promoción especial 20%',
   20.0, 200.00, 50, SYSDATE, SYSDATE + 30);

COMMIT;
