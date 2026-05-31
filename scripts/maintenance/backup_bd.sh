#!/bin/bash
# backup_bd.sh - Backup diario de BD Oracle SpaceWork
# Ubicación: scripts/maintenance/backup_bd.sh
# Cron: 0 21 * * * /scripts/maintenance/backup_bd.sh

set -e

FECHA=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="backup"
BACKUP_FILE="$BACKUP_DIR/spacework_$FECHA.sql"
LOG_FILE="$BACKUP_DIR/backup_$FECHA.log"

# Crear directorio si no existe
mkdir -p "$BACKUP_DIR"

echo "=== Iniciando Backup de BD SpaceWork ===" | tee "$LOG_FILE"
echo "Fecha: $FECHA" | tee -a "$LOG_FILE"
echo "Archivo: $BACKUP_FILE" | tee -a "$LOG_FILE"

# Exportar BD con Oracle exp (o expdp para versiones nuevas)
# Este es un ejemplo para JDBC/SQLPlus
sqlplus -S system/password @<<EOF >> "$LOG_FILE" 2>&1
SET ECHO OFF
SET FEEDBACK OFF
SET HEADING OFF
SET PAGESIZE 0
SET TERMOUT OFF
SPOOL $BACKUP_FILE
SELECT DBMS_METADATA.GET_DDL(object_type, object_name, owner)
FROM DBA_OBJECTS
WHERE owner = 'SPACEWORK';
SPOOL OFF
EXIT;
EOF

# Comprimir backup
if [ -f "$BACKUP_FILE" ]; then
    echo "Comprimiendo backup..." | tee -a "$LOG_FILE"
    gzip "$BACKUP_FILE"
    BACKUP_FILE="${BACKUP_FILE}.gz"
    echo "[OK] Backup comprimido exitosamente" | tee -a "$LOG_FILE"
else
    echo "[ERROR] Archivo de backup no creado" | tee -a "$LOG_FILE"
    exit 1
fi

# Limpiar backups antiguos (mantener últimos 30)
echo "Limpiando backups antiguos..." | tee -a "$LOG_FILE"
find "$BACKUP_DIR" -name "spacework_*.sql.gz" -mtime +30 -delete
echo "Limpieza completada" | tee -a "$LOG_FILE"

# Verificar tamaño
SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
echo "Tamaño del backup: $SIZE" | tee -a "$LOG_FILE"

# Verifica si copia remota debe hacerse (opcional)
# scp "$BACKUP_FILE" backup_server:/remote_backup/ 2>/dev/null || true

echo "=== Backup completado exitosamente ===" | tee -a "$LOG_FILE"
echo "" | tee -a "$LOG_FILE"
