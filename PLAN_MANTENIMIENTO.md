# Plan de Mantenimiento - SpaceWork

**Sistema:** Sistema de Gestión de Reservas de Espacios  
**Fecha:** Mayo 2026  
**Responsable:** Juan Pareja

---

## 1. Objetivos

- Garantizar disponibilidad y estabilidad del sistema
- Prevenir degradacion de rendimiento con el tiempo
- Mantener integridad de los datos
- Gestionar la seguridad de la aplicacion

---

## 2. Tipos de Mantenimiento

### 2.1 Mantenimiento Preventivo

| Tarea | Frecuencia | Duracion estimada |
|-------|------------|-------------------|
| Backup de base de datos | Diario (21:00) | 30 min |
| Limpieza de logs antiguos | Semanal (domingos) | 15 min |
| Analisis de rendimiento BD | Semanal | 1 hora |
| Actualizacion de dependencias | Mensual | 2 horas |
| Revision de seguridad | Trimestral | 4 horas |

### 2.2 Mantenimiento Correctivo

- Parches de seguridad criticos: Inmediatamente
- Bugs criticos: Dentro de 24 horas
- Bugs normales: Dentro de 72 horas
- Mejoras menores: Cuando sea posible

---

## 3. Backup de Base de Datos

### 3.1 Script de backup (backup_bd.sh)

```bash
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups/spacework"
mkdir -p $BACKUP_DIR

expdp system/password \
  schemas=SPACEWORK \
  directory=BACKUP_DIR \
  dumpfile=spacework_$DATE.dmp \
  logfile=spacework_$DATE.log

# Comprimir y limpiar backups de mas de 30 dias
find $BACKUP_DIR -name "*.dmp" -mtime +30 -delete
```

### 3.2 Verificacion del backup

```sql
-- Verificar integridad de tablas
SELECT table_name, num_rows
FROM user_tables
ORDER BY num_rows DESC;

-- Contar registros principales
SELECT 'CLIENTES' as tabla, COUNT(*) FROM CLIENTES
UNION ALL SELECT 'RESERVAS', COUNT(*) FROM RESERVAS
UNION ALL SELECT 'PAGOS', COUNT(*) FROM PAGOS;
```

---

## 4. Limpieza de Logs

### Script de limpieza (cleanup_logs.sh)

```bash
#!/bin/bash
LOG_DIR="./logs"

# Comprimir logs de mas de 7 dias
find $LOG_DIR -name "*.log" -mtime +7 -exec gzip {} \;

# Eliminar logs comprimidos de mas de 90 dias
find $LOG_DIR -name "*.gz" -mtime +90 -delete

echo "Limpieza completada: $(date)"
```

---

## 5. Mantenimiento de Base de Datos

### 5.1 Analisis de performance semanal

```sql
-- Queries mas lentas
SELECT sql_text, elapsed_time/executions as avg_time
FROM v$sql
WHERE executions > 0
ORDER BY avg_time DESC
FETCH FIRST 10 ROWS ONLY;

-- Indices sin uso
SELECT index_name, table_name
FROM user_indexes
WHERE index_name NOT IN (
    SELECT object_name FROM v$object_usage WHERE used = 'YES'
);

-- Espacio disponible en tablespace
SELECT tablespace_name,
       ROUND((bytes - free_space) / 1024 / 1024, 2) AS used_mb,
       ROUND(free_space / 1024 / 1024, 2) AS free_mb
FROM dba_free_space
ORDER BY free_mb ASC;
```

### 5.2 Archivo de auditoria mensual (rotation_auditoria.sql)

```sql
-- Archivar registros de auditoria mayores a 6 meses
INSERT INTO AUDITORIA_HISTORICO
SELECT * FROM AUDITORIA
WHERE fecha_accion < SYSDATE - 180;

DELETE FROM AUDITORIA
WHERE fecha_accion < SYSDATE - 180;

COMMIT;
```

---

## 6. Actualizacion de Dependencias

### 6.1 Verificar versiones desactualizadas

```bash
mvn versions:display-dependency-updates
```

### 6.2 Dependencias criticas a revisar

| Dependencia | Version actual | Frecuencia de revision |
|-------------|---------------|------------------------|
| Spring Boot | 2.7.14 | Trimestral |
| Oracle JDBC | ojdbc8 | Semestral |
| Logback | (incluida en Spring) | Con Spring Boot |

---

## 7. Seguridad

### 7.1 Revision trimestral

- Verificar que los tokens JWT expiran correctamente
- Revisar permisos de usuarios en base de datos
- Comprobar que las contrasenas estan hasheadas (BCrypt)
- Verificar configuracion CORS en `CorsConfig.java`
- Revisar logs de intentos de login fallidos

### 7.2 Actualizacion de contrasenas

```sql
-- Verificar usuarios con contrasenas vencidas
SELECT id_usuario, nombre, fecha_ultimo_login
FROM USUARIOS
WHERE fecha_ultimo_login < SYSDATE - 90;
```

---

## 8. Procedimientos de Emergencia

### La aplicacion no responde
1. Verificar proceso Java: `jps -l`
2. Revisar logs recientes: `tail -200 logs/spacework.log`
3. Reiniciar: `mvn spring-boot:run`

### La base de datos no conecta
1. Verificar que Oracle esta corriendo
2. Revisar credenciales en `Conexion.java`
3. Probar conexion: `sqlplus usuario/password@//localhost:1521/XE`

### Disco casi lleno
1. Ejecutar limpieza de logs: `./scripts/maintenance/cleanup_logs.sh`
2. Verificar backups antiguos en `/backups/`
3. Liberar espacio en tablespace Oracle si es necesario

---

**Ultima actualizacion:** Mayo 2026
