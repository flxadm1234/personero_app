from datetime import timedelta
from typing import Optional
from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from sqlalchemy.orm import Session

from src.config.database import get_db
from src.config.settings import settings
from src.middlewares.auth import (
    authenticate_user,
    create_access_token,
    get_current_user_with_persona,
    verify_password,
    get_password_hash
)
from src.models.usuario import Usuarios
from src.models.persona import Persona
from src.models.usuarios_distritos import UsuariosDistritos
from src.models.centro_votacion import CentroVotacion
from src.models.mesa import Mesa
from src.schemas import (
    LoginResponse,
    UserResponseData,
    ChangePasswordRequest,
    UpdateProfileRequest,
    PerfilUsuarioCompletoResponse,
    DistritoAsignadoItem,
)

router = APIRouter(prefix="/api/auth", tags=["Autenticación"])


@router.post("/login", response_model=LoginResponse)
def login(
    form_data: OAuth2PasswordRequestForm = Depends(),
    db: Session = Depends(get_db)
):
    user = authenticate_user(db, form_data.username, form_data.password)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Usuario o contraseña incorrectos",
            headers={"WWW-Authenticate": "Bearer"},
        )

    persona = db.query(Persona).filter(Persona.id_persona == user.idpersona).first()

    distritos_usuarios = db.query(UsuariosDistritos).filter(UsuariosDistritos.idusuario == user.idusuario).all()
    distritos_asignados = [ud.distrito for ud in distritos_usuarios]

    nombre_centro_asignado = None
    id_centro_asignado = getattr(user, "id_centro_asignado", None)
    if id_centro_asignado:
        centro = db.query(CentroVotacion).filter(CentroVotacion.id_centro == id_centro_asignado).first()
        if centro:
            nombre_centro_asignado = centro.nombre_local

    id_mesa_asignada = getattr(user, "id_mesa_asignada", None)

    access_token_expires = timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    access_token = create_access_token(
        data={
            "sub": user.usuario,
            "idusuario": user.idusuario,
            "rol": user.rol,
            "usuario": user.usuario
        },
        expires_delta=access_token_expires
    )

    user_data = UserResponseData(
        idusuario=user.idusuario,
        usuario=user.usuario,
        rol=user.rol,
        idpersona=user.idpersona,
        nombre=persona.nombre if persona else "",
        apellidos=persona.apellidos if persona else "",
        dni=persona.dni if persona else "",
        telefono1=persona.telefono1 if persona else None,
        provincia=persona.provincia if persona else None,
        distrito=persona.distrito if persona else None,
        correo=getattr(persona, "correo", None) if persona else None,
        id_centro_asignado=id_centro_asignado,
        id_mesa_asignada=id_mesa_asignada,
        nombre_centro_asignado=nombre_centro_asignado,
        distritos_asignados=distritos_asignados
    )

    return {
        "access_token": access_token,
        "token_type": "bearer",
        "user": user_data
    }


@router.post("/change-password")
def change_password(
    request: ChangePasswordRequest,
    user_data: dict = Depends(get_current_user_with_persona),
    db: Session = Depends(get_db)
):
    user = user_data["usuario"]

    if not verify_password(request.old_password, user.clave):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Contraseña actual incorrecta"
        )

    if len(request.new_password) < 6:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="La nueva contraseña debe tener al menos 6 caracteres"
        )

    user.clave = get_password_hash(request.new_password)
    db.commit()

    return {"mensaje": "Contraseña actualizada exitosamente"}


@router.get("/me", response_model=PerfilUsuarioCompletoResponse)
def get_me(
    user_data: dict = Depends(get_current_user_with_persona),
    db: Session = Depends(get_db)
):
    user = user_data["usuario"]
    persona = user_data["persona"]

    centro_asignado = None
    id_centro_asignado = getattr(user, "id_centro_asignado", None)
    if id_centro_asignado:
        centro_asignado = db.query(CentroVotacion).filter(CentroVotacion.id_centro == id_centro_asignado).first()

    mesa_asignada = None
    id_mesa_asignada = getattr(user, "id_mesa_asignada", None)
    if id_mesa_asignada:
        mesa_asignada = db.query(Mesa).filter(Mesa.id_mesa == id_mesa_asignada).first()

    distritos_usuarios = db.query(UsuariosDistritos).filter(UsuariosDistritos.idusuario == user.idusuario).all()
    distritos_asignados_items = []
    for ud in distritos_usuarios:
        distrito_nombre = ud.distrito
        provincia_aprox = None
        centro_ejemplo = db.query(CentroVotacion).filter(CentroVotacion.distrito == distrito_nombre).first()
        if centro_ejemplo:
            provincia_aprox = centro_ejemplo.provincia
        distritos_asignados_items.append(
            DistritoAsignadoItem(distrito=distrito_nombre, provincia=provincia_aprox)
        )

    return {
        "idusuario": user.idusuario,
        "usuario": user.usuario,
        "rol": user.rol,
        "fecha_creado": user.fecha_creado,
        "estado": user.estado,
        "idpersona": user.idpersona,
        "persona": persona,
        "id_centro_asignado": id_centro_asignado,
        "id_mesa_asignada": id_mesa_asignada,
        "centro_asignado": centro_asignado,
        "mesa_asignada": mesa_asignada,
        "distritos_asignados": distritos_asignados_items,
    }


@router.put("/profile")
def update_profile(
    request: UpdateProfileRequest,
    user_data: dict = Depends(get_current_user_with_persona),
    db: Session = Depends(get_db)
):
    user = user_data["usuario"]
    persona = user_data["persona"]

    if not persona:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Perfil no encontrado"
        )

    if request.nombre is not None:
        persona.nombre = request.nombre
    if request.apellidos is not None:
        persona.apellidos = request.apellidos
    if request.telefono1 is not None:
        persona.telefono1 = request.telefono1
    if request.telefono2 is not None:
        persona.telefono2 = request.telefono2
    if request.direccion is not None:
        persona.direccion = request.direccion
    if request.distrito is not None:
        persona.distrito = request.distrito
    if request.provincia is not None:
        persona.provincia = request.provincia
    if request.correo is not None:
        persona.correo = request.correo

    db.commit()
    db.refresh(persona)

    return {
        "mensaje": "Perfil actualizado exitosamente",
        "persona": {
            "id_persona": persona.id_persona,
            "dni": persona.dni,
            "nombre": persona.nombre,
            "apellidos": persona.apellidos,
            "telefono1": persona.telefono1,
            "telefono2": persona.telefono2,
            "distrito": persona.distrito,
            "provincia": persona.provincia,
            "direccion": persona.direccion,
            "correo": getattr(persona, "correo", None)
        }
    }
