-- =============================================
-- SEEDS: DATOS INICIALES
-- Región: Loreto, Perú
-- =============================================

-- =============================================
-- 1. PERSONA DEFAULT + USUARIO SUPER_ADMIN
-- =============================================

-- Insertar persona del administrador
INSERT INTO persona (dni, nombre, apellidos, telefono1, telefono2, distrito, provincia, direccion)
VALUES (
    '00000001',
    'Administrador',
    'Sistema',
    '900000001',
    NULL,
    'Iquitos',
    'Maynas',
    'Calle Principal S/N, Iquitos, Loreto'
)
ON CONFLICT (dni) DO NOTHING;

-- Insertar usuario SUPER_ADMIN (clave: superadmin - hash bcrypt valido generado con passlib/bcrypt 4.1.3)
INSERT INTO usuarios (idpersona, usuario, clave, rol, fecha_creado, estado)
VALUES (
    (SELECT id_persona FROM persona WHERE dni = '00000001'),
    'admin',
    '$2b$12$dYQdpkCnl0WXNnE1tVC1hOKZziqdORK0m/6m08R0Dypy/Wp0j/2a.',
    'SUPER_ADMIN',
    CURRENT_TIMESTAMP,
    TRUE
)
ON CONFLICT (usuario) DO NOTHING;

-- =============================================
-- 2. CENTROS DE VOTACIÓN (Región Loreto)
-- =============================================

-- Centro 1: I.E. N° 001 "San Juan" - Iquitos, Maynas
INSERT INTO centro_votacion (codigo_local, ubigeo, nombre_local, direccion_local, departamento, provincia, distrito)
VALUES (
    'LO-001-0001',
    '160101',
    'I.E. N° 001 "San Juan"',
    'Av. Putumayo 123, Iquitos',
    'Loreto',
    'Maynas',
    'Iquitos'
)
ON CONFLICT (codigo_local) DO NOTHING;

-- Centro 2: I.E. N° 002 "Santa Rosa" - Iquitos, Maynas
INSERT INTO centro_votacion (codigo_local, ubigeo, nombre_local, direccion_local, departamento, provincia, distrito)
VALUES (
    'LO-001-0002',
    '160101',
    'I.E. N° 002 "Santa Rosa"',
    'Jr. Napo 456, Iquitos',
    'Loreto',
    'Maynas',
    'Iquitos'
)
ON CONFLICT (codigo_local) DO NOTHING;

-- Centro 3: I.E. N° 003 "Mariscal Cáceres" - Nauta, Loreto
INSERT INTO centro_votacion (codigo_local, ubigeo, nombre_local, direccion_local, departamento, provincia, distrito)
VALUES (
    'LO-002-0001',
    '160201',
    'I.E. N° 003 "Mariscal Cáceres"',
    'Av. 28 de Julio 789, Nauta',
    'Loreto',
    'Loreto',
    'Nauta'
)
ON CONFLICT (codigo_local) DO NOTHING;

-- =============================================
-- 3. MESAS DE VOTACIÓN (6 mesas)
-- =============================================

-- Mesas para Centro 1: I.E. N° 001 "San Juan" (2 mesas)
INSERT INTO mesa (num_mesa, cod_local, codi_ubigeo, nombre_local, direccion_local, departamento, provincia, distrito)
VALUES (
    '001',
    'LO-001-0001',
    '160101',
    'I.E. N° 001 "San Juan"',
    'Av. Putumayo 123, Iquitos',
    'Loreto',
    'Maynas',
    'Iquitos'
)
ON CONFLICT (num_mesa) DO NOTHING;

INSERT INTO mesa (num_mesa, cod_local, codi_ubigeo, nombre_local, direccion_local, departamento, provincia, distrito)
VALUES (
    '002',
    'LO-001-0001',
    '160101',
    'I.E. N° 001 "San Juan"',
    'Av. Putumayo 123, Iquitos',
    'Loreto',
    'Maynas',
    'Iquitos'
)
ON CONFLICT (num_mesa) DO NOTHING;

-- Mesas para Centro 2: I.E. N° 002 "Santa Rosa" (2 mesas)
INSERT INTO mesa (num_mesa, cod_local, codi_ubigeo, nombre_local, direccion_local, departamento, provincia, distrito)
VALUES (
    '003',
    'LO-001-0002',
    '160101',
    'I.E. N° 002 "Santa Rosa"',
    'Jr. Napo 456, Iquitos',
    'Loreto',
    'Maynas',
    'Iquitos'
)
ON CONFLICT (num_mesa) DO NOTHING;

INSERT INTO mesa (num_mesa, cod_local, codi_ubigeo, nombre_local, direccion_local, departamento, provincia, distrito)
VALUES (
    '004',
    'LO-001-0002',
    '160101',
    'I.E. N° 002 "Santa Rosa"',
    'Jr. Napo 456, Iquitos',
    'Loreto',
    'Maynas',
    'Iquitos'
)
ON CONFLICT (num_mesa) DO NOTHING;

-- Mesas para Centro 3: I.E. N° 003 "Mariscal Cáceres" (2 mesas)
INSERT INTO mesa (num_mesa, cod_local, codi_ubigeo, nombre_local, direccion_local, departamento, provincia, distrito)
VALUES (
    '005',
    'LO-002-0001',
    '160201',
    'I.E. N° 003 "Mariscal Cáceres"',
    'Av. 28 de Julio 789, Nauta',
    'Loreto',
    'Loreto',
    'Nauta'
)
ON CONFLICT (num_mesa) DO NOTHING;

INSERT INTO mesa (num_mesa, cod_local, codi_ubigeo, nombre_local, direccion_local, departamento, provincia, distrito)
VALUES (
    '006',
    'LO-002-0001',
    '160201',
    'I.E. N° 003 "Mariscal Cáceres"',
    'Av. 28 de Julio 789, Nauta',
    'Loreto',
    'Loreto',
    'Nauta'
)
ON CONFLICT (num_mesa) DO NOTHING;
