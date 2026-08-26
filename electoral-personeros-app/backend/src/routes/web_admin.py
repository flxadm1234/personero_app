from typing import Optional, List, Any, Dict
from datetime import datetime
import logging
from io import BytesIO

from fastapi import APIRouter, Depends, HTTPException, status, Query, UploadFile, File
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session, joinedload
from sqlalchemy import func, or_, and_, text, String
from openpyxl import Workbook
from openpyxl.styles import Font, Alignment, PatternFill
from openpyxl.utils import get_column_letter

from src.config.database import get_db
from src.middlewares.auth import (
    RoleChecker,
    get_password_hash,
    get_current_user,
)
from src.models.persona import Persona
from src.models.usuario import Usuarios
from src.models.usuarios_distritos import UsuariosDistritos
from src.models.centro_votacion import CentroVotacion
from src.models.mesa import Mesa
from src.models.personeros_users import PersonerosUsers

logger = logging.getLogger("uvicorn.error")

router = APIRouter(
    prefix="/api/admin-web",
    tags=["Web Admin Panel"],
)

MODELS_MAP = {
    "persona": Persona,
    "usuarios": Usuarios,
    "usuarios_distritos": UsuariosDistritos,
    "centro_votacion": CentroVotacion,
    "mesa": Mesa,
    "personeros_users": PersonerosUsers,
}

MODEL_PK = {
    "persona": "id_persona",
    "usuarios": "idusuario",
    "usuarios_distritos": "id",
    "centro_votacion": "id_centro",
    "mesa": "id_mesa",
    "personeros_users": "id_solicitud",
}

ADMIN_ROLE_CHECKER = RoleChecker(["SUPER_ADMIN", "ADMINISTRADOR"])


def _get_admin_distritos_filter(current_user: Usuarios, db: Session) -> Optional[List[str]]:
    if current_user.rol == "SUPER_ADMIN":
        return None
    if current_user.rol == "ADMINISTRADOR":
        qs = db.query(UsuariosDistritos).filter(UsuariosDistritos.idusuario == current_user.idusuario).all()
        return [ud.distrito for ud in qs if ud.distrito]
    return []


def _assert_jurisdiccion(distrito: Optional[str], distritos_filter: Optional[List[str]], accion: str):
    if distritos_filter is None:
        return
    if not distrito:
        raise HTTPException(
            status_code=403,
            detail=f"No tiene permiso para {accion}: elemento sin distrito y usted tiene jurisdicción limitada."
        )
    if distrito not in distritos_filter:
        raise HTTPException(
            status_code=403,
            detail=(
                f"No tiene permiso para {accion} este elemento. "
                f"Su jurisdicción cubre los distritos: {distritos_filter}. "
                f"El elemento pertenece al distrito: '{distrito}'."
            )
        )


def _extract_distrito_from_obj(tabla: str, obj: Any, db: Session) -> Optional[str]:
    if tabla == "persona":
        return getattr(obj, "distrito", None)
    if tabla == "usuarios":
        p = db.query(Persona).filter(Persona.id_persona == obj.idpersona).first()
        return getattr(p, "distrito", None) if p else None
    if tabla == "usuarios_distritos":
        return getattr(obj, "distrito", None)
    if tabla == "centro_votacion":
        return getattr(obj, "distrito", None)
    if tabla == "mesa":
        return getattr(obj, "distrito", None)
    if tabla == "personeros_users":
        if obj.id_persona:
            p = db.query(Persona).filter(Persona.id_persona == obj.id_persona).first()
            return getattr(p, "distrito", None) if p else None
        if obj.id_centro_asignado:
            c = db.query(CentroVotacion).filter(CentroVotacion.id_centro == obj.id_centro_asignado).first()
            return getattr(c, "distrito", None) if c else None
        if obj.id_mesa_asignada:
            m = db.query(Mesa).filter(Mesa.id_mesa == obj.id_mesa_asignada).first()
            return getattr(m, "distrito", None) if m else None
    return None


def _aplicar_filtro_distrital_query(query, tabla: str, distritos_filter: Optional[List[str]]):
    if distritos_filter is None:
        return query
    if tabla == "persona":
        return query.filter(Persona.distrito.in_(distritos_filter))
    if tabla == "usuarios":
        return query.filter(
            Usuarios.idpersona.in_(
                db_query := Persona.__table__.select().where(Persona.distrito.in_(distritos_filter)).with_only_columns(Persona.id_persona)
            )
        )
    if tabla == "usuarios_distritos":
        return query.filter(UsuariosDistritos.distrito.in_(distritos_filter))
    if tabla == "centro_votacion":
        return query.filter(CentroVotacion.distrito.in_(distritos_filter))
    if tabla == "mesa":
        return query.filter(Mesa.distrito.in_(distritos_filter))
    if tabla == "personeros_users":
        return query.filter(
            or_(
                PersonerosUsers.id_persona.in_(
                    Persona.__table__.select().where(Persona.distrito.in_(distritos_filter)).with_only_columns(Persona.id_persona)
                ),
                PersonerosUsers.id_centro_asignado.in_(
                    CentroVotacion.__table__.select().where(CentroVotacion.distrito.in_(distritos_filter)).with_only_columns(CentroVotacion.id_centro)
                ),
                PersonerosUsers.id_mesa_asignada.in_(
                    Mesa.__table__.select().where(Mesa.distrito.in_(distritos_filter)).with_only_columns(Mesa.id_mesa)
                ),
            )
        )
    return query


def _model_to_dict(obj: Any, exclude: List[str] = None) -> Dict[str, Any]:
    exclude = exclude or []
    data = {}
    for c in obj.__table__.columns:
        if c.name in exclude:
            continue
        val = getattr(obj, c.name)
        if isinstance(val, datetime):
            data[c.name] = val.isoformat()
        else:
            data[c.name] = val
    return data


def _serialize_usuarios_con_persona(user: Usuarios, persona: Optional[Persona] = None) -> Dict[str, Any]:
    data = _model_to_dict(user, exclude=["clave"])
    if persona is None:
        data["persona"] = None
    else:
        data["persona"] = _model_to_dict(persona)
    return data


