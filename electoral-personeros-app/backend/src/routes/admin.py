from typing import Optional, List
from fastapi import APIRouter, Depends, HTTPException, status, Query
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session
from sqlalchemy import func, and_
from datetime import datetime
import io
from openpyxl import Workbook
from openpyxl.styles import Font, PatternFill, Alignment

from src.config.database import get_db
from src.middlewares.auth import RoleChecker, get_current_user, get_password_hash
from src.models.usuario import Usuarios
from src.models.persona import Persona
from src.models.centro_votacion import CentroVotacion
from src.models.mesa import Mesa
from src.models.personeros_users import PersonerosUsers
from src.models.usuarios_distritos import UsuariosDistritos
from src.schemas import (
    PersoneroCentroCreateRequest,
    PersoneroCentroResponse,
    CentroVotacionResponse,
    MesaCentroResponse,
    UsuarioWithPersonaResponse,
    CoberturaResponse,
    DesgloseItem,
    SolicitudPendienteResponse,
    PersonaResponse,
    PersonaDNILookupResponse,
    RegistrarYAsignarMesaRequest,
    DesasignarMesaRequest,
    UsuarioCreateRequest,
    UsuarioUpdateRequest,
)

router = APIRouter(
    prefix="/api/admin",
    tags=["Admin"],
    dependencies=[Depends(RoleChecker(["SUPER_ADMIN", "ADMINISTRADOR"]))]
)


def _get_admin_distritos_filter(user: Usuarios, db: Session) -> List[str] | None:
    if user.rol == "SUPER_ADMIN":
        return None
    if user.rol == "ADMINISTRADOR":
        distritos_rows = db.query(UsuariosDistritos.distrito).filter(
            UsuariosDistritos.idusuario == user.idusuario
        ).all()
        distritos_list = [row.distrito for row in distritos_rows if row.distrito]
        return distritos_list if len(distritos_list) > 0 else []
    return None


def _calcular_cobertura(db: Session, provincia: Optional[str] = None, distritos_filter: Optional[List[str]] = None) -> CoberturaResponse:
    query_centros = db.query(CentroVotacion)
    if provincia:
        query_centros = query_centros.filter(CentroVotacion.provincia == provincia)
    if distritos_filter is not None:
        query_centros = query_centros.filter(CentroVotacion.distrito.in_(distritos_filter))
    total_centros = query_centros.count()

    subquery_centros_asignados = (
        db.query(PersonerosUsers.id_centro_asignado)
        .filter(
            PersonerosUsers.estado_vinculacion == "ASIGNADO",
            PersonerosUsers.tipo_personero_solicitado == "CENTRO",
            PersonerosUsers.id_centro_asignado.isnot(None)
        )
        .distinct()
    )
    query_centros_asignados = db.query(CentroVotacion).filter(CentroVotacion.id_centro.in_(subquery_centros_asignados))
    if provincia:
        query_centros_asignados = query_centros_asignados.filter(CentroVotacion.provincia == provincia)
    if distritos_filter is not None:
        query_centros_asignados = query_centros_asignados.filter(CentroVotacion.distrito.in_(distritos_filter))
    centros_asignados = query_centros_asignados.count()

    centros_sin_asignar = total_centros - centros_asignados
    porcentaje_centros = round((centros_asignados / total_centros * 100), 2) if total_centros > 0 else 0.0

    query_mesas = db.query(Mesa)
    if provincia or distritos_filter is not None:
        query_mesas = query_mesas.join(
            CentroVotacion, Mesa.cod_local == CentroVotacion.codigo_local
        )
    if provincia:
        query_mesas = query_mesas.filter(CentroVotacion.provincia == provincia)
    if distritos_filter is not None:
        query_mesas = query_mesas.filter(CentroVotacion.distrito.in_(distritos_filter))
    total_mesas = query_mesas.count()

    subquery_mesas_asignadas = (
        db.query(PersonerosUsers.id_mesa_asignada)
        .filter(
            PersonerosUsers.estado_vinculacion == "ASIGNADO",
            PersonerosUsers.tipo_personero_solicitado == "MESA",
            PersonerosUsers.id_mesa_asignada.isnot(None)
        )
        .distinct()
    )
    query_mesas_asignadas = db.query(Mesa).filter(Mesa.id_mesa.in_(subquery_mesas_asignadas))
    if provincia or distritos_filter is not None:
        query_mesas_asignadas = query_mesas_asignadas.join(
            CentroVotacion, Mesa.cod_local == CentroVotacion.codigo_local
        )
    if provincia:
        query_mesas_asignadas = query_mesas_asignadas.filter(CentroVotacion.provincia == provincia)
    if distritos_filter is not None:
        query_mesas_asignadas = query_mesas_asignadas.filter(CentroVotacion.distrito.in_(distritos_filter))
    mesas_asignadas = query_mesas_asignadas.count()

    mesas_sin_asignar = total_mesas - mesas_asignadas
    porcentaje_mesas = round((mesas_asignadas / total_mesas * 100), 2) if total_mesas > 0 else 0.0

    query_pc = db.query(PersonerosUsers).filter(
        PersonerosUsers.estado_vinculacion == "ASIGNADO",
        PersonerosUsers.tipo_personero_solicitado == "CENTRO"
    )
    if distritos_filter is not None:
        query_pc = query_pc.join(Persona, PersonerosUsers.id_persona == Persona.id_persona).filter(
            Persona.distrito.in_(distritos_filter)
        )
    total_personeros_centro = query_pc.count()

    query_pm = db.query(PersonerosUsers).filter(
        PersonerosUsers.estado_vinculacion == "ASIGNADO",
        PersonerosUsers.tipo_personero_solicitado == "MESA"
    )
    if distritos_filter is not None:
        query_pm = query_pm.join(Persona, PersonerosUsers.id_persona == Persona.id_persona).filter(
            Persona.distrito.in_(distritos_filter)
        )
    total_personeros_mesa = query_pm.count()

    return CoberturaResponse(
        total_centros=total_centros,
        centros_asignados=centros_asignados,
        centros_sin_asignar=centros_sin_asignar,
        porcentaje_centros=porcentaje_centros,
        total_mesas=total_mesas,
        mesas_asignadas=mesas_asignadas,
        mesas_sin_asignar=mesas_sin_asignar,
        porcentaje_mesas=porcentaje_mesas,
        total_personeros_centro=total_personeros_centro,
        total_personeros_mesa=total_personeros_mesa
    )


