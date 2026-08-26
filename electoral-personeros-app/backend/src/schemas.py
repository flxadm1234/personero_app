from typing import Optional, Literal, List
from datetime import datetime
from pydantic import BaseModel, Field


# ========== AUTH ==========
class LoginRequest(BaseModel):
    username: str
    password: str


class LoginResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: "UserResponseData"


class UserResponseData(BaseModel):
    idusuario: int
    usuario: str
    rol: str
    idpersona: int
    nombre: str
    apellidos: str
    dni: str
    telefono1: Optional[str] = None
    provincia: Optional[str] = None
    distrito: Optional[str] = None
    correo: Optional[str] = None
    id_centro_asignado: Optional[int] = None
    id_mesa_asignada: Optional[int] = None
    nombre_centro_asignado: Optional[str] = None
    distritos_asignados: List[str] = []


class ChangePasswordRequest(BaseModel):
    old_password: str
    new_password: str


class UpdateProfileRequest(BaseModel):
    nombre: Optional[str] = None
    apellidos: Optional[str] = None
    telefono1: Optional[str] = None
    telefono2: Optional[str] = None
    direccion: Optional[str] = None
    distrito: Optional[str] = None
    provincia: Optional[str] = None
    correo: Optional[str] = None


# ========== PERSONA ==========
class PersonaBase(BaseModel):
    dni: str = Field(..., max_length=8)
    nombre: str = Field(..., max_length=100)
    apellidos: str = Field(..., max_length=100)
    telefono1: str = Field(..., max_length=15)
    telefono2: Optional[str] = Field(None, max_length=15)
    correo: Optional[str] = Field(None, max_length=150)
    distrito: str = Field(..., max_length=100)
    provincia: str = Field(..., max_length=100)
    direccion: Optional[str] = None


class PersonaResponse(PersonaBase):
    id_persona: int

    class Config:
        from_attributes = True


class PersonaDNILookupResponse(BaseModel):
    encontrado: bool
    persona: Optional[PersonaResponse] = None
    id_solicitud_activa: Optional[int] = None
    id_solicitud_tipo: Optional[str] = None
    sugerencia_nombre: Optional[str] = None
    sugerencia_apellidos: Optional[str] = None


# ========== USUARIOS ==========
ROL_CHOICES = Literal["SUPER_ADMIN", "ADMINISTRADOR", "PERSONERO_CENTRO", "PERSONERO_MESA"]
TIPO_PERSONERO = Literal["CENTRO", "MESA"]
ESTADO_VINCULACION = Literal["SOLICITADO", "ASIGNADO", "RECHAZADO"]


class UsuarioCreateRequest(BaseModel):
    dni: str = Field(..., max_length=8)
    nombre: str = Field(..., max_length=100)
    apellidos: str = Field(..., max_length=100)
    telefono1: str = Field(..., max_length=15)
    telefono2: Optional[str] = Field(None, max_length=15)
    distrito: str = Field(..., max_length=100)
    provincia: str = Field(..., max_length=100)
    direccion: Optional[str] = None
    usuario: str = Field(..., max_length=50)
    clave: str = Field(..., max_length=255)
    rol: ROL_CHOICES


class UsuarioUpdateRequest(BaseModel):
    dni: Optional[str] = Field(None, max_length=8)
    nombre: Optional[str] = Field(None, max_length=100)
    apellidos: Optional[str] = Field(None, max_length=100)
    telefono1: Optional[str] = Field(None, max_length=15)
    telefono2: Optional[str] = Field(None, max_length=15)
    distrito: Optional[str] = Field(None, max_length=100)
    provincia: Optional[str] = Field(None, max_length=100)
    direccion: Optional[str] = None
    usuario: Optional[str] = Field(None, max_length=50)
    clave: Optional[str] = Field(None, max_length=255)
    rol: Optional[ROL_CHOICES] = None


class UsuarioEstadoRequest(BaseModel):
    estado: bool


class UsuarioResetPasswordRequest(BaseModel):
    new_clave: str = Field(..., max_length=255)


class UsuarioWithPersonaResponse(BaseModel):
    idusuario: int
    usuario: str
    rol: str
    fecha_creado: datetime
    estado: bool
    idpersona: int
    persona: PersonaResponse


# ========== PUBLIC - SOLICITUD (REGISTRO PERSONERO) ==========
class SolicitudPersoneroRequest(BaseModel):
    dni: str = Field(..., max_length=8)
    nombre: str = Field(..., max_length=100)
    apellidos: str = Field(..., max_length=100)
    telefono1: str = Field(..., max_length=15)
    telefono2: Optional[str] = Field(None, max_length=15)
    correo: Optional[str] = Field(None, max_length=150)
    distrito: str = Field(..., max_length=100)
    provincia: str = Field(..., max_length=100)
    direccion: Optional[str] = None
    tipo_personero_solicitado: TIPO_PERSONERO


# ========== REGISTRAR Y ASIGNAR MESA (DIRECTO) ==========
class RegistrarYAsignarMesaRequest(BaseModel):
    dni: str = Field(..., max_length=8)
    nombre: str = Field(..., max_length=100)
    apellidos: str = Field(..., max_length=100)
    telefono1: str = Field(..., max_length=15)
    telefono2: Optional[str] = Field(None, max_length=15)
    correo: Optional[str] = Field(None, max_length=150)
    distrito: str = Field(..., max_length=100)
    provincia: str = Field(..., max_length=100)
    direccion: Optional[str] = None
    id_centro_asignado: Optional[int] = None
    id_mesa_asignada: int = Field(..., gt=0)


