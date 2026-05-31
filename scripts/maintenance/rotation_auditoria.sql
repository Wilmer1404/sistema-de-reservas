-- rotation_auditoria.sql
-- Rotar/Archivar registros históricos de auditoría
-- Ubicación: scripts/maintenance/rotation_auditoria.sql
-- Cron: 0 3 1 * * (1° del mes 03:00)

SET ECHO ON;
SET TIMING ON;
SET PAGESIZE 50;
SET LINESIZE 120;

SPOOL rotation_auditoria.log;

-- ============================================
-- 1. CREAR TABLA DE ARCHIVO (si no existe)
-- ============================================
BEGIN
    EXECUTE IMMEDIATE 'CREATE TABLE AUDITORIA_ARCHIVO AS SELECT * FROM AUDITORIA WHERE 1=0';
    DBMS_OUTPUT.PUT_LINE('Tabla AUDITORIA_ARCHIVO creada exitosamente');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -955 THEN
            DBMS_OUTPUT.PUT_LINE('Tabla AUDITORIA_ARCHIVO ya existe');
        ELSE
            RAISE;
        END IF;
END;
/

-- ============================================
-- 2. INSERTAR REGISTROS HISTÓRICOS (> 90 días)
-- ============================================
BEGIN
    INSERT INTO AUDITORIA_ARCHIVO
    SELECT * FROM AUDITORIA
    WHERE fecha_hora < TRUNC(SYSDATE - 90)
      AND id_auditoria NOT IN (SELECT id_auditoria FROM AUDITORIA_ARCHIVO);

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Registros insertados: ' || SQL%ROWCOUNT);
END;
/

-- ============================================
-- 3. ELIMINAR REGISTROS HISTÓRICOS
-- ============================================
BEGIN
    DELETE FROM AUDITORIA
    WHERE fecha_hora < TRUNC(SYSDATE - 90);

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Registros eliminados: ' || SQL%ROWCOUNT);
END;
/

-- ============================================
-- 4. OPTIMIZAR TABLA
-- ============================================
BEGIN
    DBMS_OUTPUT.PUT_LINE('Analizando tabla AUDITORIA...');
    DBMS_STATS.GATHER_TABLE_STATS(
        ownname => 'SPACEWORK',
        tabname => 'AUDITORIA'
    );
    DBMS_OUTPUT.PUT_LINE('Análisis completado');
END;
/

-- ============================================
-- 5. CREAR ÍNDICE PARA ARCHIVO (si no existe)
-- ============================================
BEGIN
    EXECUTE IMMEDIATE 'CREATE INDEX IDX_AUD_ARCH_FECHA ON AUDITORIA_ARCHIVO(fecha_hora)';
    DBMS_OUTPUT.PUT_LINE('Índice creado: IDX_AUD_ARCH_FECHA');
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -955 THEN
            DBMS_OUTPUT.PUT_LINE('Índice IDX_AUD_ARCH_FECHA ya existe');
        ELSE
            RAISE;
        END IF;
END;
/

-- ============================================
-- 6. REPORTE DE ROTACIÓN
-- ============================================
PROMPT ========================================
PROMPT  REPORTE DE ROTACIÓN DE AUDITORÍA
PROMPT ========================================

PROMPT
PROMPT Total de registros en AUDITORIA:
SELECT COUNT(*) AS total_vigentes FROM AUDITORIA;

PROMPT
PROMPT Total de registros archivados:
SELECT COUNT(*) AS total_archivados FROM AUDITORIA_ARCHIVO;

PROMPT
PROMPT Rango de fechas en tabla vigente:
SELECT MIN(fecha_hora) AS fecha_inicio,
       MAX(fecha_hora) AS fecha_final,
       COUNT(*) AS cantidad
FROM AUDITORIA;

PROMPT
PROMPT Rango de fechas en tabla archivo:
SELECT MIN(fecha_hora) AS fecha_inicio,
       MAX(fecha_hora) AS fecha_final,
       COUNT(*) AS cantidad
FROM AUDITORIA_ARCHIVO;

PROMPT
PROMPT Registros por tipo (vigentes):
SELECT tipo_cambio, COUNT(*) AS cantidad
FROM AUDITORIA
GROUP BY tipo_cambio
ORDER BY cantidad DESC;

PROMPT
PROMPT Registros por tabla (vigentes):
SELECT tabla, COUNT(*) AS cantidad
FROM AUDITORIA
GROUP BY tabla
ORDER BY cantidad DESC;

PROMPT
PROMPT Espacio ocupado:
SELECT
    ROUND(SUM(bytes) / 1024 / 1024, 2) AS tamaño_MB,
    segment_name
FROM dba_segments
WHERE owner = 'SPACEWORK'
  AND segment_name IN ('AUDITORIA', 'AUDITORIA_ARCHIVO')
GROUP BY segment_name;

PROMPT
PROMPT ========================================
PROMPT Rotación completada exitosamente
PROMPT Timestamp: TO_CHAR(SYSDATE, 'YYYY-MM-DD HH24:MI:SS')
PROMPT ========================================

SPOOL OFF;