def _calcular_desglose(db: Session, distritos_filter: Optional[List[str]] = None) -> List[DesgloseItem]:
    query_base = db.query(
        CentroVotacion.provincia.label("provincia"),
        CentroVotacion.distrito.label("distrito"),
        func.count(CentroVotacion.id_centro).label("total_centros"),
    )
    if distritos_filter is not None:
        query_base = query_base.filter(CentroVotacion.distrito.in_(distritos_filter))
    resultados = (
        query_base
        .group_by(CentroVotacion.provincia, CentroVotacion.distrito)
        .all()
    )

    desglose = []
    for row in resultados:
        provincia = row.provincia or "Sin asignar"
        distrito = row.distrito or "Sin asignar"
        total_centros = row.total_centros or 0

        subquery_centros_asignados = (
            db.query(PersonerosUsers.id_centro_asignado)
            .filter(
                PersonerosUsers.estado_vinculacion == "ASIGNADO",
                PersonerosUsers.tipo_personero_solicitado == "CENTRO",
                PersonerosUsers.id_centro_asignado.isnot(None)
            )
            .distinct()
            .subquery()
        )
        q_centros_asignados = (
            db.query(func.count(CentroVotacion.id_centro))
            .filter(
                CentroVotacion.id_centro.in_(subquery_centros_asignados),
                CentroVotacion.provincia == row.provincia,
                CentroVotacion.distrito == row.distrito
            )
        )
        if distritos_filter is not None:
            q_centros_asignados = q_centros_asignados.filter(CentroVotacion.distrito.in_(distritos_filter))
        centros_asignados = q_centros_asignados.scalar() or 0

        q_total_mesas = (
            db.query(func.count(Mesa.id_mesa))
            .join(CentroVotacion, Mesa.cod_local == CentroVotacion.codigo_local)
            .filter(
                CentroVotacion.provincia == row.provincia,
                CentroVotacion.distrito == row.distrito
            )
        )
        if distritos_filter is not None:
            q_total_mesas = q_total_mesas.filter(CentroVotacion.distrito.in_(distritos_filter))
        total_mesas = q_total_mesas.scalar() or 0

        subquery_mesas_asignadas = (
            db.query(PersonerosUsers.id_mesa_asignada)
            .filter(
                PersonerosUsers.estado_vinculacion == "ASIGNADO",
                PersonerosUsers.tipo_personero_solicitado == "MESA",
                PersonerosUsers.id_mesa_asignada.isnot(None)
            )
            .distinct()
            .subquery()
        )
        q_mesas_asignadas = (
            db.query(func.count(Mesa.id_mesa))
            .join(CentroVotacion, Mesa.cod_local == CentroVotacion.codigo_local)
            .filter(
                CentroVotacion.provincia == row.provincia,
                CentroVotacion.distrito == row.distrito,
                Mesa.id_mesa.in_(subquery_mesas_asignadas)
            )
        )
        if distritos_filter is not None:
            q_mesas_asignadas = q_mesas_asignadas.filter(CentroVotacion.distrito.in_(distritos_filter))
        mesas_asignadas = q_mesas_asignadas.scalar() or 0

        desglose.append(DesgloseItem(
            provincia=provincia,
            distrito=distrito,
            total_centros=total_centros,
            centros_asignados=centros_asignados,
            total_mesas=total_mesas,
            mesas_asignadas=mesas_asignadas
        ))

    return desglose


@router.get("/personas-by-dni/{dni}", response_model=PersonaDNILookupResponse)
def lookup_persona_by_dni_admin(
    dni: str,
    db: Session = Depends(get_db),
    _: Usuarios = Depends(get_current_user),
):
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


