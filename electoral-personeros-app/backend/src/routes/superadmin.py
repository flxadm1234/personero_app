from typing import Optional, List
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session

from src.config.database import get_db
from src.middlewares.auth import RoleChecker, get_password_hash, get_current_user
from src.models.usuario import Usuarios
from src.models.persona import Persona
from src.models.usuarios_distritos import UsuariosDistritos
from src.schemas import (
    UsuarioCreateRequest,
    UsuarioUpdateRequest,
    UsuarioEstadoRequest,
    UsuarioResetPasswordRequest,
    UsuarioWithPersonaResponse,
    AdminDistritosUpdateRequest,
    DistritoAsignadoItem,
)

router = APIRouter(
    prefix="/api/superadmin",
    tags=["SuperAdmin"],
    dependencies=[Depends(RoleChecker(["SUPER_ADMIN"]))]
)


def _serialize_usuario(user: Usuarios, persona: Optional[Persona] = None):
    from src.schemas import PersonaResponse
    return UsuarioWithPersonaResponse(
        idusuario=user.idusuario,
        usuario=user.usuario,
        rol=user.rol,
        fecha_creado=user.fecha_creado,
        estado=user.estado,
        idpersona=user.idpersona,
        persona=PersonaResponse(
            id_persona=persona.id_persona,
            dni=persona.dni,
            nombre=persona.nombre,
            apellidos=persona.apellidos,
            telefono1=persona.telefono1,
            telefono2=persona.telefono2,
            correo=getattr(persona, "correo", None),
            distrito=persona.distrito,
            provincia=persona.provincia,
            direccion=persona.direccion
        ) if persona else None
    )


@router.post("/usuarios", status_code=status.HTTP_201_CREATED, response_model=UsuarioWithPersonaResponse)
def crear_usuario(
    request: UsuarioCreateRequest,
    db: Session = Depends(get_db),
    _: Usuarios = Depends(get_current_user)
):
    usuario_existente = db.query(Usuarios).filter(Usuarios.usuario == request.usuario).first()
    if usuario_existente:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El nombre de usuario ya está en uso"
        )

    persona_existente = db.query(Persona).filter(Persona.dni == request.dni).first()
    if persona_existente:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Ya existe una persona con este DNI"
        )

    persona = Persona(
        dni=request.dni,
        nombre=request.nombre,
        apellidos=request.apellidos,
        telefono1=request.telefono1,
        telefono2=request.telefono2,
        correo=getattr(request, "correo", None),
        distrito=request.distrito,
        provincia=request.provincia,
        direccion=request.direccion
    )
    db.add(persona)
    db.flush()

    nuevo_usuario = Usuarios(
        idpersona=persona.id_persona,
        usuario=request.usuario,
        clave=get_password_hash(request.clave),
        rol=request.rol,
        estado=True
    )
    db.add(nuevo_usuario)
    db.commit()
    db.refresh(nuevo_usuario)
    db.refresh(persona)

    return _serialize_usuario(nuevo_usuario, persona)


@router.get("/usuarios", response_model=List[UsuarioWithPersonaResponse])
def listar_usuarios(
    rol: Optional[str] = Query(None),
    estado: Optional[bool] = Query(None),
    busqueda: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    _: Usuarios = Depends(get_current_user)
):
    query = db.query(Usuarios)

    if rol:
        query = query.filter(Usuarios.rol == rol)
    if estado is not None:
        query = query.filter(Usuarios.estado == estado)
    if busqueda:
        busqueda_like = f"%{busqueda}%"
        query = query.filter(Usuarios.usuario.ilike(busqueda_like))

    usuarios = query.all()

    resultados = []
    for user in usuarios:
        persona = db.query(Persona).filter(Persona.id_persona == user.idpersona).first()
        resultados.append(_serialize_usuario(user, persona))

    return resultados


