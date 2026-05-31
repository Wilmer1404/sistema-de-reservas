#!/bin/bash
# cleanup_logs.sh - Limpiar y rotar logs de SpaceWork
# Ubicación: scripts/maintenance/cleanup_logs.sh
# Cron: 0 2 * * 0 (Domingo 02:00)

set -e

LOG_DIR="logs"
ARCHIVE_DIR="$LOG_DIR/archive"
FECHA=$(date +%Y%m%d)
CLEANUP_REPORT="$LOG_DIR/cleanup_$FECHA.report"

# Crear directorio de archivo si no existe
mkdir -p "$ARCHIVE_DIR"

echo "=== Iniciando Limpieza de Logs ===" > "$CLEANUP_REPORT"
echo "Fecha: $FECHA" >> "$CLEANUP_REPORT"
echo "Hora: $(date '+%Y-%m-%d %H:%M:%S')" >> "$CLEANUP_REPORT"
echo "" >> "$CLEANUP_REPORT"

# 1. Comprimir logs de hace 7 días
echo "Comprimiendo logs antigüos (> 7 días)..." >> "$CLEANUP_REPORT"
COMPRESSED=0
for file in "$LOG_DIR"/spacework*.log; do
    if [ -f "$file" ] && [ $(find "$file" -mtime +7 2>/dev/null | wc -l) -gt 0 ]; then
        gzip "$file" 2>/dev/null && ((COMPRESSED++)) || true
    fi
done
echo "Archivos comprimidos: $COMPRESSED" >> "$CLEANUP_REPORT"

# 2. Mover logs comprimidos a archivo
echo "Moviendo logs comprimidos al archivo..." >> "$CLEANUP_REPORT"
MOVED=0
if ls "$LOG_DIR"/*.gz 1> /dev/null 2>&1; then
    mv "$LOG_DIR"/*.gz "$ARCHIVE_DIR/" 2>/dev/null || true
    MOVED=$(ls "$ARCHIVE_DIR"/*.gz 2>/dev/null | wc -l)
fi
echo "Archivos movidos: $MOVED" >> "$CLEANUP_REPORT"

# 3. Limpiar logs de error muy antiguos (> 90 días)
echo "Eliminando logs muy antiguos (> 90 días)..." >> "$CLEANUP_REPORT"
DELETED=0
if ls "$ARCHIVE_DIR"/*.log.gz 1> /dev/null 2>&1; then
    DELETED=$(find "$ARCHIVE_DIR" -name "*.log.gz" -mtime +90 -delete -print | wc -l)
fi
echo "Archivos eliminados: $DELETED" >> "$CLEANUP_REPORT"

# 4. Listar contenido del archivo
echo "" >> "$CLEANUP_REPORT"
echo "=== Contenido del Archivo ===" >> "$CLEANUP_REPORT"
ls -lh "$ARCHIVE_DIR" | tail -20 >> "$CLEANUP_REPORT"

# 5. Calcular espacio usado
echo "" >> "$CLEANUP_REPORT"
echo "=== Espacio en Disco ===" >> "$CLEANUP_REPORT"
echo "Directorio de logs:" >> "$CLEANUP_REPORT"
du -sh "$LOG_DIR" >> "$CLEANUP_REPORT"
echo "" >> "$CLEANUP_REPORT"
echo "Directorio de archivo:" >> "$CLEANUP_REPORT"
du -sh "$ARCHIVE_DIR" >> "$CLEANUP_REPORT"

# 6. Verificar que espacios críticos no estén llenos
echo "" >> "$CLEANUP_REPORT"
echo "=== Verificación de Espacio en Disco ===" >> "$CLEANUP_REPORT"
USAGE=$(df "$LOG_DIR" | awk 'NR==2 {print $5}' | sed 's/%//')
echo "Uso de disco: ${USAGE}%" >> "$CLEANUP_REPORT"

if [ "$USAGE" -gt 80 ]; then
    echo "[WARNING] Disco al ${USAGE}%" >> "$CLEANUP_REPORT"
    # Aquí se podría enviar alerta por email
    # mail -s "ALERTA: Disco de Logs al ${USAGE}%" devops@company.com < "$CLEANUP_REPORT"
fi

if [ "$USAGE" -gt 95 ]; then
    echo "[CRÍTICO] Disco casi lleno (${USAGE}%)" >> "$CLEANUP_REPORT"
fi

echo "" >> "$CLEANUP_REPORT"
echo "=== Limpieza Completada ===" >> "$CLEANUP_REPORT"
echo "Timestamp final: $(date '+%Y-%m-%d %H:%M:%S')" >> "$CLEANUP_REPORT"

# Mostrar resumen
cat "$CLEANUP_REPORT"