@router.post("/asignaciones/personero-mesa/desasignar-mesa")
def desasignar_mesa_admin(
    body: DesasignarMesaRequest,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    import traceback as _tb
    try:
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
        print("ERROR desasignar-mesa admin:", err)
        raise HTTPException(status_code=500, detail=f"{type(_e).__name__}: {_e}")


@router.post("/asignaciones/personero-mesa/registrar-y-asignar-mesa")
def registrar_y_asignar_mesa_admin(
    request: RegistrarYAsignarMesaRequest,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    import traceback as _tb
    try:
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
        print("ERROR registrar-y-asignar-mesa admin:", err)
        raise HTTPException(status_code=500, detail=f"{type(_e).__name__}: {_e}")


@router.post("/personeros-centro", status_code=status.HTTP_201_CREATED, response_model=PersoneroCentroResponse)
def crear_personero_centro(
    request: PersoneroCentroCreateRequest,
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
        distrito=request.distrito,
        provincia=request.provincia,
        direccion=request.direccion
    )
    db.add(persona)
    db.flush()

    usuario = Usuarios(
        idpersona=persona.id_persona,
        usuario=request.usuario,
        clave=get_password_hash(request.clave),
        rol="PERSONERO_CENTRO",
        estado=True
    )
    db.add(usuario)
    db.commit()
    db.refresh(usuario)
    db.refresh(persona)

    return PersoneroCentroResponse(
        idusuario=usuario.idusuario,
        usuario=usuario.usuario,
        rol=usuario.rol,
        estado=usuario.estado,
        idpersona=persona.id_persona,
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
        )
    )


@router.get("/personeros-centro", response_model=List[PersoneroCentroResponse])
def listar_personeros_centro(
    provincia: Optional[str] = Query(None),
    distrito: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user)
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    query = db.query(Usuarios).filter(Usuarios.rol == "PERSONERO_CENTRO")

    resultados = []
    usuarios = query.all()
    for user in usuarios:
        persona = db.query(Persona).filter(Persona.id_persona == user.idpersona).first()
        if persona:
            if provincia and persona.provincia != provincia:
                continue
            if distrito and persona.distrito != distrito:
                continue
            if distritos_filter is not None and persona.distrito not in distritos_filter:
                continue
        resultados.append(PersoneroCentroResponse(
            idusuario=user.idusuario,
            usuario=user.usuario,
            rol=user.rol,
            estado=user.estado,
            idpersona=user.idpersona,
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
            ) if persona else None
        ))

    return resultados


@router.get("/centros-sin-personero", response_model=List[CentroVotacionResponse])
def centros_sin_personero(
    provincia: Optional[str] = Query(None),
    distrito: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user)
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    subquery = (
        db.query(PersonerosUsers.id_centro_asignado)
        .filter(
            PersonerosUsers.estado_vinculacion == "ASIGNADO",
            PersonerosUsers.tipo_personero_solicitado == "CENTRO",
            PersonerosUsers.id_centro_asignado.isnot(None)
        )
        .distinct()
    )
    query = db.query(CentroVotacion).filter(~CentroVotacion.id_centro.in_(subquery))

    if provincia:
        query = query.filter(CentroVotacion.provincia == provincia)
    if distrito:
        query = query.filter(CentroVotacion.distrito == distrito)
    if distritos_filter is not None:
        query = query.filter(CentroVotacion.distrito.in_(distritos_filter))

    centros = query.all()

    return [
        CentroVotacionResponse(
            id_centro=c.id_centro,
            codigo_local=c.codigo_local,
            ubigeo=c.ubigeo,
            nombre_local=c.nombre_local,
            direccion_local=c.direccion_local,
            departamento=c.departamento,
            provincia=c.provincia,
            distrito=c.distrito
        )
        for c in centros
    ]


@router.get("/monitoreo/cobertura", response_model=CoberturaResponse)
def monitoreo_cobertura(
    provincia: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user)
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)
    return _calcular_cobertura(db, provincia, distritos_filter)


@router.get("/dashboard/desglose", response_model=List[DesgloseItem])
def dashboard_desglose(
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user)
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)
    return _calcular_desglose(db, distritos_filter)


@router.get("/solicitudes-pendientes", response_model=List[SolicitudPendienteResponse])
def solicitudes_pendientes(
    provincia: Optional[str] = Query(None),
    skip: int = Query(0, ge=0),
    limit: int = Query(50, ge=1, le=200),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user)
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    query = (
        db.query(PersonerosUsers, Persona)
        .join(Persona, PersonerosUsers.id_persona == Persona.id_persona)
        .filter(PersonerosUsers.estado_vinculacion == "SOLICITADO")
    )
    if provincia:
        query = query.filter(Persona.provincia == provincia)
    if distritos_filter is not None:
        query = query.filter(Persona.distrito.in_(distritos_filter))

    query = query.order_by(PersonerosUsers.fecha_registro.desc())
    resultados = query.offset(skip).limit(limit).all()

    solicitudes = []
    for pu, p in resultados:
        solicitudes.append(SolicitudPendienteResponse(
            id_solicitud=pu.id_solicitud,
            tipo_personero_solicitado=pu.tipo_personero_solicitado or "",
            estado_vinculacion=pu.estado_vinculacion,
            fecha_registro=pu.fecha_registro,
            id_persona=p.id_persona,
            persona=PersonaResponse(
                id_persona=p.id_persona,
                dni=p.dni,
                nombre=p.nombre,
                apellidos=p.apellidos,
                telefono1=p.telefono1,
                telefono2=p.telefono2,
                distrito=p.distrito,
                provincia=p.provincia,
                direccion=p.direccion
            )
        ))

    return solicitudes


