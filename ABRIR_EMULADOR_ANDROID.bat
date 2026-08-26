@echo off
REM =============================================================
REM ABRIR EMULADOR ANDROID - Sistema Electoral de Personeros
REM Ejecutable de 1 CLIC. Arranca el AVD Medium_Phone_API_36.1
REM con variables SDK correctas, espera boot, y luego
REM instala + abre la app electoral-personeros automaticamente
REM =============================================================

setlocal
chcp 65001 >nul
title Sistema Electoral de Personeros - Emulador Android

echo.
echo ============================================================
echo   SISTEMA ELECTORAL DE PERSONEROS - ABRIR EMULADOR ANDROID
echo ============================================================
echo.

REM ----- Rutas -----
set ANDROID_SDK_ROOT=C:\Users\FRAMIREZ\AppData\Local\Android\Sdk
set ANDROID_HOME=%ANDROID_SDK_ROOT%
set EMULATOR=%ANDROID_SDK_ROOT%\emulator\emulator.exe
set ADB=%ANDROID_SDK_ROOT%\platform-tools\adb.exe
set AVD_NAME=Medium_Phone_API_36.1
set APK="%~dp0electoral-personeros-app\android\app\build\outputs\apk\debug\app-debug.apk"
set PKG=com.electoral.personeros
set ACT=.MainActivity

REM ----- Validar herramientas -----
if not exist "%EMULATOR%" (
    echo [ERROR] No se encontro emulator.exe en: %EMULATOR%
    echo         Verifica que Android SDK este instalado.
    pause
    exit /b 1
)
if not exist "%ADB%" (
    echo [ERROR] No se encontro adb.exe en: %ADB%
    pause
    exit /b 1
)

echo [1/6] Emulador:  %EMULATOR%
echo [2/6] ADB:       %ADB%
echo [3/6] AVD:       %AVD_NAME%
echo [4/6] APK path:  %APK%
echo.

REM ----- Kill emulador anterior si sigue vivo -----
"%ADB%" devices >nul 2>&1
for /f "tokens=1" %%d in ('"%ADB%" devices 2^>nul ^| findstr emulator') do (
    echo [!] Cerrando emulador anterior %%d ...
    "%ADB%" -s %%d emu kill >nul 2>&1
    timeout /t 3 /nobreak >nul
)

REM ----- Arrancar emulador (background) -----
echo [5/6] Iniciando emulador "%AVD_NAME%" ...
echo       (Puede tardar 1 a 2 minutos en arrancar completamente)
echo.

start "Android Emulator - %AVD_NAME%" /min "%EMULATOR%" ^
    -avd %AVD_NAME% ^
    -no-snapshot-load ^
    -gpu swiftshader_indirect

if errorlevel 1 (
    echo [ERROR] No se pudo iniciar el emulador.
    pause
    exit /b 1
)

REM ----- Esperar a que adb detecte el dispositivo -----
echo.
echo [6/6] Esperando boot del emulador ...
:wait_device
"%ADB%" wait-for-device >nul 2>&1
for /f "usebackq tokens=1,2" %%a in (`"%ADB%" devices 2^>nul`) do (
    if "%%a"=="emulator-5554" if "%%b"=="device" goto boot_check
)
timeout /t 3 /nobreak >nul
goto wait_device

:boot_check
echo       Dispositivo detectado. Esperando boot_completed ...
:wait_boot
for /f "usebackq" %%v in (`"%ADB%" -s emulator-5554 shell getprop sys.boot_completed 2^>nul`) do (
    if "%%v"=="1" goto boot_done
)
timeout /t 3 /nobreak >nul
goto wait_boot

:boot_done
echo.
echo ============================================================
echo   ✅ EMULADOR ARRANCADO CORRECTAMENTE - emulator-5554
echo ============================================================
echo.

REM ----- Instalar APK si existe -----
if exist %APK% (
    echo [i] Instalando APK actualizado: %APK% ...
    "%ADB%" -s emulator-5554 shell am force-stop %PKG% >nul 2>&1
    "%ADB%" -s emulator-5554 install -r %APK%
    if errorlevel 1 ( echo [WARN] Fallo reinstalacion - se usara APK previo ) else ( echo [OK] APK instalado correctamente )
) else (
    echo [WARN] APK no encontrado en %APK%. Compila primero con compilar.bat
)

REM ----- Abrir la app -----
echo.
echo [i] Abriendo Sistema Electoral de Personeros ...
"%ADB%" -s emulator-5554 shell am start -S -W ^
    -n "%PKG%/%ACT%" ^
    -a android.intent.action.MAIN ^
    -c android.intent.category.LAUNCHER >nul 2>&1

echo.
echo ============================================================
echo   ✅ APP LISTA PARA USAR
echo   Credencial de prueba:  admin  /  Admin123!   (SUPER_ADMIN)
echo ============================================================
echo.
timeout /t 5 /nobreak >nul
endlocal
exit /b 0
