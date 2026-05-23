-- ============================================================
-- 00_crear_usuario.sql
-- Sistema de Reservas SpaceWork Perú S.A.C.
-- Ejecutar conectado como SYSTEM en Oracle XE
-- ============================================================

-- Crear usuario spacework
CREATE USER spacework IDENTIFIED BY spacework;

-- Otorgar privilegios
GRANT CONNECT, RESOURCE, CREATE VIEW, CREATE SEQUENCE,
      CREATE TRIGGER, CREATE PROCEDURE, CREATE TABLE TO spacework;

-- Asignar cuota ilimitada
ALTER USER spacework QUOTA UNLIMITED ON USERS;

-- Otorgar permisos adicionales para secuencias y triggers
GRANT CREATE ANY SEQUENCE TO spacework;
GRANT CREATE ANY TRIGGER TO spacework;

COMMIT;