@router.get("/reportes/excel")
def generar_reporte_excel(
    provincia: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user)
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    wb = Workbook()

    header_font = Font(bold=True, color="FFFFFF")
    header_fill = PatternFill(start_color="4472C4", end_color="4472C4", fill_type="solid")
    center_align = Alignment(horizontal="center", vertical="center")

    ws1 = wb.active
    ws1.title = "Cobertura"

    ws1.append(["REPORTE DE COBERTURA DE PERSONEROS"])
    ws1.merge_cells("A1:B1")
    ws1["A1"].font = Font(bold=True, size=14)
    ws1["A1"].alignment = center_align
    ws1.append([])
    ws1.append(["Indicador", "Valor"])
    for cell in ["A3", "B3"]:
        ws1[cell].font = header_font
        ws1[cell].fill = header_fill

    cobertura = _calcular_cobertura(db, provincia, distritos_filter)

    datos_cob = [
        ["Total Centros", cobertura.total_centros],
        ["Centros Asignados", cobertura.centros_asignados],
        ["Centros Sin Asignar", cobertura.centros_sin_asignar],
        ["% Centros", f"{cobertura.porcentaje_centros}%"],
        ["Total Mesas", cobertura.total_mesas],
        ["Mesas Asignadas", cobertura.mesas_asignadas],
        ["Mesas Sin Asignar", cobertura.mesas_sin_asignar],
        ["% Mesas", f"{cobertura.porcentaje_mesas}%"],
        ["Total Personeros Centro", cobertura.total_personeros_centro],
        ["Total Personeros Mesa", cobertura.total_personeros_mesa],
    ]
    for fila in datos_cob:
        ws1.append(fila)
    for col in ["A", "B"]:
        ws1.column_dimensions[col].width = 35

    ws2 = wb.create_sheet("Desglose")
    ws2.append(["Provincia", "Distrito", "Total Centros", "Centros Asignados", "Total Mesas", "Mesas Asignadas"])
    for cell in ["A1", "B1", "C1", "D1", "E1", "F1"]:
        ws2[cell].font = header_font
        ws2[cell].fill = header_fill
        ws2[cell].alignment = center_align

    desglose = _calcular_desglose(db, distritos_filter)
    for item in desglose:
        ws2.append([
            item.provincia,
            item.distrito,
            item.total_centros,
            item.centros_asignados,
            item.total_mesas,
            item.mesas_asignadas
        ])
    for col in ["A", "B", "C", "D", "E", "F"]:
        ws2.column_dimensions[col].width = 22

    ws3 = wb.create_sheet("Solicitudes")
    ws3.append(["ID Solicitud", "DNI", "Nombre", "Apellidos", "Teléfono 1", "Teléfono 2", "Provincia", "Distrito", "Tipo", "Estado", "Fecha Registro"])
    for cell in ["A1", "B1", "C1", "D1", "E1", "F1", "G1", "H1", "I1", "J1", "K1"]:
        ws3[cell].font = header_font
        ws3[cell].fill = header_fill
        ws3[cell].alignment = center_align

    query_sol = (
        db.query(PersonerosUsers, Persona)
        .join(Persona, PersonerosUsers.id_persona == Persona.id_persona)
        .order_by(PersonerosUsers.fecha_registro.desc())
    )
    if provincia:
        query_sol = query_sol.filter(Persona.provincia == provincia)
    if distritos_filter is not None:
        query_sol = query_sol.filter(Persona.distrito.in_(distritos_filter))
    sol_rows = query_sol.all()
    for pu, p in sol_rows:
        ws3.append([
            pu.id_solicitud,
            p.dni,
            p.nombre,
            p.apellidos,
            p.telefono1 or "",
            p.telefono2 or "",
            p.provincia or "",
            p.distrito or "",
            pu.tipo_personero_solicitado or "",
            pu.estado_vinculacion,
            pu.fecha_registro.strftime("%Y-%m-%d %H:%M:%S") if pu.fecha_registro else ""
        ])
    for col in ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"]:
        ws3.column_dimensions[col].width = 20

    ws4 = wb.create_sheet("Usuarios")
    ws4.append(["ID Usuario", "Usuario", "Rol", "Estado", "DNI", "Nombre", "Apellidos", "Teléfono 1", "Provincia", "Distrito", "Fecha Creado"])
    for cell in ["A1", "B1", "C1", "D1", "E1", "F1", "G1", "H1", "I1", "J1", "K1"]:
        ws4[cell].font = header_font
        ws4[cell].fill = header_fill
        ws4[cell].alignment = center_align

    query_usr = db.query(Usuarios).order_by(Usuarios.idusuario)
    usr_rows = query_usr.all()
    for u in usr_rows:
        p = db.query(Persona).filter(Persona.id_persona == u.idpersona).first()
        if distritos_filter is not None and p and p.distrito not in distritos_filter:
            continue
        ws4.append([
            u.idusuario,
            u.usuario,
            u.rol,
            "Activo" if u.estado else "Inactivo",
            p.dni if p else "",
            p.nombre if p else "",
            p.apellidos if p else "",
            p.telefono1 if p else "",
            p.provincia if p else "",
            p.distrito if p else "",
            u.fecha_creado.strftime("%Y-%m-%d %H:%M:%S") if u.fecha_creado else ""
        ])
    for col in ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K"]:
        ws4.column_dimensions[col].width = 20

    stream = io.BytesIO()
    wb.save(stream)
    stream.seek(0)

    filename = f"reporte_personeros_{datetime.now().strftime('%Y%m%d_%H%M%S')}.xlsx"

    return StreamingResponse(
        stream,
        media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        headers={"Content-Disposition": f"attachment; filename={filename}"}
    )