class DesasignarMesaRequest(BaseModel):
    id_mesa: int


# ========== CENTRO VOTACION ==========
class CentroVotacionResponse(BaseModel):
    id_centro: int
    codigo_local: str
    ubigeo: str
    nombre_local: str
    direccion_local: Optional[str] = None
    departamento: str
    provincia: str
    distrito: str

    class Config:
        from_attributes = True


# ========== MESA ==========
class MesaResponse(BaseModel):
    id_mesa: int
    num_mesa: str
    cod_local: Optional[str] = None
    codi_ubigeo: Optional[str] = None
    nombre_local: Optional[str] = None
    direccion_local: Optional[str] = None
    departamento: Optional[str] = None
    provincia: Optional[str] = None
    distrito: Optional[str] = None

    class Config:
        from_attributes = True


class MesaCentroResponse(BaseModel):
    id_mesa: int
    num_mesa: str
    cod_local: str = ""
    codi_ubigeo: str = ""
    nombre_local: str = ""
    direccion_local: str = ""
    departamento: str = "LORETO"
    provincia: str = ""
    distrito: str = ""
    estado: Literal["LIBRE", "OCUPADA"]
    personero: Optional[dict] = None


# ========== PERSONEROS USERS (SOLICITUDES) ==========
class PersonerosUsersResponse(BaseModel):
    id_solicitud: int
    id_persona: Optional[int] = None
    tipo_personero_solicitado: Optional[str] = None
    id_centro_asignado: Optional[int] = None
    id_mesa_asignada: Optional[int] = None
    estado_vinculacion: str
    fecha_registro: datetime

    class Config:
        from_attributes = True


class SolicitudPendienteResponse(BaseModel):
    id_solicitud: int
    tipo_personero_solicitado: str
    estado_vinculacion: str
    fecha_registro: datetime
    id_persona: int
    persona: PersonaResponse


# ========== ADMIN - PERSONERO CENTRO ==========
class PersoneroCentroCreateRequest(BaseModel):
    dni: str = Field(..., max_length=8)
    nombre: str = Field(..., max_length=100)
    apellidos: str = Field(..., max_length=100)
    telefono1: str = Field(..., max_length=15)
    telefono2: Optional[str] = Field(None, max_length=15)
    distrito: str = Field(..., max_length=100)
    provincia: str = Field(..., max_length=100)
    direccion: Optional[str] = None
    usuario: str = Field(..., max_length=50)
    clave: str = Field(..., max_length=255)


class PersoneroCentroResponse(BaseModel):
    idusuario: int
    usuario: str
    rol: str
    estado: bool
    idpersona: int
    persona: PersonaResponse


# ========== MONITOREO / COBERTURA ==========
class CoberturaResponse(BaseModel):
    total_centros: int
    centros_asignados: int
    centros_sin_asignar: int
    porcentaje_centros: float
    total_mesas: int
    mesas_asignadas: int
    mesas_sin_asignar: int
    porcentaje_mesas: float
    total_personeros_centro: int
    total_personeros_mesa: int


class DesgloseItem(BaseModel):
    provincia: str
    distrito: str
    total_centros: int
    centros_asignados: int
    total_mesas: int
    mesas_asignadas: int


# ========== PERSONERO DISPONIBLE ==========
class PersoneroDisponibleResponse(BaseModel):
    nombre: str
    apellidos: str
    dni: str
    telefono1: str
    id_solicitud: int
    idusuario: Optional[int] = None
    id_usuario: Optional[int] = None
    idUsuario: Optional[int] = None
    id_persona: Optional[int] = None
    tipo_personero: Optional[str] = None


# ========== ASIGNAR MESA ==========
class AsignarMesaRequest(BaseModel):
    id_solicitud_personero_mesa: int
    id_mesa: int


# ========== PERSONERO CENTRO SOLICITUDES ==========
class SolicitudPersoneroCentroResponse(BaseModel):
    id_solicitud: int
    tipo_personero_solicitado: str
    estado_vinculacion: str
    fecha_registro: datetime
    id_persona: int
    persona: PersonaResponse
    centro_asignado: Optional[CentroVotacionResponse] = None
    mesa_asignada: Optional[MesaResponse] = None


# ========== USUARIO PERFIL COMPLETO (POST: va al final para que existan PersonaResponse, CentroVotacionResponse, MesaResponse ==========
class DistritoAsignadoItem(BaseModel):
    distrito: str
    provincia: Optional[str] = None


class PerfilUsuarioCompletoResponse(BaseModel):
    idusuario: int
    usuario: str
    rol: str
    fecha_creado: datetime
    estado: bool
    idpersona: int
    persona: PersonaResponse
    id_centro_asignado: Optional[int] = None
    id_mesa_asignada: Optional[int] = None
    centro_asignado: Optional[CentroVotacionResponse] = None
    mesa_asignada: Optional[MesaResponse] = None
    distritos_asignados: List[DistritoAsignadoItem] = []


class AdminDistritosUpdateRequest(BaseModel):
    distritos: List[str] = Field(..., min_length=0, max_length=200)