def _serialize_usuarios_distritos(ud: UsuariosDistritos, usuario: Optional[Usuarios] = None) -> Dict[str, Any]:
    data = _model_to_dict(ud)
    if usuario is None:
        data["nombre_usuario"] = None
    else:
        data["nombre_usuario"] = usuario.usuario
        data["idusuario_ref"] = usuario.idusuario
    return data


def _serialize_personeros_users(pu: PersonerosUsers, persona: Optional[Persona] = None,
                                centro: Optional[CentroVotacion] = None,
                                mesa: Optional[Mesa] = None) -> Dict[str, Any]:
    data = _model_to_dict(pu)
    data["persona"] = _model_to_dict(persona) if persona else None
    data["centro_votacion"] = _model_to_dict(centro) if centro else None
    data["mesa"] = _model_to_dict(mesa) if mesa else None
    return data


# ============================================================
# A) DASHBOARD STATS
# ============================================================
@router.get("/dashboard/stats")
def dashboard_stats(
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    q_persona = db.query(Persona)
    q_usuarios = db.query(Usuarios)
    q_centros = db.query(CentroVotacion)
    q_mesas = db.query(Mesa)
    q_personeros = db.query(PersonerosUsers)

    if distritos_filter is not None:
        q_persona = q_persona.filter(Persona.distrito.in_(distritos_filter))
        q_usuarios = q_usuarios.filter(
            Usuarios.idpersona.in_(
                Persona.__table__.select().where(Persona.distrito.in_(distritos_filter)).with_only_columns(Persona.id_persona)
            )
        )
        q_centros = q_centros.filter(CentroVotacion.distrito.in_(distritos_filter))
        q_mesas = q_mesas.filter(Mesa.distrito.in_(distritos_filter))
        q_personeros = q_personeros.filter(
            or_(
                PersonerosUsers.id_persona.in_(
                    Persona.__table__.select().where(Persona.distrito.in_(distritos_filter)).with_only_columns(Persona.id_persona)
                ),
                PersonerosUsers.id_centro_asignado.in_(
                    CentroVotacion.__table__.select().where(CentroVotacion.distrito.in_(distritos_filter)).with_only_columns(CentroVotacion.id_centro)
                ),
                PersonerosUsers.id_mesa_asignada.in_(
                    Mesa.__table__.select().where(Mesa.distrito.in_(distritos_filter)).with_only_columns(Mesa.id_mesa)
                ),
            )
        )

    total_personas = q_persona.count()
    total_usuarios = q_usuarios.count()
    total_centros = q_centros.count()
    total_mesas = q_mesas.count()
    total_personeros_asignados = q_personeros.filter(PersonerosUsers.estado_vinculacion == "ASIGNADO").count()
    total_solicitudes_pendientes = q_personeros.filter(PersonerosUsers.estado_vinculacion == "SOLICITADO").count()

    por_rol = []
    rol_rows = q_usuarios.with_entities(Usuarios.rol, func.count(Usuarios.idusuario)).group_by(Usuarios.rol).all()
    for rol, cnt in rol_rows:
        por_rol.append({"rol": rol, "count": cnt})

    mesas_subq = q_mesas.with_entities(Mesa.provincia, Mesa.id_mesa).subquery()
    asignadas_subq = q_personeros.filter(
        PersonerosUsers.estado_vinculacion == "ASIGNADO",
        PersonerosUsers.id_mesa_asignada.isnot(None)
    ).with_entities(PersonerosUsers.id_mesa_asignada).distinct().subquery()

    provincia_query = db.query(
        mesas_subq.c.provincia,
        func.count(mesas_subq.c.id_mesa).label("total"),
        func.count(asignadas_subq.c.id_mesa_asignada).label("asignadas")
    ).outerjoin(asignadas_subq, asignadas_subq.c.id_mesa_asignada == mesas_subq.c.id_mesa) \
        .group_by(mesas_subq.c.provincia).all()

    mesas_por_provincia = []
    for provincia, total, asignadas in provincia_query:
        prov = provincia or "SIN PROVINCIA"
        mesas_por_provincia.append({
            "provincia": prov,
            "mesas_libres": max(total - asignadas, 0),
            "mesas_asignadas": asignadas,
            "total": total
        })

    centros_por_distrito = []
    distrito_rows = q_centros.with_entities(CentroVotacion.distrito, func.count(CentroVotacion.id_centro)) \
        .group_by(CentroVotacion.distrito).all()
    for dist, cnt in distrito_rows:
        centros_por_distrito.append({"distrito": dist or "SIN DISTRITO", "count": cnt})

    solicitudes_estado = []
    estado_rows = q_personeros.with_entities(PersonerosUsers.estado_vinculacion, func.count(PersonerosUsers.id_solicitud)) \
        .group_by(PersonerosUsers.estado_vinculacion).all()
    for est, cnt in estado_rows:
        solicitudes_estado.append({"estado": est or "DESCONOCIDO", "count": cnt})

    solicitudes_timeline = []
    fecha_rows = q_personeros.with_entities(
        func.date(PersonerosUsers.fecha_registro).label("fecha"),
        func.count(PersonerosUsers.id_solicitud).label("count")
    ).group_by(func.date(PersonerosUsers.fecha_registro)).order_by(func.date(PersonerosUsers.fecha_registro)).all()
    for fecha, cnt in fecha_rows:
        solicitudes_timeline.append({"fecha": fecha.isoformat() if fecha else None, "count": cnt})

    return {
        "totales": {
            "personas": total_personas,
            "usuarios": total_usuarios,
            "centros": total_centros,
            "mesas": total_mesas,
            "personeros_asignados": total_personeros_asignados,
            "solicitudes_pendientes": total_solicitudes_pendientes,
        },
        "por_rol": por_rol,
        "mesas_por_provincia": mesas_por_provincia,
        "centros_por_distrito": centros_por_distrito,
        "solicitudes_estado": solicitudes_estado,
        "solicitudes_timeline": solicitudes_timeline,
    }


# ============================================================
# B) CRUD GENERICO 7 TABLAS
# ============================================================
def _validar_tabla(tabla: str):
    if tabla not in MODELS_MAP:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Tabla no permitida: '{tabla}'. Válidas: {list(MODELS_MAP.keys())}"
        )


