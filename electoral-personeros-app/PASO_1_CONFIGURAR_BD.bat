@echo off
chcp 65001 >nul
title ================================================
title PASO 1: CONFIGURAR POSTGRESQL - SISTEMA PERSONEROS
title ================================================

echo.
echo ************************************************************
echo * PASO 1: CONFIGURACION DE BASE DE DATOS POSTGRESQL        *
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

if not exist "%PG_BIN%\psql.exe" (
    echo [ERROR] No se encontro PostgreSQL 18 en %PG_BIN%
    echo Ajusta la variable PG_BIN en este script.
    pause
    exit /b 1
)

echo [PASO 1/7] Haciendo backup de pg_hba.conf...
copy /Y "%PG_HBA%" "%PG_HBA_BAK%" >nul
echo   -> Backup guardado en: %PG_HBA_BAK%

echo.
echo [PASO 2/7] Cambiando temporalmente autenticacion a "trust" (sin password)...
powershell -Command "(Get-Content '%PG_HBA%') -replace 'scram-sha-256', 'trust' | Set-Content '%PG_HBA%' -Encoding UTF8"
if errorlevel 1 (
    echo [ERROR] No se pudo modificar pg_hba.conf. Ejecuta este .bat como ADMINISTRADOR.
    echo     (Click derecho sobre este archivo -^> Ejecutar como administrador)
    pause
    exit /b 1
)
echo   -> Listo.

echo.
echo [PASO 3/7] Reiniciando servicio PostgreSQL 18...
net stop postgresql-x64-18
timeout /t 2 /nobreak >nul
net start postgresql-x64-18
timeout /t 3 /nobreak >nul
echo   -> Servicio reiniciado.

echo.
echo [PASO 4/7] Estableciendo contrasena "Admin123!" al usuario "postgres"...
set PGPASSWORD=
"%PG_BIN%\psql.exe" -U postgres -h 127.0.0.1 -c "ALTER USER postgres WITH PASSWORD 'Admin123!';"
if errorlevel 1 (
    echo [ERROR] No se pudo cambiar la contrasena. Verifica que PostgreSQL este corriendo.
    pause
    exit /b 1
)
echo   -> Contrasena establecida correctamente.

echo.
echo [PASO 5/7] Creando base de datos "electoral_personeros" (si no existe)...
set PGPASSWORD=
"%PG_BIN%\psql.exe" -U postgres -h 127.0.0.1 -c "SELECT 1 FROM pg_database WHERE datname='electoral_personeros'" | findstr /C:"1 row" >nul
if errorlevel 1 (
    "%PG_BIN%\psql.exe" -U postgres -h 127.0.0.1 -c "CREATE DATABASE electoral_personeros;"
    echo   -> Base de datos electoral_personeros CREADA.
) else (
    echo   -> Base de datos electoral_personeros YA EXISTE (no se recreo).
)

echo.
echo [PASO 6/7] Ejecutando esquema (schema.sql) y datos iniciales (seeds.sql)...
set SCRIPT_DIR=%~dp0
set SCHEMA=%SCRIPT_DIR%backend\database\schema.sql
set SEEDS=%SCRIPT_DIR%backend\database\seeds.sql

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
powershell -Command "(Get-Content '%PG_HBA%') -replace '^\btrust\b', 'scram-sha-256' | Set-Content '%PG_HBA%' -Encoding UTF8"
net stop postgresql-x64-18
timeout /t 2 /nobreak >nul
net start postgresql-x64-18
timeout /t 3 /nobreak >nul
echo   -> Autenticacion restaurada. Servicio en linea.

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
echo   Usuario del sistema (para login app):
echo     Usuario:  admin
echo     Password: superadmin
echo     Rol:      SUPER_ADMIN
echo.
echo   Ahora ejecuta: PASO_2_INSTALAR_Y_EJECUTAR_BACKEND.bat
echo.
pause
endlocal
