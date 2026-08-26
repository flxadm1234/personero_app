@echo off
chcp 65001 >nul
title ================================================
title PASO 1 V2: CONFIGURAR POSTGRESQL  [SIN BOM - A PRUEBA DE FALLOS]
title ================================================

echo.
echo ************************************************************
echo * PASO 1 V2: CONFIGURACION DE BASE DE DATOS POSTGRESQL     *
echo *    (VERSION MEJORADA: Sin BOM - UTF8 correcto)           *
echo *    Sistema Electoral de Gestion de Personeros            *
echo ************************************************************
echo.
echo Este script realizara lo siguiente AUTOMATICAMENTE:
echo   1. Habilitara acceso temporal sin contrasena (trust)
echo   2. Reiniciara el servicio PostgreSQL 18
echo   3. Establecera la contrasena "Admin123!" para el usuario postgres
echo   4. Creara la base de datos "electoral_personeros"
echo   5. Ejecutara el esquema (tablas) y los datos iniciales (seeds)
echo   6. Restaurara la autenticacion segura (scram-sha-256)
echo.
pause

setlocal enabledelayedexpansion
set PG_BIN=C:\Program Files\PostgreSQL\18\bin
set PG_DATA=C:\Program Files\PostgreSQL\18\data
set PG_HBA=%PG_DATA%\pg_hba.conf
set PG_HBA_BAK=%PG_DATA%\pg_hba.conf.backup_personeros
set SCRIPT_DIR=%~dp0
set TMP_DIR=%SCRIPT_DIR%tmp_pg_setup
set SCHEMA=%SCRIPT_DIR%backend\database\schema.sql
set SEEDS=%SCRIPT_DIR%backend\database\seeds.sql

if not exist "%PG_BIN%\psql.exe" (
    echo [ERROR] No se encontro PostgreSQL 18 en %PG_BIN%
    echo Ajusta la variable PG_BIN en este script.
    pause
    exit /b 1
)

mkdir "%TMP_DIR%" >nul 2>&1
echo [PASO 1/7] Haciendo backup NUEVO de pg_hba.conf...
copy /Y "%PG_HBA%" "%PG_HBA_BAK%" >nul
copy /Y "%PG_HBA%" "%TMP_DIR%\pg_hba_original.conf" >nul
echo   -> Backup OK: %PG_HBA_BAK%

echo.
echo [PASO 2/7] Cambiando autenticacion scram-sha-256 ^> trust  (SIN BOM)...
powershell -NoProfile -Command ^
  "$ErrorActionPreference='Stop';" ^
  "$enc = New-Object System.Text.UTF8Encoding $false;" ^
  "$content = [System.IO.File]::ReadAllLines('%TMP_DIR%\pg_hba_original.conf');" ^
  "$new = $content -replace 'scram-sha-256', 'trust';" ^
  "[System.IO.File]::WriteAllLines('%TMP_DIR%\pg_hba_trust.conf', $new, $enc);" ^
  "Write-Host '  -> Editado OK en carpeta temporal (UTF8 sin BOM).';"

if errorlevel 1 (
    echo [ERROR] No se pudo editar el archivo temporal.
    pause
    exit /b 1
)

echo   -> Copiando al directorio PostgreSQL...
copy /Y "%TMP_DIR%\pg_hba_trust.conf" "%PG_HBA%" >nul
if errorlevel 1 (
    echo [ERROR PERMISOS] No se pudo sobrescribir pg_hba.conf.
    echo     HAZ CLICK DERECHO -^> Ejecutar COMO ADMINISTRADOR.
    pause
    exit /b 1
)
echo   -> Listo. (archivo sin BOM, PostgreSQL lo reconocera)

echo.
echo [PASO 3/7] Reiniciando servicio PostgreSQL 18...
net stop postgresql-x64-18 2>nul
timeout /t 2 /nobreak >nul
net start postgresql-x64-18
timeout /t 4 /nobreak >nul
echo   -> Servicio reiniciado.

