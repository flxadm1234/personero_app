@echo off
chcp 65001 >nul
title ================================================
title RESTAURAR AUTENTICACION SEGURA POSTGRESQL
title ================================================

echo.
echo ******************************************************************
echo *  FINALIZAR CONFIGURACION - Restaurar scram-sha-256 (SEGURO)     *
echo *   PostgreSQL se dejo en modo TRUST (sin password) temporal.     *
echo *   Este script lo vuelve SEGURO (autenticacion scram-sha-256).   *
echo ******************************************************************
echo.

setlocal enabledelayedexpansion
set PG_DATA=C:\Program Files\PostgreSQL\18\data
set PG_HBA=%PG_DATA%\pg_hba.conf
set TMP_SRC=%~dp0tmp_pg_setup\pg_hba_trust.conf

if not exist "%PG_HBA%" (
    echo [ERROR] No se encuentra %PG_HBA%
    pause
    exit /b 1
)

echo [1/3] Cambiando todos los 'trust' por 'scram-sha-256' en pg_hba.conf  (SIN BOM)...
powershell -NoProfile -Command ^
  "$ErrorActionPreference='Stop';" ^
  "$enc = New-Object System.Text.UTF8Encoding $false;" ^
  "$hba = '%PG_HBA%';" ^
  "$lines = [System.IO.File]::ReadAllLines($hba);" ^
  "$changed = 0;" ^
  "$new = foreach ($l in $lines) { if ($l -match '\btrust\b') { $l -replace '\btrust\b', 'scram-sha-256'; $changed++ } else { $l } };" ^
  "[System.IO.File]::WriteAllLines($hba, $new, $enc);" ^
  "Write-Host '  -> Reemplazados '$changed' ocurrencias (UTF8 sin BOM OK).';"

if errorlevel 1 (
    echo [ERROR] No se pudo editar. EJECUTA ESTE ARCHIVO COMO ADMINISTRADOR.
    echo   (Click derecho -> Ejecutar como administrador)
    pause
    exit /b 1
)

echo.
echo [2/3] Reiniciando servicio PostgreSQL 18...
net stop postgresql-x64-18 2>nul
timeout /t 3 /nobreak >nul
net start postgresql-x64-18
timeout /t 4 /nobreak >nul
echo   -> Servicio reiniciado.

echo.
echo [3/3] Prueba CONEXION FINAL:
set PG_BIN=C:\Program Files\PostgreSQL\18\bin
set PATH=%PATH%;%PG_BIN%
set PGPASSWORD=Admin123!
echo   -> Conectando con usuario postgres y contrasena Admin123!...
"%PG_BIN%\psql.exe" -U postgres -h 127.0.0.1 -d electoral_personeros -c "SELECT count(*) AS total_tablas FROM information_schema.tables WHERE table_schema='public';"

if errorlevel 1 (
    echo.
    echo [ERROR] No se pudo conectar con Admin123!. Avisa a Felix.
    pause
    exit /b 1
)

echo.
echo ******************************************************************
echo *  POSTGRESQL AHORA ESTA CONFIGURADO Y SEGURO. 100% COMPLETO.    *
echo ******************************************************************
echo.
echo   Credenciales definitivas:
echo     Servidor:   127.0.0.1:5432
echo     Usuario:    postgres
echo     Contrasena: Admin123!
echo     Base datos: electoral_personeros
echo.
echo   Usuario del sistema (App / Swagger):
echo     Usuario:  admin
echo     Password: superadmin
echo     Rol:      SUPER_ADMIN
echo.
echo   Ahora ejecuta: PASO_2_INSTALAR_Y_EJECUTAR_BACKEND.bat
echo.
pause
endlocal