@router.put("/usuarios/{idusuario}", response_model=UsuarioWithPersonaResponse)
def actualizar_usuario(
    idusuario: int,
    request: UsuarioUpdateRequest,
    db: Session = Depends(get_db),
    _: Usuarios = Depends(get_current_user)
):
    usuario = db.query(Usuarios).filter(Usuarios.idusuario == idusuario).first()
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario no encontrado"
        )

    if request.usuario and request.usuario != usuario.usuario:
        usuario_existente = db.query(Usuarios).filter(Usuarios.usuario == request.usuario).first()
        if usuario_existente:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="El nombre de usuario ya está en uso"
            )
        usuario.usuario = request.usuario

    if request.clave:
        usuario.clave = get_password_hash(request.clave)

    if request.rol:
        usuario.rol = request.rol

    persona = db.query(Persona).filter(Persona.id_persona == usuario.idpersona).first()
    if persona:
        if request.dni is not None:
            if request.dni != persona.dni:
                persona_existente = db.query(Persona).filter(Persona.dni == request.dni).first()
                if persona_existente:
                    raise HTTPException(
                        status_code=status.HTTP_400_BAD_REQUEST,
                        detail="Ya existe una persona con este DNI"
                    )
            persona.dni = request.dni
        if request.nombre is not None:
            persona.nombre = request.nombre
        if request.apellidos is not None:
            persona.apellidos = request.apellidos
        if request.telefono1 is not None:
            persona.telefono1 = request.telefono1
        if request.telefono2 is not None:
            persona.telefono2 = request.telefono2
        if getattr(request, "correo", None) is not None:
            persona.correo = getattr(request, "correo", None)
        if request.distrito is not None:
            persona.distrito = request.distrito
        if request.provincia is not None:
            persona.provincia = request.provincia
        if request.direccion is not None:
            persona.direccion = request.direccion

    db.commit()
    db.refresh(usuario)
    if persona:
        db.refresh(persona)

    return _serialize_usuario(usuario, persona)


@router.patch("/usuarios/{idusuario}/estado", response_model=UsuarioWithPersonaResponse)
def actualizar_estado_usuario(
    idusuario: int,
    request: UsuarioEstadoRequest,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user)
):
    if idusuario == current_user.idusuario:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No puede cambiar el estado de su propio usuario"
        )

    usuario = db.query(Usuarios).filter(Usuarios.idusuario == idusuario).first()
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario no encontrado"
        )

    usuario.estado = request.estado
    db.commit()
    db.refresh(usuario)

    persona = db.query(Persona).filter(Persona.id_persona == usuario.idpersona).first()
    return _serialize_usuario(usuario, persona)


@router.post("/usuarios/{idusuario}/reset-password")
def reset_password(
    idusuario: int,
    request: UsuarioResetPasswordRequest,
    db: Session = Depends(get_db),
    _: Usuarios = Depends(get_current_user)
):
    usuario = db.query(Usuarios).filter(Usuarios.idusuario == idusuario).first()
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario no encontrado"
        )

    usuario.clave = get_password_hash(request.new_clave)
    db.commit()

    return {
        "mensaje": "Contraseña reseteada exitosamente"
    }


@router.get("/usuarios/{idusuario}/distritos")
def obtener_distritos_usuario(
    idusuario: int,
    db: Session = Depends(get_db),
    _: Usuarios = Depends(get_current_user)
):
    usuario = db.query(Usuarios).filter(Usuarios.idusuario == idusuario).first()
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario no encontrado"
        )

    distritos_qs = db.query(UsuariosDistritos).filter(UsuariosDistritos.idusuario == idusuario).all()
    distritos = [ud.distrito for ud in distritos_qs]

    return {
        "idusuario": idusuario,
        "distritos": distritos
    }


@router.put("/usuarios/{idusuario}/distritos")
def actualizar_distritos_usuario(
    idusuario: int,
    request: AdminDistritosUpdateRequest,
    db: Session = Depends(get_db),
    _: Usuarios = Depends(get_current_user)
):
    usuario = db.query(Usuarios).filter(Usuarios.idusuario == idusuario).first()
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario no encontrado"
        )

    db.query(UsuariosDistritos).filter(UsuariosDistritos.idusuario == idusuario).delete()

    nuevos = [
        UsuariosDistritos(idusuario=idusuario, distrito=d)
        for d in request.distritos
    ]
    db.bulk_save_objects(nuevos)
    db.commit()

    distritos_qs = db.query(UsuariosDistritos).filter(UsuariosDistritos.idusuario == idusuario).all()
    distritos = [ud.distrito for ud in distritos_qs]

    return {
        "idusuario": idusuario,
        "distritos": distritos
    }


@router.get("/usuarios/{idusuario}/distritos-asignados", response_model=List[DistritoAsignadoItem])
def obtener_distritos_asignados_usuario(
    idusuario: int,
    db: Session = Depends(get_db),
    _: Usuarios = Depends(get_current_user)
):
    usuario = db.query(Usuarios).filter(Usuarios.idusuario == idusuario).first()
    if not usuario:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Usuario no encontrado"
        )

    distritos_qs = db.query(UsuariosDistritos).filter(UsuariosDistritos.idusuario == idusuario).all()
    resultado = [
        DistritoAsignadoItem(distrito=ud.distrito, provincia=None)
        for ud in distritos_qs
    ]

    return resultado
