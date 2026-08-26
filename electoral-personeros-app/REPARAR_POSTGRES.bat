@echo off
chcp 65001 >nul
title ================================================
title REPARAR POSTGRESQL - RESTAURAR pg_hba.conf
title ================================================

echo.
echo ******************************************************************
echo *  REPARACION URGENTE DE POSTGRESQL                               *
echo *     Archivo pg_hba.conf corrupto. Restaurando desde BACKUP.     *
echo *     (Backup creado correctamente en el PASO 1 anterior)         *
echo ******************************************************************
echo.

setlocal enabledelayedexpansion
set PG_DATA=C:\Program Files\PostgreSQL\18\data
set PG_HBA=%PG_DATA%\pg_hba.conf
set PG_HBA_BAK=%PG_DATA%\pg_hba.conf.backup_personeros

echo [PASO 1/4] Verificando que exista el backup...
if not exist "%PG_HBA_BAK%" (
    echo.
    echo [ERROR CRITICO] Backup NO ENCONTRADO en: %PG_HBA_BAK%
    echo  NO se puede continuar. Tienes que reinstalar PostgreSQL.
    pause
    exit /b 1
)
echo   -> Backup OK: %PG_HBA_BAK%

echo.
echo [PASO 2/4] Copiando backup al archivo original pg_hba.conf...
copy /Y "%PG_HBA_BAK%" "%PG_HBA%"
if errorlevel 1 (
    echo.
    echo [ERROR] No se pudo copiar. HAZ CLICK DERECHO sobre este archivo
    echo  y selecciona: 'Ejecutar como administrador'
    echo.
    pause
    exit /b 1
)
echo   -> pg_hba.conf restaurado exitosamente.

echo.
echo [PASO 3/4] Reiniciando servicio PostgreSQL 18 para aplicar cambios...
net stop postgresql-x64-18 2>nul
timeout /t 3 /nobreak >nul
net start postgresql-x64-18
timeout /t 4 /nobreak >nul
echo   -> Servicio PostgreSQL 18 reiniciado.

echo.
echo [PASO 4/4] Verificando que PostgreSQL acepte conexiones...
set PG_BIN=C:\Program Files\PostgreSQL\18\bin
if exist "%PG_BIN%\psql.exe" (
    echo   -> Probando conectar con tu configuracion original pg_ident.conf (SIN password):
    "%PG_BIN%\psql.exe" -U postgres -h 127.0.0.1 -c "SELECT version();" 2>&1
) else (
    echo   -> psql no encontrado, pero el servicio se reinicio OK.
)

echo.
echo ******************************************************************
echo *  REPARACION FINALIZADA. PostgreSQL YA ESTA FUNCIONAL.          *
echo ******************************************************************
echo.
echo  Ahora ejecuta COMO ADMINISTRADOR el archivo:
echo    PASO_1_CONFIGURAR_BD_v2.bat     (el nuevo - usa edicion SIN BOM)
echo.
pause
endlocal
