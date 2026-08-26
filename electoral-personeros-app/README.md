# 🗳️ Sistema Electoral de Gestión y Monitoreo de Personeros

> Solución completa para Registro, Monitoreo y Asignación de Personeros Electorales.
> **Región Loreto, Perú — Accesible para adultos mayores (Senior-Friendly).**

---

## 📋 Arquitectura del Proyecto

```
electoral-personeros-app/
├── backend/                     # 🐍 API REST — Python + FastAPI
│   ├── src/
│   │   ├── config/              # Settings + Database SQLAlchemy
│   │   ├── controllers/         # (estructura para expansion)
│   │   ├── middlewares/         # Auth JWT + RoleChecker
│   │   ├── models/              # Tablas SQLAlchemy
│   │   ├── routes/              # Routers por rol (auth, public, superadmin, admin, personero_centro)
│   │   ├── services/            # (estructura para logica de negocio)
│   │   └── schemas.py           # Pydantic Request/Response
│   ├── database/
│   │   ├── schema.sql           # DDL PostgreSQL + Indices
│   │   └── seeds.sql            # Datos iniciales (admin + 3 centros + 6 mesas)
│   ├── main.py                  # Entrypoint FastAPI
│   ├── requirements.txt
│   ├── .env.example
│   ├── .env                     # Ya configurado para pruebas locales
│   └── Dockerfile
│
├── android/                     # 🤖 App Móvil — Kotlin + Jetpack Compose + MVVM + Retrofit
│   ├── app/src/main/java/com/electoral/personeros/
│   │   ├── data/
│   │   │   ├── api/             # RetrofitInstance + ApiService (31 endpoints)
│   │   │   ├── models/          # Data classes alineados 100% con FastAPI
│   │   │   └── repository/      # 7 repositorios (Auth, Usuario, Persona, Centro, Mesa, Solicitud, Cobertura)
│   │   ├── ui/
│   │   │   ├── theme/           # Tema Clean Blue (#0288D1 / #00A3E0 / #F4F6F9)
│   │   │   ├── screens/         # Login, AdminDashboard, SuperAdmin, Solicitudes, PersoneroCentro + Modales
│   │   │   └── viewmodels/      # 4 ViewModels
│   │   ├── utils/               # Constants (provincias Loreto), JWTUtils, FileUtils
│   │   └── MainActivity.kt
│   └── build.gradle.kts
│
├── PASO_1_CONFIGURAR_BD.bat     # ⬇️ Doble clic — Configura PostgreSQL local
├── PASO_2_INSTALAR_Y_EJECUTAR_BACKEND.bat
├── docker-compose.yml           # 🐳 Levanta BD + Backend en Docker (para VPS)
└── README.md                    # 📖 Este documento
```

---

## 🚀 Inicio Rápido — Pruebas Locales (Windows)

### 🔑 Credenciales por Defecto (todos los entornos)
| Entorno         | Usuario    | Contraseña  | Rol            |
|-----------------|------------|-------------|----------------|
| App / Backend   | `admin`    | `superadmin`| `SUPER_ADMIN`  |
| PostgreSQL      | `postgres` | `Admin123!` | BD: `electoral_personeros` |

---

### 📦 Paso 1: Configurar Base de Datos (PostgreSQL 18 local)

