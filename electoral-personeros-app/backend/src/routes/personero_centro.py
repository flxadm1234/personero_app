from typing import Optional, List
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from sqlalchemy import and_

from src.config.database import get_db
from src.middlewares.auth import RoleChecker, get_current_user_with_persona, get_current_user
from src.models.usuario import Usuarios
from src.models.persona import Persona
from src.models.centro_votacion import CentroVotacion
from src.models.mesa import Mesa
from src.models.personeros_users import PersonerosUsers
from src.schemas import (
    SolicitudPersoneroCentroResponse,
    PersoneroDisponibleResponse,
    MesaCentroResponse,
    AsignarMesaRequest,
    PersonaResponse,
    CentroVotacionResponse,
    MesaResponse,
    PersonaDNILookupResponse,
    RegistrarYAsignarMesaRequest,
    DesasignarMesaRequest,
)
from datetime import datetime

router = APIRouter(
    prefix="/api/personero-centro",
    tags=["Personero Centro"],
    dependencies=[Depends(RoleChecker(["SUPER_ADMIN", "ADMINISTRADOR", "PERSONERO_CENTRO"]))]
)


def _get_pc_id_centro_asignado(user: Usuarios, db: Session) -> int | None:
    if user.rol != "PERSONERO_CENTRO" or not user.idpersona:
        return None
    pu = (
        db.query(PersonerosUsers)
        .filter(
            PersonerosUsers.id_persona == user.idpersona,
            PersonerosUsers.tipo_personero_solicitado == "CENTRO",
            PersonerosUsers.estado_vinculacion == "ASIGNADO",
        )
        .first()
    )
    return pu.id_centro_asignado if pu else None


def _validar_mesa_pertenece_a_centro(db: Session, id_mesa: int, id_centro_esperado: int) -> bool:
    mesa = db.query(Mesa).filter(Mesa.id_mesa == id_mesa).first()
    if not mesa:
        raise HTTPException(404, "Mesa no encontrada")
    if mesa.cod_local:
        cv = db.query(CentroVotacion).filter(CentroVotacion.codigo_local == mesa.cod_local).first()
        if cv and cv.id_centro == id_centro_esperado:
            return True
    return False


