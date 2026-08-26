@echo off
chcp 65001 >nul
title PASO 2: INSTALAR DEPENDENCIAS Y LEVANTAR BACKEND FASTAPI

echo.
echo ************************************************************
echo * PASO 2: INSTALAR DEPENDENCIAS PYTHON Y EJECUTAR BACKEND *
echo *          Sistema Electoral de Gestion de Personeros      *
echo ************************************************************
echo.
echo Este script:
echo   1. Instalara las dependencias Python del archivo requirements.txt
echo   2. Verificara la conexion a PostgreSQL
echo   3. Levantara el servidor FastAPI en http://localhost:8000
echo   4. Abrira automaticamente la documentacion Swagger
echo.
pause

setlocal
cd /d "%~dp0backend"

where python >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Python no esta instalado o no esta en PATH.
    echo Descargalo de https://www.python.org/downloads/ y marca "Add to PATH"
    pause
    exit /b 1
)

echo.
echo [1/3] Instalando dependencias Python (fastapi, uvicorn, sqlalchemy, psycopg2, bcrypt, JWT, openpyxl)...
python -m pip install --upgrade pip
python -m pip install -r requirements.txt
if errorlevel 1 (
    echo [ERROR] Fallo la instalacion de dependencias.
    pause
    exit /b 1
)
echo   -> Dependencias instaladas correctamente.

echo.
echo [2/3] Verificando conexion a PostgreSQL (electoral_personeros)...
python -c "import os, sys; sys.path.insert(0, '.'); from src.config.database import engine; from sqlalchemy import text; conn=engine.connect(); r=conn.execute(text('SELECT count(*) FROM information_schema.tables WHERE table_schema=%s', ('public',))); print(f'   -> Conexion OK. Tablas en public: {r.scalar()}'); conn.close()"
if errorlevel 1 (
    echo [ADVERTENCIA] No se pudo conectar a PostgreSQL.
    echo   - Asegurate de haber ejecutado PASO_1_CONFIGURAR_BD.bat
    echo   - Verifica el usuario/contrasena en backend\.env
    echo.
    choice /C SN /M "Deseas continuar de todos modos para ver el error especifico"
    if errorlevel 2 goto :fin
)

echo.
echo [3/3] Levantando servidor FastAPI...
echo   - API URL:   http://localhost:8000
echo   - Swagger:   http://localhost:8000/docs
echo   - Health:    http://localhost:8000/api/health
echo.
echo   Presiona CTRL+C para detener el servidor.
echo.

start "" "http://localhost:8000/docs"
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload

:fin
endlocal
pause