1. **Abre explorador de archivos** en la carpeta del proyecto
2. **Click derecho** sobre [PASO_1_CONFIGURAR_BD.bat](file:///f:/SISTEMA%20DE%20PERSONERO/electoral-personeros-app/PASO_1_CONFIGURAR_BD.bat)
3. **Ejecutar como Administrador**
4. Sigue las instrucciones en pantalla. El script hará automáticamente:
   - ✅ Backup de `pg_hba.conf`
   - ✅ Reseteará la contraseña `postgres` a `Admin123!`
   - ✅ Creará BD `electoral_personeros`
   - ✅ Aplicará `schema.sql` + `seeds.sql`
   - ✅ Restaurará autenticación segura

Al finalizar aparecerá:
```
CONFIGURACION FINALIZADA EXITOSAMENTE
```

---

### ⚡ Paso 2: Levantar el Backend FastAPI

1. **Doble clic** sobre [PASO_2_INSTALAR_Y_EJECUTAR_BACKEND.bat](file:///f:/SISTEMA%20DE%20PERSONERO/electoral-personeros-app/PASO_2_INSTALAR_Y_EJECUTAR_BACKEND.bat)
2. Espera a que instale todas las dependencias (fastapi, uvicorn, sqlalchemy, bcrypt, etc.)
3. Se abrirá **automáticamente tu navegador** en la documentación Swagger:
   - 🔗 **Swagger UI:** http://localhost:8000/docs — (prueba endpoints aquí!)
   - 🔗 **Health check:** http://localhost:8000/api/health

#### 🧪 Prueba manual rápida con Swagger:
1. Entra a `/docs`
2. Expande `POST /api/auth/login` → **Try it out**
3. Username: `admin` · Password: `superadmin` → **Execute**
4. Copia el `access_token` devuelto
5. Haz clic en 🔒 **Authorize** arriba a la derecha → pega el token (sin comillas)
6. Ahora prueba: `GET /api/superadmin/usuarios` — debería retornar la lista con tu usuario `admin`.

---

### 🐳 Alternativa — Docker (Todo en uno) — Para tu VPS Linux / Windows con Docker

```bash
# Dentro de electoral-personeros-app/
docker-compose up -d --build

# Espera ~30s (la BD se inicializa con schema + seeds)
# Luego accede:
#   - API:     http://localhost:8000
#   - Swagger: http://localhost:8000/docs
#   - PostgreSQL: localhost:5432  (user postgres, pwd Admin123!)

# Detener:
docker-compose down
# (los datos de BD se preservan en volumen pg_personeros_data)
```

---

### 📱 Paso 3: App Android (Kotlin)

1. Abre **Android Studio Hedgehog o superior**
2. **File → Open →** selecciona la carpeta `electoral-personeros-app/android/`
3. Espera a que **Gradle Sync** termine (descarga Compose BOM, Retrofit, OkHttp, etc.)
4. **Configura Emulador o Dispositivo:**
   - **Importante para emulador:** El Base URL es `http://10.0.2.2:8000` (bridge al host).
   - **Dispositivo físico:** Edita [Constants.kt](file:///f:/SISTEMA%20DE%20PERSONERO/electoral-personeros-app/android/app/src/main/java/com/electoral/personeros/utils/Constants.kt) y cambia `BASE_URL` por la IP de tu PC en la red local (ej: `http://192.168.1.10:8000`). El backend debe correr con `--host 0.0.0.0` (lo hace por defecto el script del PASO 2).
5. Click en ▶️ **Run 'app'**

## 🧩 Endpoints Principales del Backend

| Grupo               | Método  | Ruta                                            | Descripción                                      |
|---------------------|---------|-------------------------------------------------|--------------------------------------------------|
| 🔓 Auth             | POST    | `/api/auth/login`                               | Login con OAuth2 Form → retorna JWT              |
| 🔓 Auth             | POST    | `/api/auth/change-password`                     | Cambiar contraseña propio                        |
| 🔓 Auth             | PUT     | `/api/auth/profile`                             | Actualizar datos personales                      |
| 🌐 Público          | POST    | `/api/public/solicitud-personero`               | Postulación voluntario (sin token)               |
| 👑 Super Admin      | CRUD    | `/api/superadmin/usuarios`                      | Gestión completa de usuarios                     |
| 👑 Super Admin      | PATCH   | `/api/superadmin/usuarios/:id/estado`           | Habilitar / Inhabilitar                          |
| 👑 Super Admin      | POST    | `/api/superadmin/usuarios/:id/reset-password`   | Resetear clave                                   |
| 🛡️ Admin           | POST    | `/api/admin/personeros-centro`                  | Crear Personero de Centro                        |
| 🛡️ Admin           | GET     | `/api/admin/personeros-centro`                  | Listar Personeros de Centro                      |
| 🛡️ Admin           | GET     | `/api/admin/centros-sin-personero`              | Centros sin asignación                           |
| 🛡️ Admin           | GET     | `/api/admin/monitoreo/cobertura`                | Totales + % de cobertura global                  |
| 🛡️ Admin           | GET     | `/api/admin/dashboard/desglose`                 | Drill-down por Provincia → Distrito              |
| 🛡️ Admin           | GET     | `/api/admin/solicitudes-pendientes`             | Postulaciones públicas pendientes                |
| 🛡️ Admin           | GET     | `/api/admin/reportes/excel`                     | Descarga reporte .xlsx consolidado               |
| 🎫 PersoneroCentro | GET     | `/api/personero-centro/solicitudes`             | Sus asignaciones                                 |
| 🎫 PersoneroCentro | GET     | `/api/personero-centro/personeros-disponibles`  | Personeros de mesa disponibles (llamar/WhatsApp) |
| 🎫 PersoneroCentro | GET     | `/api/personero-centro/mesas-centro/:id_centro` | Mesas LIBRE / OCUPADA del centro                 |
| 🎫 PersoneroCentro | POST    | `/api/personero-centro/asignar-mesa`            | Asignar personero a mesa libre                   |

---

## 🎨 Paleta de Colores — Tema Clean Blue

| Elemento          | Color Hex | Uso                                  |
|-------------------|-----------|--------------------------------------|
| Primary           | `#0288D1` | Botones principales, AppBar, Títulos |
| Accent / Secondary| `#00A3E0` | Acentos, links, badges               |
| Background        | `#F4F6F9` | Fondo general de pantallas           |
| Surface / Card    | `#FFFFFF` | Tarjetas con 6dp de esquina redonda  |
| Asignado ✓        | Verde     | Badge / Estado                       |
| Sin Asignar ✗     | Rojo      | Badge / Estado                       |

---

## 🗃️ Modelo de Base de Datos (5 Tablas)

1. **`persona`** — Datos personales (DNI único)
2. **`usuarios`** — Credenciales de acceso (rol: `SUPER_ADMIN | ADMINISTRADOR | PERSONERO_CENTRO | PERSONERO_MESA`)
3. **`centro_votacion`** — Locales de votación (codigo_local único, ubigeo, provincia, distrito)
4. **`mesa`** — Mesas por centro (num_mesa único, FK a centro.codigo_local)
5. **`personeros_users`** — Solicitudes y asignaciones (`CENTRO|`MESA`, `SOLICITADO|ASIGNADO|RECHAZADO`)

Todos los scripts e índices están en [schema.sql](file:///f:/SISTEMA%20DE%20PERSONERO/electoral-personeros-app/backend/database/schema.sql) + [seeds.sql](file:///f:/SISTEMA%20DE%20PERSONERO/electoral-personeros-app/backend/database/seeds.sql).

---

## 📋 Despliegue en VPS (Linux Ubuntu 22.04)

```bash
# 1. Instalar Docker + Docker Compose Plugin
sudo apt update && sudo apt install -y docker.io docker-compose-plugin git
sudo usermod -aG docker $USER && newgrp docker

# 2. Subir proyecto / clonar desde GitHub
git clone <tu-repo-url> && cd electoral-personeros-app

# 3. Levantar todo
docker compose up -d --build

# 4. Verificar
docker compose ps   # los 2 contenedores deben estar healthy / Up
curl http://localhost:8000/api/health   # -> {"estado":"ok"}

# 5. (Opcional) Nginx como reverse proxy con Let's Encrypt
# Instala certbot + nginx y configura proxy_pass a localhost:8000
```

---

## 🔐 Seguridad y Buenas Prácticas

- ✅ **Contraseñas:** Hash `bcrypt` (passlib + bcrypt 4.1.3) — NUNCA se guardan en plano
- ✅ **Autenticación:** JWT HS256 (24h expiración configurable via `.env`)
- ✅ **Roles:** Middleware `RoleChecker` por endpoint — solo los autorizados acceden
- ✅ **CORS:** Habilitado para desarrollo; antes de producción reemplaza `allow_origins=["*"]` por tus dominios
- ✅ **PostgreSQL:** Producción usa scram-sha-256 (scripts reestablecen auth segura siempre)
- ⚠️ **Antes de producción:** Cambia `SECRET_KEY` por una cadena aleatoria larga y única!

---

## ✅ Roadmap y Mejoras Futuras

1. **Sprint 2:** Probar app Android real + conectividad con backend local
2. **Sprint 3:** Exportar reportes PDF
3. **Sprint 4:** Notificaciones push FCM cuando se asignan mesas
4. **Sprint 5:** Panel web (React) para administradores de escritorio

---

## 👤 Atribución

Sistema diseñado y desarrollado para la Región **Loreto, Perú** — Enfoque Senior-Friendly con botones táctiles grandes (≥56dp), fuentes ≥16sp e indicadores de color explícitos.

---

_— Firma simple:_ **Felix Ramirez**