# ======================================================================
# NUEVOS ENDPOINTS - Busqueda centros, mesas disponibles, aprobar/rechazar, asignar
# ======================================================================

@router.get("/centros", response_model=List[CentroVotacionResponse])
def listar_centros(
    provincia: Optional[str] = Query(None),
    distrito: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)
    q = db.query(CentroVotacion)
    if provincia:
        q = q.filter(CentroVotacion.provincia == provincia)
    if distrito:
        q = q.filter(CentroVotacion.distrito == distrito)
    if distritos_filter is not None:
        q = q.filter(CentroVotacion.distrito.in_(distritos_filter))
    return [CentroVotacionResponse.model_validate(c) for c in q.order_by(CentroVotacion.provincia, CentroVotacion.distrito, CentroVotacion.nombre_local).all()]


@router.get("/centros/buscar", response_model=List[CentroVotacionResponse])
def buscar_centros(
    q: Optional[str] = Query("", alias="q"),
    provincia: Optional[str] = Query(None),
    distrito: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(RoleChecker(["SUPER_ADMIN", "ADMINISTRADOR", "PERSONERO_CENTRO"])),
):
    distritos_filter = _get_admin_distritos_filter(current_user, db) if hasattr(current_user, 'rol') else None
    query = db.query(CentroVotacion)
    if provincia:
        query = query.filter(CentroVotacion.provincia == provincia)
    if distrito:
        query = query.filter(CentroVotacion.distrito == distrito)
    if distritos_filter is not None:
        query = query.filter(CentroVotacion.distrito.in_(distritos_filter))
    if q:
        ql = f"%{q.lower()}%"
        from sqlalchemy import or_
        query = query.filter(or_(
            CentroVotacion.nombre_local.ilike(ql),
            CentroVotacion.direccion_local.ilike(ql),
            CentroVotacion.distrito.ilike(ql),
            CentroVotacion.provincia.ilike(ql),
            CentroVotacion.codigo_local.ilike(ql),
            CentroVotacion.ubigeo.ilike(ql),
        ))
    results = query.order_by(CentroVotacion.nombre_local).limit(80).all()
    return [CentroVotacionResponse.model_validate(c) for c in results]


@router.get("/mesas", response_model=List[MesaCentroResponse])
def listar_mesas(
    centroId: Optional[int] = Query(None),
    provincia: Optional[str] = Query(None),
    distrito: Optional[str] = Query(None),
    soloDisponibles: Optional[bool] = Query(False, alias="soloDisponibles"),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(RoleChecker(["SUPER_ADMIN", "ADMINISTRADOR", "PERSONERO_CENTRO"])),
):
    distritos_filter = _get_admin_distritos_filter(current_user, db) if hasattr(current_user, 'rol') and current_user.rol in ["SUPER_ADMIN", "ADMINISTRADOR"] else None
    q = db.query(Mesa)
    if centroId:
        cv = db.query(CentroVotacion).filter(CentroVotacion.id_centro == centroId).first()
        if cv:
            q = q.filter(Mesa.cod_local == cv.codigo_local)
    if provincia:
        q = q.filter(Mesa.provincia == provincia)
    if distrito:
        q = q.filter(Mesa.distrito == distrito)

    mesas_raw = q.order_by(Mesa.cod_local, Mesa.num_mesa).all()

    mesas = []
    if distritos_filter is not None:
        for m in mesas_raw:
            cv_distrito = None
            if m.cod_local:
                cv_m = db.query(CentroVotacion).filter(CentroVotacion.codigo_local == m.cod_local).first()
                if cv_m:
                    cv_distrito = cv_m.distrito
            d = (getattr(m, "distrito", None) or "") or (cv_distrito or "")
            if d and d in distritos_filter:
                mesas.append(m)
            elif not d:
                mesas.append(m)
    else:
        mesas = mesas_raw

    sub_asignadas = (
        db.query(PersonerosUsers.id_mesa_asignada)
        .filter(
            PersonerosUsers.estado_vinculacion == "ASIGNADO",
            PersonerosUsers.tipo_personero_solicitado == "MESA",
            PersonerosUsers.id_mesa_asignada.isnot(None),
        )
        .distinct()
        .subquery()
    )
    id_asignadas = {row[0] for row in db.query(sub_asignadas).all()}

    out = []
    for m in mesas:
        ocupada = m.id_mesa in id_asignadas
        estado = "OCUPADA" if ocupada else "LIBRE"
        if soloDisponibles and ocupada:
            continue
        cv = None
        if m.cod_local:
            cv = db.query(CentroVotacion).filter(CentroVotacion.codigo_local == m.cod_local).first()
        cod_local = (m.cod_local or "") or (cv.codigo_local if cv else "") or ""
        nombre_local = (getattr(m, "nombre_local", None) or "") or (cv.nombre_local if cv else "") or ""
        direccion_local = (getattr(m, "direccion_local", None) or "") or (cv.direccion_local if cv else "") or ""
        prov = (getattr(m, "provincia", None) or "") or (cv.provincia if cv else "") or ""
        dist = (getattr(m, "distrito", None) or "") or (cv.distrito if cv else "") or ""
        codi_ubigeo = (getattr(m, "codi_ubigeo", None) or "") or (getattr(cv, "codi_ubigeo", None) or getattr(cv, "ubigeo", "") or "") or ""
        personero_data = None
        if ocupada:
            pu = (
                db.query(PersonerosUsers, Persona, Usuarios)
                .outerjoin(Persona, PersonerosUsers.id_persona == Persona.id_persona)
                .outerjoin(Usuarios, Usuarios.idpersona == Persona.id_persona)
                .filter(
                    PersonerosUsers.id_mesa_asignada == m.id_mesa,
                    PersonerosUsers.estado_vinculacion == "ASIGNADO",
                )
                .first()
            )
            if pu:
                p_u, p_p, p_usuario = pu
                personero_data = {
                    "id_solicitud": p_u.id_solicitud,
                    "idusuario": p_usuario.idusuario if p_usuario else None,
                    "idusuario_id": p_usuario.idusuario if p_usuario else None,
                    "nombre": (p_p.nombre if p_p else "") or "",
                    "apellidos": (p_p.apellidos if p_p else "") or "",
                    "dni": (p_p.dni if p_p else "") or "",
                    "telefono1": (p_p.telefono1 if p_p else "") or "",
                }
        out.append(MesaCentroResponse(
            id_mesa=m.id_mesa,
            num_mesa=(m.num_mesa or "") or "",
            cod_local=cod_local,
            codi_ubigeo=codi_ubigeo,
            nombre_local=nombre_local,
            direccion_local=direccion_local,
            departamento="LORETO",
            provincia=prov,
            distrito=dist,
            estado=estado,
            personero=personero_data,
        ))
    return out