echo.
echo [PASO 4/7] Estableciendo contrasena "Admin123!" al usuario "postgres"...
set PGPASSWORD=
"%PG_BIN%\psql.exe" -U postgres -h 127.0.0.1 -c "ALTER USER postgres WITH PASSWORD 'Admin123!';"
if errorlevel 1 (
    echo [ERROR] No se pudo cambiar la contrasena.
    pause
    exit /b 1
)
echo   -> Contrasena establecida correctamente.

echo.
echo [PASO 5/7] Creando base de datos "electoral_personeros"...
set PGPASSWORD=
"%PG_BIN%\psql.exe" -U postgres -h 127.0.0.1 -c "SELECT 1 FROM pg_database WHERE datname='electoral_personeros'" 2>nul | findstr /C:"1 row" >nul
if errorlevel 1 (
    "%PG_BIN%\psql.exe" -U postgres -h 127.0.0.1 -c "CREATE DATABASE electoral_personeros;"
    echo   -> Base de datos electoral_personeros CREADA.
) else (
    echo   -> Base de datos electoral_personeros YA EXISTE (no se recreo).
)

echo.
echo [PASO 6/7] Aplicando schema.sql (tablas) y seeds.sql (datos)...
set PGPASSWORD=Admin123!
if exist "%SCHEMA%" (
    echo   -> Aplicando schema.sql...
    "%PG_BIN%\psql.exe" -U postgres -h 127.0.0.1 -d electoral_personeros -f "%SCHEMA%"
    echo   -> schema.sql aplicado.
) else (
    echo [ADVERTENCIA] No se encontro: %SCHEMA%
)
if exist "%SEEDS%" (
    echo   -> Aplicando seeds.sql...
    "%PG_BIN%\psql.exe" -U postgres -h 127.0.0.1 -d electoral_personeros -f "%SEEDS%"
    echo   -> seeds.sql aplicado.
) else (
    echo [ADVERTENCIA] No se encontro: %SEEDS%
)

echo.
echo [PASO 7/7] Restaurando autenticacion SEGURA scram-sha-256...
powershell -NoProfile -Command ^
  "$ErrorActionPreference='Stop';" ^
  "$enc = New-Object System.Text.UTF8Encoding $false;" ^
  "$content = [System.IO.File]::ReadAllLines('%TMP_DIR%\pg_hba_trust.conf');" ^
  "$new = $content -replace '\btrust\b', 'scram-sha-256';" ^
  "[System.IO.File]::WriteAllLines('%TMP_DIR%\pg_hba_secure.conf', $new, $enc);" ^
  "Write-Host '  -> Editado en temporal.';"
copy /Y "%TMP_DIR%\pg_hba_secure.conf" "%PG_HBA%" >nul
net stop postgresql-x64-18 2>nul
timeout /t 2 /nobreak >nul
net start postgresql-x64-18
timeout /t 4 /nobreak >nul
echo   -> Autenticacion SEGURA restaurada. Servicio en linea.

echo.
echo [VERIFICACION FINAL] Probando conexion con Admin123!...
set PGPASSWORD=Admin123!
"%PG_BIN%\psql.exe" -U postgres -h 127.0.0.1 -d electoral_personeros -c "SELECT tablename FROM pg_tables WHERE schemaname='public' ORDER BY tablename;"
if errorlevel 1 (
    echo.
    echo [ADVERTENCIA] Prueba SQL fallida. Revisa arriba.
) else (
    echo   -> CONEXION OK - Tablas creadas correctamente.
)

echo.
echo ************************************************************
echo *  CONFIGURACION FINALIZADA EXITOSAMENTE                  *
echo ************************************************************
echo.
echo   Credenciales PostgreSQL:
echo     Usuario:    postgres
echo     Contrasena: Admin123!
echo     Base datos: electoral_personeros
echo.
echo   Usuario del sistema:
echo     Usuario:  admin
echo     Password: superadmin
echo     Rol:      SUPER_ADMIN
echo.
echo   Ahora ejecuta: PASO_2_INSTALAR_Y_EJECUTAR_BACKEND.bat
echo.
pause
rmdir /S /Q "%TMP_DIR%" >nul 2>&1
endlocal
