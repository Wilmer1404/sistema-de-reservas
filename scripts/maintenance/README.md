# Scripts de Mantenimiento - SpaceWork

Scripts para el mantenimiento preventivo del sistema.

---

## Contenido

| Script | Tipo | Frecuencia | Descripcion |
|--------|------|------------|-------------|
| `backup_bd.sh` | Bash | Diaria (21:00) | Backup de la base de datos Oracle |
| `cleanup_logs.sh` | Bash | Semanal (domingos) | Limpiar y comprimir logs antiguos |
| `rotation_auditoria.sql` | SQL | Mensual | Archivar registros de auditoria |
| `performance_check.sql` | SQL | Semanal | Analisis de queries lentas |

---

## Uso en Linux/Mac

Dar permisos de ejecucion:
```bash
chmod +x scripts/maintenance/*.sh
```

Configurar cron jobs:
```bash
sudo crontab -e
```

Agregar:
```
0 21 * * *   cd /ruta/SistemaReservas && scripts/maintenance/backup_bd.sh >> logs/cron.log 2>&1
0 2  * * 0   cd /ruta/SistemaReservas && scripts/maintenance/cleanup_logs.sh >> logs/cron.log 2>&1
0 3  1 * *   sqlplus system/password @scripts/maintenance/rotation_auditoria.sql >> logs/cron.log 2>&1
0 10 * * 3   sqlplus system/password @scripts/maintenance/performance_check.sql >> logs/cron.log 2>&1
```

## Uso en Windows

Usar el Programador de Tareas de Windows para ejecutar cada script en la frecuencia indicada.

---

## Descripcion de scripts

### backup_bd.sh
Genera un backup completo de la base de datos Oracle.
- Guarda el archivo en `backup/spacework_YYYYMMDD_HHMMSS.sql.gz`
- Elimina backups con mas de 30 dias automaticamente

Ejecutar manualmente:
```bash
./scripts/maintenance/backup_bd.sh
```

### cleanup_logs.sh
Rota y comprime los logs del sistema.
- Logs sin comprimir: se mantienen 7 dias
- Logs comprimidos: se mantienen 30 dias
- Logs mas antiguos: se eliminan

Ejecutar manualmente:
```bash
./scripts/maintenance/cleanup_logs.sh
```

### rotation_auditoria.sql
Archiva los registros de auditoria mayores a 90 dias en la tabla `AUDITORIA_ARCHIVO`.

Ejecutar manualmente:
```bash
sqlplus system/password @scripts/maintenance/rotation_auditoria.sql
```

### performance_check.sql
Analiza queries lentas e indices sin uso en Oracle.

Ejecutar manualmente:
```bash
sqlplus system/password @scripts/maintenance/performance_check.sql
```

---

## Solucion de problemas

**El script de backup falla:**
```bash
bash -x scripts/maintenance/backup_bd.sh
```

**Los logs no se rotan:**
```bash
ls -ld logs/
./scripts/maintenance/cleanup_logs.sh
```

**El cron no ejecuta:**
```bash
tail -50 /var/log/syslog | grep CRON
crontab -l
```

---

## Seguridad

No incluir contrasenas directamente en los scripts. Usar variables de entorno:
```bash
export DB_USER=system
export DB_PASSWORD=password
sqlplus $DB_USER/$DB_PASSWORD
```

Restringir permisos de los scripts:
```bash
chmod 700 scripts/maintenance/*.sh
```