@router.post("/solicitudes/{id_solicitud}/aprobar")
def aprobar_solicitud(
    id_solicitud: int,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    pu = db.query(PersonerosUsers).filter(PersonerosUsers.id_solicitud == id_solicitud).first()
    if not pu:
        raise HTTPException(404, "Solicitud no encontrada")
    pu.estado_vinculacion = "ASIGNADO"
    from datetime import datetime
    pu.fecha_asignacion = datetime.utcnow()
    if not hasattr(pu, "id_usuario_asignador") and False:
        pass
    try:
        from sqlalchemy import inspect
        insp = inspect(PersonerosUsers)
        if "id_usuario_asignador" in [c.key for c in insp.mapper.column_attrs]:
            pu.id_usuario_asignador = current_user.idusuario
    except Exception:
        pass
    db.commit()
    return {"mensaje": "Solicitud aprobada", "id_solicitud": id_solicitud, "estado": pu.estado_vinculacion}


@router.post("/solicitudes/{id_solicitud}/rechazar")
def rechazar_solicitud(
    id_solicitud: int,
    observaciones: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    _: Usuarios = Depends(get_current_user),
):
    pu = db.query(PersonerosUsers).filter(PersonerosUsers.id_solicitud == id_solicitud).first()
    if not pu:
        raise HTTPException(404, "Solicitud no encontrada")
    pu.estado_vinculacion = "RECHAZADO"
    if observaciones:
        try:
            pu.observaciones = observaciones
        except Exception:
            pass
    db.commit()
    return {"mensaje": "Solicitud rechazada", "id_solicitud": id_solicitud, "estado": pu.estado_vinculacion}


@router.post("/asignaciones/personero-centro/asignar-centro")
def asignar_centro_a_personero(
    payload: dict,
    db: Session = Depends(get_db),
    _: Usuarios = Depends(RoleChecker(["SUPER_ADMIN", "ADMINISTRADOR"])),
):
    idusuario = payload.get("idusuario") or payload.get("id_usuario") or payload.get("idUsuario")
    id_centro = payload.get("id_centro") or payload.get("idCentro")
    id_solicitud = payload.get("id_solicitud") or payload.get("idSolicitud")
    if not idusuario and id_solicitud:
        pu = db.query(PersonerosUsers).filter(PersonerosUsers.id_solicitud == id_solicitud).first()
        if pu and pu.id_persona:
            u = db.query(Usuarios).filter(Usuarios.idpersona == pu.id_persona).first()
            if u:
                idusuario = u.idusuario
    if not id_centro or (not idusuario and not id_solicitud):
        raise HTTPException(400, "id_centro es requerido, junto con idusuario o id_solicitud")
    user = None
    if idusuario:
        user = db.query(Usuarios).filter(Usuarios.idusuario == idusuario).first()
        if not user:
            raise HTTPException(404, "Usuario no encontrado")
    cv = db.query(CentroVotacion).filter(CentroVotacion.id_centro == id_centro).first()
    if not cv:
        raise HTTPException(404, "Centro de votacion no encontrado")

    if user:
        try:
            from sqlalchemy import inspect as _insp
            cols_usr = {c.key for c in _insp(Usuarios).mapper.column_attrs}
            if "id_centro_asignado" in cols_usr:
                user.id_centro_asignado = id_centro
        except Exception:
            pass

    pu = None
    if id_solicitud:
        pu = db.query(PersonerosUsers).filter(PersonerosUsers.id_solicitud == id_solicitud).first()
    if not pu and user and user.idpersona:
        pu = (
            db.query(PersonerosUsers)
            .filter(PersonerosUsers.id_persona == user.idpersona, PersonerosUsers.tipo_personero_solicitado == "CENTRO")
            .first()
        )
    if not pu and user and user.idpersona:
        pu = PersonerosUsers(id_persona=user.idpersona, tipo_personero_solicitado="CENTRO")
        db.add(pu); db.flush()
    if pu:
        pu.estado_vinculacion = "ASIGNADO"
        pu.id_centro_asignado = id_centro
        from datetime import datetime
        pu.fecha_asignacion = datetime.utcnow()

    db.commit()
    persona_nombre = ""
    if pu and pu.id_persona:
        p = db.query(Persona).filter(Persona.id_persona == pu.id_persona).first()
        if p:
            persona_nombre = f"{p.nombre} {p.apellidos}".strip()
    return {"mensaje": "Centro asignado exitosamente", "idusuario": idusuario, "id_centro": id_centro,
            "centro_nombre": cv.nombre_local, "personero_nombre": persona_nombre, "id_solicitud": id_solicitud}


@router.post("/asignaciones/personero-mesa/asignar-mesa")
def asignar_mesa_a_personero(
    payload: dict,
    db: Session = Depends(get_db),
    _: Usuarios = Depends(RoleChecker(["SUPER_ADMIN", "ADMINISTRADOR", "PERSONERO_CENTRO"])),
):
    import traceback as _tb
    try:
        idusuario = payload.get("idusuario") or payload.get("id_usuario") or payload.get("idUsuario")
        id_mesa = payload.get("id_mesa") or payload.get("idMesa")
        id_solicitud = payload.get("id_solicitud") or payload.get("idSolicitud")
        id_centro = payload.get("id_centro") or payload.get("idCentro")
        if not idusuario and id_solicitud:
            pu = db.query(PersonerosUsers).filter(PersonerosUsers.id_solicitud == id_solicitud).first()
            if pu and pu.id_persona:
                u = db.query(Usuarios).filter(Usuarios.idpersona == pu.id_persona).first()
                if u:
                    idusuario = u.idusuario
        if not id_mesa or (not idusuario and not id_solicitud):
            raise HTTPException(400, "id_mesa es requerido, junto con idusuario o id_solicitud")
        user = None
        if idusuario:
            user = db.query(Usuarios).filter(Usuarios.idusuario == idusuario).first()
            if not user:
                raise HTTPException(404, "Usuario no encontrado")
        mesa = db.query(Mesa).filter(Mesa.id_mesa == id_mesa).first()
        if not mesa:
            raise HTTPException(404, "Mesa no encontrada")

        mesa_cv = db.query(CentroVotacion).filter(CentroVotacion.codigo_local == mesa.cod_local).first()
        if not id_centro and mesa_cv:
            id_centro = mesa_cv.id_centro

        exist_asig = (
            db.query(PersonerosUsers)
            .filter(
                PersonerosUsers.id_mesa_asignada == id_mesa,
                PersonerosUsers.estado_vinculacion == "ASIGNADO",
                PersonerosUsers.tipo_personero_solicitado == "MESA",
            )
            .first()
        )
        if exist_asig and exist_asig.id_solicitud != id_solicitud:
            raise HTTPException(400, "Esta mesa ya se encuentra asignada a otro personero")

        if user:
            try:
                from sqlalchemy import inspect as _insp2
                cols_usr = {c.key for c in _insp2(Usuarios).mapper.column_attrs}
                if "id_mesa_asignada" in cols_usr:
                    user.id_mesa_asignada = id_mesa
            except Exception:
                pass

        pu = None
        if id_solicitud:
            pu = db.query(PersonerosUsers).filter(PersonerosUsers.id_solicitud == id_solicitud).first()
        if not pu and user and user.idpersona:
            pu = (
                db.query(PersonerosUsers)
                .filter(PersonerosUsers.id_persona == user.idpersona, PersonerosUsers.tipo_personero_solicitado == "MESA")
                .first()
            )
        if not pu and user and user.idpersona:
            pu = PersonerosUsers(id_persona=user.idpersona, tipo_personero_solicitado="MESA")
            db.add(pu); db.flush()
        if pu:
            pu.estado_vinculacion = "ASIGNADO"
            pu.id_mesa_asignada = id_mesa
            from datetime import datetime
            pu.fecha_asignacion = datetime.utcnow()

        db.commit()
        persona_nombre = ""
        if pu and pu.id_persona:
            p = db.query(Persona).filter(Persona.id_persona == pu.id_persona).first()
            if p:
                persona_nombre = f"{p.nombre} {p.apellidos}".strip()
        return {"mensaje": "Mesa asignada exitosamente", "idusuario": idusuario, "id_mesa": id_mesa,
                "num_mesa": mesa.num_mesa, "personero_nombre": persona_nombre, "id_solicitud": id_solicitud}
    except HTTPException:
        raise
    except Exception as _e:
        db.rollback()
        err = _tb.format_exc()
        print("ERROR asignar-mesa:", err)
        raise HTTPException(status_code=500, detail=f"{type(_e).__name__}: {_e}")


def _serialize_usuario_admin(user: Usuarios, persona: Persona | None = None) -> UsuarioWithPersonaResponse:
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


def _usuario_persona_distrito(user: Usuarios, db: Session) -> str | None:
    p = db.query(Persona).filter(Persona.id_persona == user.idpersona).first()
    return p.distrito if p else None


def _assert_jurisdiccion(user_jurisdiccion_distrito: str | None, distritos_filter: List[str] | None, action: str):
    if distritos_filter is None:
        return
    if user_jurisdiccion_distrito is None:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=f"No tiene permiso para {action} este usuario (no se detectó su distrito)"
        )
    if user_jurisdiccion_distrito not in distritos_filter:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail=(
                f"No tiene permiso para {action} este usuario: su distrito '{user_jurisdiccion_distrito}' "
                f"no pertenece a su jurisdicción asignada ({distritos_filter})"
            )
        )