@router.get("/tables/{tabla}")
def listar_tabla(
    tabla: str,
    skip: int = Query(0, ge=0),
    limit: int = Query(100, ge=1, le=1000),
    search: Optional[str] = Query(None),
    filter_col: Optional[str] = Query(None),
    filter_val: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(ADMIN_ROLE_CHECKER),
):
    _validar_tabla(tabla)
    Model = MODELS_MAP[tabla]
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    query = db.query(Model)
    query = _aplicar_filtro_distrital_query(query, tabla, distritos_filter)

    if filter_col and filter_val:
        try:
            col = getattr(Model, filter_col)
            query = query.filter(col == filter_val)
        except AttributeError:
            raise HTTPException(status_code=400, detail=f"Columna '{filter_col}' no existe en {tabla}")

    if search:
        search_term = f"%{search.lower()}%"
        or_filters = []
        for c in Model.__table__.columns:
            try:
                col = getattr(Model, c.name)
                col_type = str(c.type).upper()
                is_text = ("VARCHAR" in col_type or "TEXT" in col_type or "CHAR" in col_type or "CITEXT" in col_type)
                if is_text:
                    or_filters.append(col.ilike(search_term))
                else:
                    try:
                        or_filters.append(func.cast(col, String).ilike(search_term))
                    except Exception:
                        continue
            except Exception:
                continue
        if or_filters:
            query = query.filter(or_(*or_filters))

    pk_name = MODEL_PK[tabla]
    pk_col = getattr(Model, pk_name)
    subq_count = query.with_entities(pk_col).subquery()
    total = db.query(func.count()).select_from(subq_count).scalar() or 0
    rows = query.offset(skip).limit(limit).all()
    resultados = []

    for obj in rows:
        if tabla == "usuarios":
            p = db.query(Persona).filter(Persona.id_persona == obj.idpersona).first()
            resultados.append(_serialize_usuarios_con_persona(obj, p))
        elif tabla == "usuarios_distritos":
            u = db.query(Usuarios).filter(Usuarios.idusuario == obj.idusuario).first()
            resultados.append(_serialize_usuarios_distritos(obj, u))
        elif tabla == "personeros_users":
            p = db.query(Persona).filter(Persona.id_persona == obj.id_persona).first() if obj.id_persona else None
            c = db.query(CentroVotacion).filter(CentroVotacion.id_centro == obj.id_centro_asignado).first() if obj.id_centro_asignado else None
            m = db.query(Mesa).filter(Mesa.id_mesa == obj.id_mesa_asignada).first() if obj.id_mesa_asignada else None
            resultados.append(_serialize_personeros_users(obj, p, c, m))
        else:
            resultados.append(_model_to_dict(obj))

    return {"total": total, "skip": skip, "limit": limit, "data": resultados}


@router.get("/tables/{tabla}/{id}")
def obtener_tabla(
    tabla: str,
    id: int,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(ADMIN_ROLE_CHECKER),
):
    _validar_tabla(tabla)
    Model = MODELS_MAP[tabla]
    pk_col = MODEL_PK[tabla]

    obj = db.query(Model).filter(getattr(Model, pk_col) == id).first()
    if not obj:
        raise HTTPException(status_code=404, detail=f"Registro {id} no encontrado en {tabla}")

    distritos_filter = _get_admin_distritos_filter(current_user, db)
    distrito = _extract_distrito_from_obj(tabla, obj, db)
    _assert_jurisdiccion(distrito, distritos_filter, "ver")

    if tabla == "usuarios":
        p = db.query(Persona).filter(Persona.id_persona == obj.idpersona).first()
        return _serialize_usuarios_con_persona(obj, p)
    elif tabla == "usuarios_distritos":
        u = db.query(Usuarios).filter(Usuarios.idusuario == obj.idusuario).first()
        return _serialize_usuarios_distritos(obj, u)
    elif tabla == "personeros_users":
        p = db.query(Persona).filter(Persona.id_persona == obj.id_persona).first() if obj.id_persona else None
        c = db.query(CentroVotacion).filter(CentroVotacion.id_centro == obj.id_centro_asignado).first() if obj.id_centro_asignado else None
        m = db.query(Mesa).filter(Mesa.id_mesa == obj.id_mesa_asignada).first() if obj.id_mesa_asignada else None
        return _serialize_personeros_users(obj, p, c, m)
    return _model_to_dict(obj)


