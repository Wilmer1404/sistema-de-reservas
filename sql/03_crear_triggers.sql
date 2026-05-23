-- ============================================================
-- 03_crear_triggers.sql
-- Sistema de Reservas SpaceWork Perú S.A.C.
-- Triggers de auditoría y validación
-- ============================================================

-- TRIGGER PARA AUDITAR CAMBIOS EN RESERVAS
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
        v_datos_new := 'id_reserva=' || :NEW.id_reserva || 
                       ',id_cliente=' || :NEW.id_cliente ||
                       ',id_espacio=' || :NEW.id_espacio ||
                       ',estado=' || :NEW.estado ||
                       ',monto_total=' || :NEW.monto_total;
    ELSIF UPDATING THEN
        v_operacion := 'UPDATE';
        v_datos_old := 'estado=' || :OLD.estado || ',monto_total=' || :OLD.monto_total;
        v_datos_new := 'estado=' || :NEW.estado || ',monto_total=' || :NEW.monto_total;
    ELSIF DELETING THEN
        v_operacion := 'DELETE';
        v_datos_old := 'id_reserva=' || :OLD.id_reserva || ',estado=' || :OLD.estado;
    END IF;

    INSERT INTO AUDITORIA (id_auditoria, tabla_modificada, operacion,
                           datos_antiguos, datos_nuevos, fecha_operacion)
    VALUES (seq_auditoria.NEXTVAL, 'RESERVAS', v_operacion,
            v_datos_old, v_datos_new, SYSDATE);
END;
/

-- TRIGGER PARA AUDITAR CAMBIOS EN PAGOS
CREATE OR REPLACE TRIGGER trg_audit_pagos
AFTER INSERT OR UPDATE OR DELETE ON PAGOS
FOR EACH ROW
DECLARE
    v_operacion VARCHAR2(20);
    v_datos_old CLOB;
    v_datos_new CLOB;
BEGIN
    IF INSERTING THEN
        v_operacion := 'INSERT';
        v_datos_new := 'id_pago=' || :NEW.id_pago || 
                       ',id_reserva=' || :NEW.id_reserva ||
                       ',monto=' || :NEW.monto ||
                       ',estado_pago=' || :NEW.estado_pago;
    ELSIF UPDATING THEN
        v_operacion := 'UPDATE';
        v_datos_old := 'estado_pago=' || :OLD.estado_pago || ',monto_final=' || :OLD.monto_final;
        v_datos_new := 'estado_pago=' || :NEW.estado_pago || ',monto_final=' || :NEW.monto_final;
    ELSIF DELETING THEN
        v_operacion := 'DELETE';
        v_datos_old := 'id_pago=' || :OLD.id_pago || ',estado_pago=' || :OLD.estado_pago;
    END IF;

    INSERT INTO AUDITORIA (id_auditoria, tabla_modificada, operacion,
                           datos_antiguos, datos_nuevos, fecha_operacion)
    VALUES (seq_auditoria.NEXTVAL, 'PAGOS', v_operacion,
            v_datos_old, v_datos_new, SYSDATE);
END;
/

-- TRIGGER PARA AUDITAR CAMBIOS EN DESCUENTOS
CREATE OR REPLACE TRIGGER trg_audit_descuentos
AFTER INSERT OR UPDATE OR DELETE ON DESCUENTOS
FOR EACH ROW
DECLARE
    v_operacion VARCHAR2(20);
    v_datos_old CLOB;
    v_datos_new CLOB;
BEGIN
    IF INSERTING THEN
        v_operacion := 'INSERT';
        v_datos_new := 'codigo=' || :NEW.codigo || ',porcentaje=' || :NEW.porcentaje;
    ELSIF UPDATING THEN
        v_operacion := 'UPDATE';
        v_datos_old := 'usos_actuales=' || :OLD.usos_actuales || ',estado=' || :OLD.estado;
        v_datos_new := 'usos_actuales=' || :NEW.usos_actuales || ',estado=' || :NEW.estado;
    ELSIF DELETING THEN
        v_operacion := 'DELETE';
        v_datos_old := 'codigo=' || :OLD.codigo;
    END IF;

    INSERT INTO AUDITORIA (id_auditoria, tabla_modificada, operacion,
                           datos_antiguos, datos_nuevos, fecha_operacion)
    VALUES (seq_auditoria.NEXTVAL, 'DESCUENTOS', v_operacion,
            v_datos_old, v_datos_new, SYSDATE);
END;
/

-- TRIGGER PARA RECALCULAR CALIFICACIÓN PROMEDIO DEL ESPACIO
CREATE OR REPLACE TRIGGER trg_recalc_calificacion
AFTER INSERT ON EVALUACIONES
FOR EACH ROW
DECLARE
    v_id_espacio NUMBER;
BEGIN
    SELECT id_espacio INTO v_id_espacio
    FROM RESERVAS WHERE id_reserva = :NEW.id_reserva;
    
    -- La calificación promedio se calcula on-demand vía query
    -- Si se quiere persistir, agregar columna calificacion_promedio a ESPACIOS
    NULL;
END;
/

COMMIT;
