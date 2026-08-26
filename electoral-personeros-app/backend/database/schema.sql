-- =============================================
-- SCHEMA: SISTEMA DE PERSONEROS ELECTORALES
-- =============================================

-- Tabla: persona
CREATE TABLE IF NOT EXISTS persona (
    id_persona SERIAL PRIMARY KEY,
    dni VARCHAR(8) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono1 VARCHAR(15) NOT NULL,
    telefono2 VARCHAR(15),
    distrito VARCHAR(100) NOT NULL,
    provincia VARCHAR(100) NOT NULL,
    direccion TEXT
);

-- Tabla: usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    idusuario SERIAL PRIMARY KEY,
    idpersona INT NOT NULL REFERENCES persona(id_persona) ON DELETE CASCADE,
    usuario VARCHAR(50) UNIQUE NOT NULL,
    clave VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL CHECK (rol IN ('SUPER_ADMIN', 'ADMINISTRADOR', 'PERSONERO_CENTRO', 'PERSONERO_MESA')),
    fecha_creado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado BOOLEAN DEFAULT TRUE
);

-- Tabla: centro_votacion
CREATE TABLE IF NOT EXISTS centro_votacion (
    id_centro SERIAL PRIMARY KEY,
    codigo_local VARCHAR(20) UNIQUE NOT NULL,
    ubigeo VARCHAR(10) NOT NULL,
    nombre_local VARCHAR(255) NOT NULL,
    direccion_local TEXT,
    departamento VARCHAR(100) NOT NULL,
    provincia VARCHAR(100) NOT NULL,
    distrito VARCHAR(100) NOT NULL
);

-- Tabla: mesa
CREATE TABLE IF NOT EXISTS mesa (
    id_mesa SERIAL PRIMARY KEY,
    num_mesa VARCHAR(10) UNIQUE NOT NULL,
    cod_local VARCHAR(20) REFERENCES centro_votacion(codigo_local),
    codi_ubigeo VARCHAR(10),
    nombre_local VARCHAR(255),
    direccion_local TEXT,
    departamento VARCHAR(100),
    provincia VARCHAR(100),
    distrito VARCHAR(100)
);

-- Tabla: personeros_users
CREATE TABLE IF NOT EXISTS personeros_users (
    id_solicitud SERIAL PRIMARY KEY,
    id_persona INT REFERENCES persona(id_persona),
    tipo_personero_solicitado VARCHAR(30) CHECK (tipo_personero_solicitado IN ('CENTRO', 'MESA')),
    id_centro_asignado INT REFERENCES centro_votacion(id_centro) NULL,
    id_mesa_asignada INT REFERENCES mesa(id_mesa) NULL,
    estado_vinculacion VARCHAR(20) DEFAULT 'SOLICITADO' CHECK (estado_vinculacion IN ('SOLICITADO', 'ASIGNADO', 'RECHAZADO')),
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- ÍNDICES EN COLUMNAS DE LLAVES FORÁNEAS
-- =============================================

CREATE INDEX IF NOT EXISTS idx_usuarios_idpersona ON usuarios(idpersona);
CREATE INDEX IF NOT EXISTS idx_mesa_cod_local ON mesa(cod_local);
CREATE INDEX IF NOT EXISTS idx_personeros_users_id_persona ON personeros_users(id_persona);
CREATE INDEX IF NOT EXISTS idx_personeros_users_id_centro_asignado ON personeros_users(id_centro_asignado);
CREATE INDEX IF NOT EXISTS idx_personeros_users_id_mesa_asignada ON personeros_users(id_mesa_asignada);

-- Índices adicionales para búsquedas frecuentes
CREATE INDEX IF NOT EXISTS idx_persona_dni ON persona(dni);
CREATE INDEX IF NOT EXISTS idx_usuarios_usuario ON usuarios(usuario);
CREATE INDEX IF NOT EXISTS idx_centro_votacion_codigo_local ON centro_votacion(codigo_local);
CREATE INDEX IF NOT EXISTS idx_centro_votacion_ubigeo ON centro_votacion(ubigeo);
CREATE INDEX IF NOT EXISTS idx_mesa_num_mesa ON mesa(num_mesa);