@router.get("/usuarios", response_model=List[UsuarioWithPersonaResponse])
def admin_listar_usuarios(
    rol: Optional[str] = Query(None),
    estado: Optional[bool] = Query(None),
    busqueda: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    query = (
        db.query(Usuarios)
        .join(Persona, Usuarios.idpersona == Persona.id_persona, isouter=True)
    )

    if distritos_filter is not None and len(distritos_filter) > 0:
        query = query.filter(Persona.distrito.in_(distritos_filter))

    if rol:
        query = query.filter(Usuarios.rol == rol)
    if estado is not None:
        query = query.filter(Usuarios.estado == estado)
    if busqueda:
        busqueda_like = f"%{busqueda}%"
        query = query.filter(
            Usuarios.usuario.ilike(busqueda_like) |
            Persona.nombre.ilike(busqueda_like) |
            Persona.apellidos.ilike(busqueda_like) |
            Persona.dni.ilike(busqueda_like)
        )

    usuarios = query.order_by(Usuarios.idusuario.desc()).all()
    resultados = []
    for user in usuarios:
        p = db.query(Persona).filter(Persona.id_persona == user.idpersona).first()
        resultados.append(_serialize_usuario_admin(user, p))
    return resultados


@router.post("/usuarios", status_code=status.HTTP_201_CREATED, response_model=UsuarioWithPersonaResponse)
def admin_crear_usuario(
    request: UsuarioCreateRequest,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)
    _assert_jurisdiccion(request.distrito, distritos_filter, "crear")

    usuario_existente = db.query(Usuarios).filter(Usuarios.usuario == request.usuario).first()
    if usuario_existente:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "El nombre de usuario ya está en uso")

    persona_existente = db.query(Persona).filter(Persona.dni == request.dni).first()
    if persona_existente:
        raise HTTPException(status.HTTP_400_BAD_REQUEST, "Ya existe una persona con este DNI")

    persona = Persona(
        dni=request.dni,
        nombre=request.nombre,
        apellidos=request.apellidos,
        telefono1=request.telefono1,
        telefono2=request.telefono2,
        correo=getattr(request, "correo", None),
        distrito=request.distrito,
        provincia=request.provincia,
        direccion=request.direccion,
    )
    db.add(persona)
    db.flush()

    nuevo_usuario = Usuarios(
        idpersona=persona.id_persona,
        usuario=request.usuario,
        clave=get_password_hash(request.clave),
        rol=request.rol,
        estado=True,
    )
    db.add(nuevo_usuario)
    db.commit()
    db.refresh(nuevo_usuario)
    db.refresh(persona)
    return _serialize_usuario_admin(nuevo_usuario, persona)


