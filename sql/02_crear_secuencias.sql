-- ============================================================
-- 02_crear_secuencias.sql
-- Sistema de Reservas SpaceWork Perú S.A.C.
-- Crear todas las secuencias para las tablas
-- ============================================================

CREATE SEQUENCE seq_rol       START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_usuario   START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_cliente   START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE seq_espacio   START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_descuento START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_reserva   START WITH 1000 INCREMENT BY 1;
CREATE SEQUENCE seq_pago      START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_bloqueo   START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_token     START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_evaluacion START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_notificacion START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE seq_auditoria START WITH 1 INCREMENT BY 1;

COMMIT;