@router.get("/personas-by-dni/{dni}", response_model=PersonaDNILookupResponse)
def lookup_persona_by_dni_pc(
    dni: str,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    if current_user.rol == "PERSONERO_CENTRO":
        id_centro = _get_pc_id_centro_asignado(current_user, db)
        if id_centro is None:
            raise HTTPException(403, "Sin centro asignado. Contacte al administrador.")

    persona = db.query(Persona).filter(Persona.dni == dni).first()
    if not persona:
        return PersonaDNILookupResponse(
            encontrado=False,
            persona=None,
            id_solicitud_activa=None,
            id_solicitud_tipo=None,
        )

    solicitud_activa = (
        db.query(PersonerosUsers)
        .filter(
            PersonerosUsers.id_persona == persona.id_persona,
            PersonerosUsers.estado_vinculacion.in_(["SOLICITADO", "ASIGNADO"])
        )
        .order_by(PersonerosUsers.fecha_registro.desc())
        .first()
    )

    return PersonaDNILookupResponse(
        encontrado=True,
        persona=PersonaResponse.model_validate(persona),
        id_solicitud_activa=solicitud_activa.id_solicitud if solicitud_activa else None,
        id_solicitud_tipo=solicitud_activa.tipo_personero_solicitado if solicitud_activa else None,
        sugerencia_nombre=persona.nombre,
        sugerencia_apellidos=persona.apellidos,
    )


@router.post("/asignaciones/desasignar-mesa")
def desasignar_mesa_pc(
    body: DesasignarMesaRequest,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    import traceback as _tb
    try:
        id_centro_asignado = None
        if current_user.rol == "PERSONERO_CENTRO":
            id_centro_asignado = _get_pc_id_centro_asignado(current_user, db)
            if id_centro_asignado is None:
                raise HTTPException(403, "Sin centro asignado. Contacte al administrador.")
            if not _validar_mesa_pertenece_a_centro(db, body.id_mesa, id_centro_asignado):
                raise HTTPException(403, "La mesa no pertenece al centro de votación asignado.")

        pu = (
            db.query(PersonerosUsers)
            .filter(
                PersonerosUsers.id_mesa_asignada == body.id_mesa,
                PersonerosUsers.estado_vinculacion == "ASIGNADO",
                PersonerosUsers.tipo_personero_solicitado == "MESA",
            )
            .first()
        )
        if not pu:
            raise HTTPException(404, "No existe personero asignado a esta mesa en estado ASIGNADO")

        id_persona_ref = pu.id_persona
        pu.id_mesa_asignada = None
        pu.estado_vinculacion = "SOLICITADO"

        if id_persona_ref:
            usuario_asociado = db.query(Usuarios).filter(Usuarios.idpersona == id_persona_ref).first()
            if usuario_asociado:
                try:
                    from sqlalchemy import inspect as _insp
                    cols_usr = {c.key for c in _insp(Usuarios).mapper.column_attrs}
                    if "id_mesa_asignada" in cols_usr:
                        if usuario_asociado.id_mesa_asignada == body.id_mesa:
                            usuario_asociado.id_mesa_asignada = None
                except Exception:
                    pass

        db.commit()
        return {"mensaje": "Mesa desasignada exitosamente", "id_mesa": body.id_mesa}
    except HTTPException:
        raise
    except Exception as _e:
        db.rollback()
        err = _tb.format_exc()
        print("ERROR desasignar-mesa personero-centro:", err)
        raise HTTPException(status_code=500, detail=f"{type(_e).__name__}: {_e}")


@router.post("/asignaciones/registrar-y-asignar-mesa")
def registrar_y_asignar_mesa_pc(
    request: RegistrarYAsignarMesaRequest,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    import traceback as _tb
    try:
        id_centro_asignado_scope = None
        if current_user.rol == "PERSONERO_CENTRO":
            id_centro_asignado_scope = _get_pc_id_centro_asignado(current_user, db)
            if id_centro_asignado_scope is None:
                raise HTTPException(403, "Sin centro asignado. Contacte al administrador.")
            if not _validar_mesa_pertenece_a_centro(db, request.id_mesa_asignada, id_centro_asignado_scope):
                raise HTTPException(403, "La mesa no pertenece al centro de votación asignado.")

        persona = db.query(Persona).filter(Persona.dni == request.dni).first()
        if not persona:
            persona = Persona(
                dni=request.dni,
                nombre=request.nombre,
                apellidos=request.apellidos,
                telefono1=request.telefono1,
                telefono2=request.telefono2,
                correo=request.correo,
                distrito=request.distrito,
                provincia=request.provincia,
                direccion=request.direccion,
            )
            db.add(persona)
            db.flush()
        else:
            if request.nombre is not None and request.nombre != "":
                persona.nombre = request.nombre
            if request.apellidos is not None and request.apellidos != "":
                persona.apellidos = request.apellidos
            if request.telefono1 is not None and request.telefono1 != "":
                persona.telefono1 = request.telefono1
            if request.telefono2 is not None:
                persona.telefono2 = request.telefono2
            if request.correo is not None:
                persona.correo = request.correo
            if request.distrito is not None and request.distrito != "":
                persona.distrito = request.distrito
            if request.provincia is not None and request.provincia != "":
                persona.provincia = request.provincia
            if request.direccion is not None:
                persona.direccion = request.direccion

        pu = (
            db.query(PersonerosUsers)
            .filter(
                PersonerosUsers.id_persona == persona.id_persona,
                PersonerosUsers.tipo_personero_solicitado == "MESA",
                PersonerosUsers.id_mesa_asignada.is_(None),
            )
            .order_by(PersonerosUsers.fecha_registro.desc())
            .first()
        )
        if not pu:
            pu = PersonerosUsers(
                id_persona=persona.id_persona,
                tipo_personero_solicitado="MESA",
                estado_vinculacion="SOLICITADO",
            )
            db.add(pu)
            db.flush()

        mesa = db.query(Mesa).filter(Mesa.id_mesa == request.id_mesa_asignada).first()
        if not mesa:
            raise HTTPException(404, "Mesa no encontrada")

        mesa_ocupada = (
            db.query(PersonerosUsers)
            .filter(
                PersonerosUsers.id_mesa_asignada == request.id_mesa_asignada,
                PersonerosUsers.estado_vinculacion == "ASIGNADO",
                PersonerosUsers.tipo_personero_solicitado == "MESA",
            )
            .first()
        )
        if mesa_ocupada and mesa_ocupada.id_solicitud != pu.id_solicitud:
            raise HTTPException(400, "La mesa ya se encuentra asignada a otro personero")

        id_centro_asignado = request.id_centro_asignado
        if id_centro_asignado is None and mesa.cod_local:
            cv = db.query(CentroVotacion).filter(CentroVotacion.codigo_local == mesa.cod_local).first()
            if cv:
                id_centro_asignado = cv.id_centro

        pu.id_mesa_asignada = request.id_mesa_asignada
        pu.estado_vinculacion = "ASIGNADO"
        pu.fecha_asignacion = datetime.utcnow()
        try:
            from sqlalchemy import inspect as _insp2
            cols_pu = {c.key for c in _insp2(PersonerosUsers).mapper.column_attrs}
            if "id_usuario_asignador" in cols_pu:
                pu.id_usuario_asignador = current_user.idusuario
        except Exception:
            pass

        if id_centro_asignado is not None:
            pu.id_centro_asignado = id_centro_asignado

        db.commit()
        db.refresh(pu)
        db.refresh(persona)
        db.refresh(mesa)

        return {
            "mensaje": f"{persona.nombre} {persona.apellidos} asignado/a a mesa {mesa.num_mesa}",
            "id_solicitud": pu.id_solicitud,
            "nombre_persona": f"{persona.nombre} {persona.apellidos}",
            "num_mesa": mesa.num_mesa,
            "id_mesa": mesa.id_mesa,
            "id_centro_asignado": id_centro_asignado,
        }
    except HTTPException:
        raise
    except Exception as _e:
        db.rollback()
        err = _tb.format_exc()
        print("ERROR registrar-y-asignar-mesa personero-centro:", err)
        raise HTTPException(status_code=500, detail=f"{type(_e).__name__}: {_e}")


@router.get("/solicitudes", response_model=List[SolicitudPersoneroCentroResponse])
def obtener_solicitudes(
    estado: Optional[str] = Query(None, pattern="^(SOLICITADO|ASIGNADO|RECHAZADO)$"),
    skip: int = Query(0, ge=0),
    limit: int = Query(50, ge=1, le=200),
    current_user: Usuarios = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    if current_user.rol == "PERSONERO_CENTRO":
        id_centro = _get_pc_id_centro_asignado(current_user, db)
        if id_centro is None:
            raise HTTPException(403, "Sin centro asignado. Contacte al administrador.")

    query = (
        db.query(PersonerosUsers)
        .join(Persona, PersonerosUsers.id_persona == Persona.id_persona)
    )
    if estado:
        query = query.filter(PersonerosUsers.estado_vinculacion == estado)

    query = query.order_by(PersonerosUsers.fecha_registro.desc())
    solicitudes = query.offset(skip).limit(limit).all()

    resultados = []
    for pu in solicitudes:
        persona = pu.persona
        centro = pu.centro_asignado
        mesa = pu.mesa_asignada
        resultados.append(SolicitudPersoneroCentroResponse(
            id_solicitud=pu.id_solicitud,
            tipo_personero_solicitado=pu.tipo_personero_solicitado or "",
            estado_vinculacion=pu.estado_vinculacion,
            fecha_registro=pu.fecha_registro,
            id_persona=persona.id_persona if persona else 0,
            persona=PersonaResponse(
                id_persona=persona.id_persona,
                dni=persona.dni,
                nombre=persona.nombre,
                apellidos=persona.apellidos,
                telefono1=persona.telefono1,
                telefono2=persona.telefono2,
                distrito=persona.distrito,
                provincia=persona.provincia,
                direccion=persona.direccion
            ) if persona else None,
            centro_asignado=CentroVotacionResponse(
                id_centro=centro.id_centro,
                codigo_local=centro.codigo_local,
                ubigeo=centro.ubigeo,
                nombre_local=centro.nombre_local,
                direccion_local=centro.direccion_local,
                departamento=centro.departamento,
                provincia=centro.provincia,
                distrito=centro.distrito
            ) if centro else None,
            mesa_asignada=MesaResponse(
                id_mesa=mesa.id_mesa,
                num_mesa=mesa.num_mesa,
                cod_local=mesa.cod_local,
                codi_ubigeo=mesa.codi_ubigeo,
                nombre_local=mesa.nombre_local,
                direccion_local=mesa.direccion_local,
                departamento=mesa.departamento,
                provincia=mesa.provincia,
                distrito=mesa.distrito
            ) if mesa else None
        ))

    return resultados


@router.get("/personeros-disponibles", response_model=List[PersoneroDisponibleResponse])
def personeros_disponibles(
    busqueda: Optional[str] = Query(None),
    tipo: Optional[str] = Query(None, description="MESA o CENTRO"),
    skip: int = Query(0, ge=0),
    limit: int = Query(50, ge=1, le=200),
    current_user: Usuarios = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    if current_user.rol == "PERSONERO_CENTRO":
        id_centro = _get_pc_id_centro_asignado(current_user, db)
        if id_centro is None:
            raise HTTPException(403, "Sin centro asignado. Contacte al administrador.")

    query = (
        db.query(PersonerosUsers, Persona)
        .join(Persona, PersonerosUsers.id_persona == Persona.id_persona)
        .filter(
            PersonerosUsers.id_mesa_asignada.is_(None),
            PersonerosUsers.id_centro_asignado.is_(None)
        )
    )
    if tipo:
        query = query.filter(PersonerosUsers.tipo_personero_solicitado == tipo.upper())
    else:
        query = query.filter(PersonerosUsers.tipo_personero_solicitado == "MESA")

    if busqueda:
        busqueda_like = f"%{busqueda}%"
        query = query.filter(
            (Persona.nombre.ilike(busqueda_like)) |
            (Persona.apellidos.ilike(busqueda_like)) |
            (Persona.dni.ilike(busqueda_like))
        )

    resultados = query.order_by(Persona.apellidos.asc(), Persona.nombre.asc()).offset(skip).limit(limit).all()

    personeros = []
    for pu, p in resultados:
        usuario = db.query(Usuarios).filter(Usuarios.idpersona == p.id_persona).first()
        id_usuario_val = usuario.idusuario if usuario else None
        personeros.append(PersoneroDisponibleResponse(
            nombre=p.nombre or "",
            apellidos=p.apellidos or "",
            dni=p.dni or "",
            telefono1=p.telefono1 or "",
            id_solicitud=pu.id_solicitud,
            idusuario=id_usuario_val,
            id_usuario=id_usuario_val,
            idUsuario=id_usuario_val,
            id_persona=p.id_persona,
            tipo_personero=pu.tipo_personero_solicitado
        ))

    return personeros


@router.get("/mesas-centro/{id_centro}", response_model=List[MesaCentroResponse])
def mesas_por_centro(
    id_centro: int,
    user_data: dict = Depends(get_current_user_with_persona),
    db: Session = Depends(get_db)
):
    centro = db.query(CentroVotacion).filter(CentroVotacion.id_centro == id_centro).first()
    if not centro:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Centro de votación no encontrado"
        )

    usuario = user_data["usuario"]
    if usuario.rol == "PERSONERO_CENTRO":
        solicitud_centro = db.query(PersonerosUsers).filter(
            PersonerosUsers.id_persona == usuario.idpersona,
            PersonerosUsers.tipo_personero_solicitado == "CENTRO",
            PersonerosUsers.estado_vinculacion == "ASIGNADO"
        ).first()
        if not solicitud_centro or solicitud_centro.id_centro_asignado != id_centro:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="No tiene permiso para acceder a este centro de votación"
            )

    mesas = db.query(Mesa).filter(Mesa.cod_local == centro.codigo_local).order_by(Mesa.num_mesa).all()

    resultado_mesas = []
    for m in mesas:
        asignacion = db.query(PersonerosUsers, Persona).join(
            Persona, PersonerosUsers.id_persona == Persona.id_persona
        ).filter(
            PersonerosUsers.id_mesa_asignada == m.id_mesa,
            PersonerosUsers.estado_vinculacion == "ASIGNADO"
        ).first()

        if asignacion:
            pu, p = asignacion
            resultado_mesas.append(MesaCentroResponse(
                id_mesa=m.id_mesa,
                num_mesa=m.num_mesa,
                estado="OCUPADA",
                personero={
                    "id_solicitud": pu.id_solicitud,
                    "nombre": p.nombre,
                    "apellidos": p.apellidos,
                    "dni": p.dni,
                    "telefono1": p.telefono1
                }
            ))
        else:
            resultado_mesas.append(MesaCentroResponse(
                id_mesa=m.id_mesa,
                num_mesa=m.num_mesa,
                estado="LIBRE",
                personero=None
            ))

    return resultado_mesas


@router.post("/asignar-mesa")
def asignar_mesa(
    request: AsignarMesaRequest,
    user_data: dict = Depends(get_current_user_with_persona),
    db: Session = Depends(get_db)
):
    usuario = user_data["usuario"]

    if usuario.rol == "PERSONERO_CENTRO":
        id_centro = _get_pc_id_centro_asignado(usuario, db)
        if id_centro is None:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="Sin centro asignado. Contacte al administrador."
            )

    mesa = db.query(Mesa).filter(Mesa.id_mesa == request.id_mesa).first()
    if not mesa:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Mesa no encontrada"
        )

    if usuario.rol == "PERSONERO_CENTRO":
        centro = db.query(CentroVotacion).filter(
            CentroVotacion.codigo_local == mesa.cod_local
        ).first()
        if centro:
            solicitud_centro = db.query(PersonerosUsers).filter(
                PersonerosUsers.id_persona == usuario.idpersona,
                PersonerosUsers.tipo_personero_solicitado == "CENTRO",
                PersonerosUsers.estado_vinculacion == "ASIGNADO"
            ).first()
            if not solicitud_centro or solicitud_centro.id_centro_asignado != centro.id_centro:
                raise HTTPException(
                    status_code=status.HTTP_403_FORBIDDEN,
                    detail="No tiene permiso para asignar mesas en este centro"
                )

    solicitud_personero = db.query(PersonerosUsers).filter(
        PersonerosUsers.id_solicitud == request.id_solicitud_personero_mesa
    ).first()
    if not solicitud_personero:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Solicitud de personero de mesa no encontrada"
        )

    if solicitud_personero.tipo_personero_solicitado != "MESA":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="La solicitud no corresponde a un personero de mesa"
        )

    if solicitud_personero.estado_vinculacion != "ASIGNADO":
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="El personero debe estar en estado ASIGNADO antes de asignar mesa"
        )

    if solicitud_personero.id_mesa_asignada is not None:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Este personero ya está asignado a otra mesa"
        )

    mesa_ocupada = db.query(PersonerosUsers).filter(
        PersonerosUsers.id_mesa_asignada == request.id_mesa,
        PersonerosUsers.estado_vinculacion == "ASIGNADO"
    ).first()
    if mesa_ocupada:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Esta mesa ya tiene un personero asignado"
        )

    solicitud_personero.id_mesa_asignada = request.id_mesa
    solicitud_personero.estado_vinculacion = "ASIGNADO"
    db.commit()
    db.refresh(solicitud_personero)

    persona = db.query(Persona).filter(Persona.id_persona == solicitud_personero.id_persona).first()

    return {
        "mensaje": "Personero asignado a la mesa exitosamente",
        "asignacion": {
            "id_solicitud": solicitud_personero.id_solicitud,
            "mesa": {
                "id_mesa": mesa.id_mesa,
                "num_mesa": mesa.num_mesa,
                "cod_local": mesa.cod_local
            },
            "personero": {
                "id_persona": persona.id_persona if persona else None,
                "dni": persona.dni if persona else "",
                "nombre": persona.nombre if persona else "",
                "apellidos": persona.apellidos if persona else ""
            } if persona else None
        }
    }