@router.put("/usuarios/{idusuario}", response_model=UsuarioWithPersonaResponse)
def admin_actualizar_usuario(
    idusuario: int,
    request: UsuarioUpdateRequest,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    usuario = db.query(Usuarios).filter(Usuarios.idusuario == idusuario).first()
    if not usuario:
        raise HTTPException(status.HTTP_404_NOT_FOUND, "Usuario no encontrado")

    distrito_actual = _usuario_persona_distrito(usuario, db)
    _assert_jurisdiccion(distrito_actual, distritos_filter, "editar")

    distrito_nuevo = request.distrito if request.distrito is not None else distrito_actual
    if distrito_nuevo != distrito_actual:
        _assert_jurisdiccion(distrito_nuevo, distritos_filter, "editar")

    if request.usuario and request.usuario != usuario.usuario:
        usuario_existente = db.query(Usuarios).filter(Usuarios.usuario == request.usuario).first()
        if usuario_existente:
            raise HTTPException(status.HTTP_400_BAD_REQUEST, "El nombre de usuario ya está en uso")
        usuario.usuario = request.usuario

    if request.clave:
        usuario.clave = get_password_hash(request.clave)

    if request.rol:
        usuario.rol = request.rol

    persona = db.query(Persona).filter(Persona.id_persona == usuario.idpersona).first()
    if persona:
        if request.dni is not None:
            if request.dni != persona.dni:
                d = db.query(Persona).filter(Persona.dni == request.dni).first()
                if d:
                    raise HTTPException(status.HTTP_400_BAD_REQUEST, "Ya existe una persona con este DNI")
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
    return _serialize_usuario_admin(usuario, persona)