@router.post("/tables/{tabla}", status_code=201)
def crear_tabla(
    tabla: str,
    body: Dict[str, Any],
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(ADMIN_ROLE_CHECKER),
):
    _validar_tabla(tabla)
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    try:
        if tabla == "usuarios":
            idpersona = body.get("idpersona")
            usuario_val = body.get("usuario")
            password = body.get("password")
            rol = body.get("rol")
            estado = body.get("estado", True)

            if not all([idpersona, usuario_val, password, rol]):
                raise HTTPException(status_code=400, detail="Campos requeridos: idpersona, usuario, password, rol")

            if db.query(Usuarios).filter(Usuarios.usuario == usuario_val).first():
                raise HTTPException(status_code=400, detail="El nombre de usuario ya está en uso (UNIQUE violation)")

            p = db.query(Persona).filter(Persona.id_persona == idpersona).first()
            if not p:
                raise HTTPException(status_code=404, detail="Persona no encontrada")

            distrito = p.distrito
            _assert_jurisdiccion(distrito, distritos_filter, "crear usuario en")

            obj = Usuarios(
                idpersona=idpersona,
                usuario=usuario_val,
                clave=get_password_hash(password),
                rol=rol,
                estado=estado,
            )
            db.add(obj)
            db.commit()
            db.refresh(obj)
            return _serialize_usuarios_con_persona(obj, p)

        elif tabla == "persona":
            dni = body.get("dni")
            if dni and db.query(Persona).filter(Persona.dni == dni).first():
                raise HTTPException(status_code=400, detail="DNI ya registrado (UNIQUE violation)")

            distrito = body.get("distrito")
            _assert_jurisdiccion(distrito, distritos_filter, "crear persona en")

            obj = Persona()
            for k, v in body.items():
                if hasattr(obj, k):
                    setattr(obj, k, v)
            db.add(obj)
            db.commit()
            db.refresh(obj)
            return _model_to_dict(obj)

        elif tabla == "usuarios_distritos":
            distrito = body.get("distrito")
            _assert_jurisdiccion(distrito, distritos_filter, "asignar distrito en")
            Model = MODELS_MAP[tabla]
            obj = Model()
            for k, v in body.items():
                if hasattr(obj, k):
                    setattr(obj, k, v)
            try:
                db.add(obj)
                db.commit()
                db.refresh(obj)
            except Exception as e:
                db.rollback()
                if "uq_usuario_distrito" in str(e).lower() or "unique" in str(e).lower():
                    raise HTTPException(status_code=400, detail=f"Duplicado en usuarios_distritos (UNIQUE violation): {e}")
                raise HTTPException(status_code=400, detail=str(e))
            u = db.query(Usuarios).filter(Usuarios.idusuario == obj.idusuario).first()
            return _serialize_usuarios_distritos(obj, u)

        elif tabla == "centro_votacion":
            distrito = body.get("distrito")
            _assert_jurisdiccion(distrito, distritos_filter, "crear centro en")
            codigo_local = body.get("codigo_local")
            if codigo_local and db.query(CentroVotacion).filter(CentroVotacion.codigo_local == codigo_local).first():
                raise HTTPException(status_code=400, detail="Código local ya existe (UNIQUE violation)")
            Model = MODELS_MAP[tabla]
            obj = Model()
            for k, v in body.items():
                if hasattr(obj, k):
                    setattr(obj, k, v)
            db.add(obj)
            db.commit()
            db.refresh(obj)
            return _model_to_dict(obj)

        elif tabla == "mesa":
            distrito = body.get("distrito")
            _assert_jurisdiccion(distrito, distritos_filter, "crear mesa en")
            num_mesa = body.get("num_mesa")
            if num_mesa and db.query(Mesa).filter(Mesa.num_mesa == num_mesa).first():
                raise HTTPException(status_code=400, detail="Número de mesa ya existe (UNIQUE violation)")
            Model = MODELS_MAP[tabla]
            obj = Model()
            for k, v in body.items():
                if hasattr(obj, k):
                    setattr(obj, k, v)
            db.add(obj)
            db.commit()
            db.refresh(obj)
            return _model_to_dict(obj)

        elif tabla == "personeros_users":
            distrito = None
            id_persona = body.get("id_persona")
            if id_persona:
                p = db.query(Persona).filter(Persona.id_persona == id_persona).first()
                if p:
                    distrito = p.distrito
            if not distrito:
                id_centro = body.get("id_centro_asignado")
                if id_centro:
                    c = db.query(CentroVotacion).filter(CentroVotacion.id_centro == id_centro).first()
                    if c:
                        distrito = c.distrito
            if not distrito:
                id_mesa = body.get("id_mesa_asignada")
                if id_mesa:
                    m = db.query(Mesa).filter(Mesa.id_mesa == id_mesa).first()
                    if m:
                        distrito = m.distrito
            _assert_jurisdiccion(distrito, distritos_filter, "crear solicitud en")

            Model = MODELS_MAP[tabla]
            obj = Model()
            for k, v in body.items():
                if hasattr(obj, k):
                    setattr(obj, k, v)
            db.add(obj)
            db.commit()
            db.refresh(obj)
            p = db.query(Persona).filter(Persona.id_persona == obj.id_persona).first() if obj.id_persona else None
            c = db.query(CentroVotacion).filter(CentroVotacion.id_centro == obj.id_centro_asignado).first() if obj.id_centro_asignado else None
            m = db.query(Mesa).filter(Mesa.id_mesa == obj.id_mesa_asignada).first() if obj.id_mesa_asignada else None
            return _serialize_personeros_users(obj, p, c, m)

        else:
            Model = MODELS_MAP[tabla]
            obj = Model()
            for k, v in body.items():
                if hasattr(obj, k):
                    setattr(obj, k, v)
            db.add(obj)
            db.commit()
            db.refresh(obj)
            return _model_to_dict(obj)

    except HTTPException:
        raise
    except Exception as e:
        db.rollback()
        logger.error("Error creando registro en tabla %s: %s", tabla, e)
        if "unique" in str(e).lower() or "uq_" in str(e).lower():
            raise HTTPException(status_code=400, detail=f"Violación UNIQUE en {tabla}: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))


@router.put("/tables/{tabla}/{id}")
def actualizar_tabla(
    tabla: str,
    id: int,
    body: Dict[str, Any],
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(ADMIN_ROLE_CHECKER),
):
    _validar_tabla(tabla)
    Model = MODELS_MAP[tabla]
    pk_col = MODEL_PK[tabla]
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    obj = db.query(Model).filter(getattr(Model, pk_col) == id).first()
    if not obj:
        raise HTTPException(status_code=404, detail=f"Registro {id} no encontrado en {tabla}")

    distrito_actual = _extract_distrito_from_obj(tabla, obj, db)
    _assert_jurisdiccion(distrito_actual, distritos_filter, "editar")

    try:
        if tabla == "usuarios":
            new_usuario = body.get("usuario")
            new_password = body.get("password")
            new_rol = body.get("rol")
            new_estado = body.get("estado")
            new_idpersona = body.get("idpersona")

            if new_idpersona and new_idpersona != obj.idpersona:
                p_new = db.query(Persona).filter(Persona.id_persona == new_idpersona).first()
                if not p_new:
                    raise HTTPException(status_code=404, detail="Persona nueva no encontrada")
                nuevo_distrito = p_new.distrito
                _assert_jurisdiccion(nuevo_distrito, distritos_filter, "cambiar idpersona a")
                obj.idpersona = new_idpersona

            p_actual = db.query(Persona).filter(Persona.id_persona == obj.idpersona).first()
            if p_actual and distrito_actual != p_actual.distrito:
                _assert_jurisdiccion(p_actual.distrito, distritos_filter, "editar usuario de")

            if new_usuario and new_usuario != obj.usuario:
                if db.query(Usuarios).filter(Usuarios.usuario == new_usuario).first():
                    raise HTTPException(status_code=400, detail="El nombre de usuario ya está en uso (UNIQUE violation)")
                obj.usuario = new_usuario
            if new_password:
                obj.clave = get_password_hash(new_password)
            if new_rol:
                obj.rol = new_rol
            if new_estado is not None:
                obj.estado = new_estado

            db.commit()
            db.refresh(obj)
            p = db.query(Persona).filter(Persona.id_persona == obj.idpersona).first()
            return _serialize_usuarios_con_persona(obj, p)

        elif tabla == "persona":
            nuevo_distrito = body.get("distrito")
            if nuevo_distrito and nuevo_distrito != distrito_actual:
                _assert_jurisdiccion(nuevo_distrito, distritos_filter, "cambiar distrito a")
            new_dni = body.get("dni")
            if new_dni and new_dni != obj.dni:
                if db.query(Persona).filter(Persona.dni == new_dni).first():
                    raise HTTPException(status_code=400, detail="DNI ya registrado (UNIQUE violation)")
                obj.dni = new_dni
            for k, v in body.items():
                if hasattr(obj, k) and k != "dni":
                    setattr(obj, k, v)
            db.commit()
            db.refresh(obj)
            return _model_to_dict(obj)

        elif tabla == "usuarios_distritos":
            nuevo_distrito = body.get("distrito")
            if nuevo_distrito and nuevo_distrito != distrito_actual:
                _assert_jurisdiccion(nuevo_distrito, distritos_filter, "cambiar distrito a")
            for k, v in body.items():
                if hasattr(obj, k):
                    setattr(obj, k, v)
            try:
                db.commit()
                db.refresh(obj)
            except Exception as e:
                db.rollback()
                if "uq_usuario_distrito" in str(e).lower() or "unique" in str(e).lower():
                    raise HTTPException(status_code=400, detail=f"Duplicado en usuarios_distritos (UNIQUE violation): {e}")
                raise HTTPException(status_code=400, detail=str(e))
            u = db.query(Usuarios).filter(Usuarios.idusuario == obj.idusuario).first()
            return _serialize_usuarios_distritos(obj, u)

        elif tabla == "centro_votacion":
            nuevo_distrito = body.get("distrito")
            if nuevo_distrito and nuevo_distrito != distrito_actual:
                _assert_jurisdiccion(nuevo_distrito, distritos_filter, "cambiar distrito a")
            new_codigo = body.get("codigo_local")
            if new_codigo and new_codigo != obj.codigo_local:
                if db.query(CentroVotacion).filter(CentroVotacion.codigo_local == new_codigo).first():
                    raise HTTPException(status_code=400, detail="Código local ya existe (UNIQUE violation)")
                obj.codigo_local = new_codigo
            for k, v in body.items():
                if hasattr(obj, k) and k != "codigo_local":
                    setattr(obj, k, v)
            db.commit()
            db.refresh(obj)
            return _model_to_dict(obj)

        elif tabla == "mesa":
            nuevo_distrito = body.get("distrito")
            if nuevo_distrito and nuevo_distrito != distrito_actual:
                _assert_jurisdiccion(nuevo_distrito, distritos_filter, "cambiar distrito a")
            new_num_mesa = body.get("num_mesa")
            if new_num_mesa and new_num_mesa != obj.num_mesa:
                if db.query(Mesa).filter(Mesa.num_mesa == new_num_mesa).first():
                    raise HTTPException(status_code=400, detail="Número de mesa ya existe (UNIQUE violation)")
                obj.num_mesa = new_num_mesa
            for k, v in body.items():
                if hasattr(obj, k) and k != "num_mesa":
                    setattr(obj, k, v)
            db.commit()
            db.refresh(obj)
            return _model_to_dict(obj)

        elif tabla == "personeros_users":
            for k, v in body.items():
                if hasattr(obj, k):
                    setattr(obj, k, v)
            db.commit()
            db.refresh(obj)
            p = db.query(Persona).filter(Persona.id_persona == obj.id_persona).first() if obj.id_persona else None
            c = db.query(CentroVotacion).filter(CentroVotacion.id_centro == obj.id_centro_asignado).first() if obj.id_centro_asignado else None
            m = db.query(Mesa).filter(Mesa.id_mesa == obj.id_mesa_asignada).first() if obj.id_mesa_asignada else None
            return _serialize_personeros_users(obj, p, c, m)

        else:
            for k, v in body.items():
                if hasattr(obj, k):
                    setattr(obj, k, v)
            db.commit()
            db.refresh(obj)
            return _model_to_dict(obj)

    except HTTPException:
        raise
    except Exception as e:
        db.rollback()
        logger.error("Error actualizando registro en tabla %s: %s", tabla, e)
        if "unique" in str(e).lower() or "uq_" in str(e).lower():
            raise HTTPException(status_code=400, detail=f"Violación UNIQUE en {tabla}: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))


@router.delete("/tables/{tabla}/{id}")
def eliminar_tabla(
    tabla: str,
    id: int,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(ADMIN_ROLE_CHECKER),
):
    _validar_tabla(tabla)
    Model = MODELS_MAP[tabla]
    pk_col = MODEL_PK[tabla]
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    obj = db.query(Model).filter(getattr(Model, pk_col) == id).first()
    if not obj:
        raise HTTPException(status_code=404, detail=f"Registro {id} no encontrado en {tabla}")

    distrito = _extract_distrito_from_obj(tabla, obj, db)
    _assert_jurisdiccion(distrito, distritos_filter, "eliminar")

    try:
        db.delete(obj)
        db.commit()
        return {"mensaje": f"Registro {id} eliminado de {tabla}", "eliminado": True}
    except Exception as e:
        db.rollback()
        logger.error("Error eliminando registro en tabla %s: %s", tabla, e)
        raise HTTPException(status_code=400, detail=f"No se pudo eliminar: {str(e)}")


# ============================================================
# C) DOWNLOAD REPORTE EXCEL COMPLETO
# ============================================================
def _estilizar_hoja(ws, headers: List[str], data_rows: List[List[Any]]):
    font_negrita = Font(bold=True, color="FFFFFF")
    fill_header = PatternFill(start_color="4472C4", end_color="4472C4", fill_type="solid")
    align_center = Alignment(horizontal="center", vertical="center", wrap_text=True)
    align_left = Alignment(horizontal="left", vertical="center", wrap_text=True)

    for col_idx, header in enumerate(headers, 1):
        cell = ws.cell(row=1, column=col_idx, value=header)
        cell.font = font_negrita
        cell.fill = fill_header
        cell.alignment = align_center

    for r_idx, row in enumerate(data_rows, 2):
        for c_idx, val in enumerate(row, 1):
            cell = ws.cell(row=r_idx, column=c_idx)
            if isinstance(val, datetime):
                cell.value = val.isoformat()
            else:
                cell.value = val
            cell.alignment = align_left

    for col_idx in range(1, len(headers) + 1):
        max_len = 0
        col_letter = get_column_letter(col_idx)
        for row_idx in range(1, len(data_rows) + 2):
            cell_val = ws.cell(row=row_idx, column=col_idx).value
            if cell_val is None:
                continue
            l = len(str(cell_val))
            if l > max_len:
                max_len = l
        ws.column_dimensions[col_letter].width = min(max(max_len + 2, 12), 60)

    ws.freeze_panes = "A2"


@router.get("/reporte/excel")
def descargar_reporte_excel(
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(ADMIN_ROLE_CHECKER),
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    wb = Workbook()
    wb.remove(wb.active)

    headers_personas = ["ID", "DNI", "Nombre", "Apellidos", "Teléfono 1", "Teléfono 2",
                        "Correo", "Distrito", "Provincia", "Dirección"]
    q_p = db.query(Persona)
    if distritos_filter is not None:
        q_p = q_p.filter(Persona.distrito.in_(distritos_filter))
    rows_p = [[p.id_persona, p.dni, p.nombre, p.apellidos, p.telefono1, p.telefono2,
               p.correo, p.distrito, p.provincia, p.direccion] for p in q_p.all()]
    ws1 = wb.create_sheet("Personas")
    _estilizar_hoja(ws1, headers_personas, rows_p)

    headers_usuarios = ["ID Usuario", "Usuario", "Rol", "Estado", "Fecha Creado",
                        "ID Persona", "DNI", "Nombre", "Apellidos", "Distrito", "Provincia"]
    q_u = db.query(Usuarios)
    if distritos_filter is not None:
        q_u = q_u.filter(
            Usuarios.idpersona.in_(
                Persona.__table__.select().where(Persona.distrito.in_(distritos_filter)).with_only_columns(Persona.id_persona)
            )
        )
    rows_u = []
    for u in q_u.all():
        p = db.query(Persona).filter(Persona.id_persona == u.idpersona).first()
        rows_u.append([
            u.idusuario, u.usuario, u.rol, "ACTIVO" if u.estado else "INACTIVO",
            u.fecha_creado, u.idpersona,
            p.dni if p else "", p.nombre if p else "", p.apellidos if p else "",
            p.distrito if p else "", p.provincia if p else ""
        ])
    ws2 = wb.create_sheet("Usuarios")
    _estilizar_hoja(ws2, headers_usuarios, rows_u)

    headers_distritos = ["ID", "ID Usuario", "Nombre Usuario", "Distrito"]
    q_ud = db.query(UsuariosDistritos)
    if distritos_filter is not None:
        q_ud = q_ud.filter(UsuariosDistritos.distrito.in_(distritos_filter))
    rows_ud = []
    for ud in q_ud.all():
        u = db.query(Usuarios).filter(Usuarios.idusuario == ud.idusuario).first()
        rows_ud.append([ud.id, ud.idusuario, u.usuario if u else "", ud.distrito])
    ws3 = wb.create_sheet("Distritos Asignados")
    _estilizar_hoja(ws3, headers_distritos, rows_ud)

    headers_centros = ["ID Centro", "Código Local", "Ubigeo", "Nombre Local",
                       "Dirección", "Departamento", "Provincia", "Distrito"]
    q_c = db.query(CentroVotacion)
    if distritos_filter is not None:
        q_c = q_c.filter(CentroVotacion.distrito.in_(distritos_filter))
    rows_c = [[c.id_centro, c.codigo_local, c.ubigeo, c.nombre_local,
               c.direccion_local, c.departamento, c.provincia, c.distrito]
              for c in q_c.all()]
    ws4 = wb.create_sheet("Centros de Votacion")
    _estilizar_hoja(ws4, headers_centros, rows_c)

    headers_mesas = ["ID Mesa", "Número Mesa", "Cód. Local", "Ubigeo", "Nombre Local",
                     "Dirección", "Departamento", "Provincia", "Distrito"]
    q_m = db.query(Mesa)
    if distritos_filter is not None:
        q_m = q_m.filter(Mesa.distrito.in_(distritos_filter))
    rows_m = [[m.id_mesa, m.num_mesa, m.cod_local, m.codi_ubigeo, m.nombre_local,
               m.direccion_local, m.departamento, m.provincia, m.distrito]
              for m in q_m.all()]
    ws5 = wb.create_sheet("Mesas")
    _estilizar_hoja(ws5, headers_mesas, rows_m)

    headers_personeros = ["ID Solicitud", "Tipo Personero", "Estado", "Fecha Registro",
                          "Fecha Asignación", "ID Persona", "DNI", "Nombre Persona",
                          "Apellidos", "ID Centro", "Centro", "ID Mesa", "N° Mesa"]
    q_pu = db.query(PersonerosUsers)
    if distritos_filter is not None:
        q_pu = q_pu.filter(
            or_(
                PersonerosUsers.id_persona.in_(
                    Persona.__table__.select().where(Persona.distrito.in_(distritos_filter)).with_only_columns(Persona.id_persona)
                ),
                PersonerosUsers.id_centro_asignado.in_(
                    CentroVotacion.__table__.select().where(CentroVotacion.distrito.in_(distritos_filter)).with_only_columns(CentroVotacion.id_centro)
                ),
                PersonerosUsers.id_mesa_asignada.in_(
                    Mesa.__table__.select().where(Mesa.distrito.in_(distritos_filter)).with_only_columns(Mesa.id_mesa)
                ),
            )
        )
    rows_pu = []
    for pu in q_pu.all():
        p = db.query(Persona).filter(Persona.id_persona == pu.id_persona).first() if pu.id_persona else None
        c = db.query(CentroVotacion).filter(CentroVotacion.id_centro == pu.id_centro_asignado).first() if pu.id_centro_asignado else None
        m = db.query(Mesa).filter(Mesa.id_mesa == pu.id_mesa_asignada).first() if pu.id_mesa_asignada else None
        rows_pu.append([
            pu.id_solicitud, pu.tipo_personero_solicitado or "",
            pu.estado_vinculacion or "", pu.fecha_registro, pu.fecha_asignacion,
            pu.id_persona or "", p.dni if p else "", p.nombre if p else "",
            p.apellidos if p else "", pu.id_centro_asignado or "",
            c.nombre_local if c else "", pu.id_mesa_asignada or "",
            m.num_mesa if m else ""
        ])
    ws6 = wb.create_sheet("Personeros - Asignaciones")
    _estilizar_hoja(ws6, headers_personeros, rows_pu)

    output = BytesIO()
    wb.save(output)
    output.seek(0)

    ts = datetime.now().strftime("%Y%m%d_%H%M")
    filename = f"reporte_completo_{ts}.xlsx"

    return StreamingResponse(
        output,
        media_type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        headers={"Content-Disposition": f'attachment; filename="{filename}"'},
    )


# ============================================================
# E) TRUNCAR TABLA (Solo SUPER_ADMIN)
# ============================================================
TRUNCATE_ALLOWED = {
    "persona": True,
    "centro_votacion": True,
    "mesa": True,
    "personeros_users": True,
    "usuarios_distritos": True,
}

SUPER_ADMIN_CHECKER = RoleChecker(["SUPER_ADMIN"])


@router.post("/tables/{tabla}/vaciar")
def vaciar_tabla(
    tabla: str,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(SUPER_ADMIN_CHECKER),
):
    if tabla == "usuarios":
        raise HTTPException(
            status_code=400,
            detail="No se permite vaciar tabla usuarios para proteger credenciales. Borre usuarios uno a uno."
        )

    if tabla not in TRUNCATE_ALLOWED:
        raise HTTPException(
            status_code=400,
            detail=f"Tabla no permitida para vaciar. Válidas: {list(TRUNCATE_ALLOWED.keys())}"
        )

    registros_eliminados = 0
    Model = MODELS_MAP.get(tabla)
    if Model:
        registros_eliminados = db.query(Model).count()

    try:
        if tabla == "persona":
            db.execute(text("TRUNCATE TABLE personeros_users RESTART IDENTITY CASCADE"))
            db.execute(text("TRUNCATE TABLE usuarios RESTART IDENTITY CASCADE"))
            db.execute(text("TRUNCATE TABLE persona RESTART IDENTITY CASCADE"))
        elif tabla == "centro_votacion":
            db.execute(text("UPDATE mesa SET cod_local = NULL"))
            db.execute(text("TRUNCATE TABLE centro_votacion RESTART IDENTITY CASCADE"))
        elif tabla == "mesa":
            db.execute(text("UPDATE personeros_users SET id_mesa_asignada = NULL"))
            db.execute(text("TRUNCATE TABLE mesa RESTART IDENTITY CASCADE"))
        elif tabla == "usuarios_distritos":
            db.execute(text("TRUNCATE TABLE usuarios_distritos RESTART IDENTITY CASCADE"))
        elif tabla == "personeros_users":
            db.execute(text("TRUNCATE TABLE personeros_users RESTART IDENTITY CASCADE"))

        db.commit()
    except Exception as e:
        db.rollback()
        logger.error("Error truncando tabla %s: %s", tabla, e)
        raise HTTPException(status_code=400, detail=f"Error al vaciar tabla: {str(e)}")

    return {
        "tabla": tabla,
        "registros_eliminados": registros_eliminados,
        "ok": True,
        "mensaje": f"Tabla {tabla} vaciada correctamente",
    }


# ============================================================
# F) CARGA MASIVA GENERALIZADA
# ============================================================
IMPORT_ALLOWED = ["mesa", "centro_votacion", "persona"]


def _import_generic(tabla: str, file: UploadFile, current_user: Usuarios, db: Session):
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    if not file.filename or not file.filename.endswith((".xlsx", ".xlsm")):
        raise HTTPException(status_code=400, detail="Archivo debe ser Excel (.xlsx/.xlsm)")

    from openpyxl import load_workbook

    try:
        content = file.file.read()
        wb = load_workbook(BytesIO(content), data_only=True)
    except Exception as e:
        logger.error("Error leyendo Excel: %s", e)
        raise HTTPException(status_code=400, detail=f"No se pudo leer el archivo Excel: {str(e)}")

    ws = wb.active
    if ws is None:
        raise HTTPException(status_code=400, detail="Libro Excel sin hojas")

    errores = []
    importados = 0
    preview = []

    if tabla == "mesa":
        col_esperadas = ["num_mesa", "cod_local", "codi_ubigeo", "nombre_local",
                         "direccion_local", "departamento", "provincia", "distrito"]
    elif tabla == "centro_votacion":
        col_esperadas = ["codigo_local", "ubigeo", "nombre_local", "direccion_local",
                         "departamento", "provincia", "distrito"]
    elif tabla == "persona":
        col_esperadas = ["dni", "nombre", "apellidos", "telefono1", "telefono2",
                         "distrito", "provincia", "direccion"]
    else:
        col_esperadas = []

    headers_raw = [cell.value for cell in ws[1]]
    headers = [str(h).strip().lower() if h else "" for h in headers_raw]

    col_idx = {}
    for expected in col_esperadas:
        idx = None
        for i, h in enumerate(headers):
            if expected == h or expected.replace("_", "") == h.replace("_", "") or expected in h:
                idx = i
                break
        col_idx[expected] = idx

    def _val(idx, vals):
        if idx is None or idx >= len(vals):
            return None
        v = vals[idx]
        if v is None:
            return None
        return str(v).strip()

    row_num = 1
    for row in ws.iter_rows(min_row=2, values_only=True):
        row_num += 1
        vals = list(row)
        if all(v is None or v == "" for v in vals):
            continue

        try:
            if tabla == "mesa":
                num_mesa = _val(col_idx["num_mesa"], vals)
                if not num_mesa:
                    errores.append({"fila": row_num, "key": None, "error": "num_mesa vacío"})
                    continue
                cod_local = _val(col_idx["cod_local"], vals)
                codi_ubigeo = _val(col_idx["codi_ubigeo"], vals)
                nombre_local = _val(col_idx["nombre_local"], vals)
                direccion_local = _val(col_idx["direccion_local"], vals)
                departamento = _val(col_idx["departamento"], vals)
                provincia = _val(col_idx["provincia"], vals)
                distrito = _val(col_idx["distrito"], vals)

                _assert_jurisdiccion(distrito, distritos_filter, f"importar mesa fila {row_num} en")

                existente = db.query(Mesa).filter(Mesa.num_mesa == num_mesa).first()
                if existente:
                    existente.cod_local = cod_local
                    existente.codi_ubigeo = codi_ubigeo
                    existente.nombre_local = nombre_local
                    existente.direccion_local = direccion_local
                    existente.departamento = departamento
                    existente.provincia = provincia
                    existente.distrito = distrito
                else:
                    mesa = Mesa(
                        num_mesa=num_mesa,
                        cod_local=cod_local,
                        codi_ubigeo=codi_ubigeo,
                        nombre_local=nombre_local,
                        direccion_local=direccion_local,
                        departamento=departamento,
                        provincia=provincia,
                        distrito=distrito,
                    )
                    db.add(mesa)
                importados += 1

                fila_ok = {
                    "fila": row_num,
                    "num_mesa": num_mesa,
                    "cod_local": cod_local,
                    "codi_ubigeo": codi_ubigeo,
                    "nombre_local": nombre_local,
                    "provincia": provincia,
                    "distrito": distrito,
                }
                if len(preview) < 10:
                    preview.append(fila_ok)

            elif tabla == "centro_votacion":
                codigo_local = _val(col_idx["codigo_local"], vals)
                if not codigo_local:
                    errores.append({"fila": row_num, "key": None, "error": "codigo_local vacío"})
                    continue
                ubigeo = _val(col_idx["ubigeo"], vals)
                nombre_local = _val(col_idx["nombre_local"], vals)
                direccion_local = _val(col_idx["direccion_local"], vals)
                departamento = _val(col_idx["departamento"], vals)
                provincia = _val(col_idx["provincia"], vals)
                distrito = _val(col_idx["distrito"], vals)

                _assert_jurisdiccion(distrito, distritos_filter, f"importar centro fila {row_num} en")

                existente = db.query(CentroVotacion).filter(CentroVotacion.codigo_local == codigo_local).first()
                if existente:
                    existente.ubigeo = ubigeo
                    existente.nombre_local = nombre_local
                    existente.direccion_local = direccion_local
                    existente.departamento = departamento
                    existente.provincia = provincia
                    existente.distrito = distrito
                else:
                    centro = CentroVotacion(
                        codigo_local=codigo_local,
                        ubigeo=ubigeo,
                        nombre_local=nombre_local,
                        direccion_local=direccion_local,
                        departamento=departamento,
                        provincia=provincia,
                        distrito=distrito,
                    )
                    db.add(centro)
                importados += 1

                fila_ok = {
                    "fila": row_num,
                    "codigo_local": codigo_local,
                    "ubigeo": ubigeo,
                    "nombre_local": nombre_local,
                    "provincia": provincia,
                    "distrito": distrito,
                }
                if len(preview) < 10:
                    preview.append(fila_ok)

            elif tabla == "persona":
                dni = _val(col_idx["dni"], vals)
                if not dni:
                    errores.append({"fila": row_num, "key": None, "error": "dni vacío"})
                    continue
                nombre = _val(col_idx["nombre"], vals)
                apellidos = _val(col_idx["apellidos"], vals)
                telefono1 = _val(col_idx["telefono1"], vals)
                telefono2 = _val(col_idx["telefono2"], vals)
                distrito = _val(col_idx["distrito"], vals)
                provincia = _val(col_idx["provincia"], vals)
                direccion = _val(col_idx["direccion"], vals)

                _assert_jurisdiccion(distrito, distritos_filter, f"importar persona fila {row_num} en")

                existente = db.query(Persona).filter(Persona.dni == dni).first()
                if existente:
                    existente.nombre = nombre
                    existente.apellidos = apellidos
                    existente.telefono1 = telefono1
                    existente.telefono2 = telefono2
                    existente.distrito = distrito
                    existente.provincia = provincia
                    existente.direccion = direccion
                else:
                    persona = Persona(
                        dni=dni,
                        nombre=nombre,
                        apellidos=apellidos,
                        telefono1=telefono1,
                        telefono2=telefono2,
                        distrito=distrito,
                        provincia=provincia,
                        direccion=direccion,
                    )
                    db.add(persona)
                importados += 1

                fila_ok = {
                    "fila": row_num,
                    "dni": dni,
                    "nombre": nombre,
                    "apellidos": apellidos,
                    "provincia": provincia,
                    "distrito": distrito,
                }
                if len(preview) < 10:
                    preview.append(fila_ok)

        except HTTPException as e:
            errores.append({"fila": row_num, "key": None, "error": e.detail})
            continue
        except Exception as e:
            db.rollback()
            errores.append({"fila": row_num, "key": None, "error": str(e)})
            continue

    try:
        db.commit()
    except Exception as e:
        db.rollback()
        logger.error("Error commit import Excel: %s", e)
        raise HTTPException(status_code=400, detail=f"Error guardando cambios: {str(e)}")

    return {
        "importados": importados,
        "errores": errores,
        "preview": preview,
    }


@router.post("/import/{tabla}")
def importar_tabla_excel(
    tabla: str,
    file: UploadFile = File(...),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(ADMIN_ROLE_CHECKER),
):
    if tabla not in IMPORT_ALLOWED:
        raise HTTPException(
            status_code=404,
            detail=f"Tabla no permitida para importar. Válidas: {IMPORT_ALLOWED}"
        )
    return _import_generic(tabla, file, current_user, db)


@router.post("/mesas/import-excel")
def importar_mesas_excel(
    file: UploadFile = File(...),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(ADMIN_ROLE_CHECKER),
):
    return _import_generic("mesa", file, current_user, db)
