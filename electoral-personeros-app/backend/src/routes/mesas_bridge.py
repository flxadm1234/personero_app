"""
ENDPOINT PUENTE /api/mesas y /api/centros_votacion
Compatibilidad con codigo Android antiguo que usa paths viejos.
Delega directamente a consultas sobre Mesa/CentroVotacion y retorna dicts simples.
"""
from typing import Optional, List, Dict, Any
from fastapi import APIRouter, Depends, Query, HTTPException
from sqlalchemy.orm import Session

from src.config.database import get_db
from src.middlewares.auth import get_current_user
from src.models.usuario import Usuarios
from src.models.centro_votacion import CentroVotacion
from src.models.mesa import Mesa
from src.models.persona import Persona
from src.models.personeros_users import PersonerosUsers

router = APIRouter(tags=["Mesas/Centros (Bridge compatibilidad Android)"])


def _estado_y_personero(db: Session, m: Mesa) -> tuple[str, Optional[Dict[str, Any]]]:
    user = None
    estado = "LIBRE"
    for attr in ("id_personero_asignado", "id_usuario_asignado", "personero_asignado_id"):
        uid = getattr(m, attr, None)
        if uid:
            user = db.query(Usuarios).filter(Usuarios.idusuario == uid).first()
            if user:
                break
    if user is None:
        try:
            pu = db.query(PersonerosUsers).filter(
                PersonerosUsers.id_mesa == m.id_mesa,
                PersonerosUsers.estado == "ASIGNADO"
            ).first()
            if pu and getattr(pu, "id_usuario", None):
                user = db.query(Usuarios).filter(Usuarios.idusuario == pu.id_usuario).first()
        except Exception:
            pass
    personero = None
    if user:
        estado = "OCUPADA"
        p = db.query(Persona).filter(Persona.id_persona == user.idpersona).first()
        personero = {
            "idusuario": user.idusuario,
            "nombre": (p.nombre if p else "") or "",
            "apellidos": (p.apellidos if p else "") or "",
            "dni": (p.dni if p else "") or "",
            "telefono1": (p.telefono1 if p else "") or "",
        }
    return estado, personero


def _mesa_to_dict(db: Session, m: Mesa, centro: Optional[CentroVotacion] = None) -> Dict[str, Any]:
    c = centro
    if c is None and getattr(m, "id_centro_votacion", None):
        c = db.query(CentroVotacion).filter(CentroVotacion.id_centro == m.id_centro_votacion).first()
    estado, personero = _estado_y_personero(db, m)
    return {
        "id_mesa": m.id_mesa,
        "num_mesa": m.num_mesa or "",
        "cod_local": (getattr(m, "cod_local", None) or "") or (c.codigo_local if c else "") or "",
        "nombre_local": (c.nombre_local if c else "") or (getattr(m, "nombre_local", None) or "") or "",
        "direccion_local": (c.direccion_local if c else "") or (getattr(m, "direccion_local", None) or "") or "",
        "distrito": (c.distrito if c else "") or (getattr(m, "distrito", None) or "") or "",
        "provincia": (c.provincia if c else "") or (getattr(m, "provincia", None) or "") or "",
        "departamento": "LORETO",
        "codi_ubigeo": (c.codi_ubigeo if c else "") or "",
        "estado": estado,
        "personero": personero,
    }


def _centro_to_dict(c: CentroVotacion) -> Dict[str, Any]:
    return {
        "id_centro": c.id_centro,
        "codigo_local": c.codigo_local or "",
        "nombre_local": c.nombre_local or "",
        "direccion_local": c.direccion_local or "",
        "departamento": getattr(c, "departamento", "LORETO") or "LORETO",
        "provincia": c.provincia or "",
        "distrito": c.distrito or "",
        "codi_ubigeo": getattr(c, "codi_ubigeo", None) or getattr(c, "ubigeo", "") or "",
        "longitud": getattr(c, "longitud", None),
        "latitud": getattr(c, "latitud", None),
        "estado": getattr(c, "estado", True),
    }


@router.get("/api/mesas", response_model=List[Dict[str, Any]])
@router.get("/api/mesas/", response_model=List[Dict[str, Any]], include_in_schema=False)
def listar_mesas_bridge(
    centroVotacionId: Optional[int] = Query(None),
    centroId: Optional[int] = Query(None),
    estado: Optional[str] = Query(None),
    personeroId: Optional[int] = Query(None),
    provincia: Optional[str] = Query(None),
    distrito: Optional[str] = Query(None),
    soloDisponibles: Optional[bool] = Query(None),
    perPage: Optional[int] = Query(500),
    db: Session = Depends(get_db),
    _: Usuarios = Depends(get_current_user),
):
    _cid = centroId or centroVotacionId
    q = db.query(Mesa)
    if _cid:
        try:
            q = q.filter(Mesa.id_centro_votacion == _cid)
        except Exception:
            pass
    mesas = q.all()
    centros_cache: Dict[int, Optional[CentroVotacion]] = {}
    resultados: List[Dict[str, Any]] = []
    for m in mesas:
        cid = getattr(m, "id_centro_votacion", None)
        centro = None
        if cid:
            if cid not in centros_cache:
                centros_cache[cid] = db.query(CentroVotacion).filter(CentroVotacion.id_centro == cid).first()
            centro = centros_cache[cid]
        if provincia and centro and centro.provincia != provincia:
            continue
        if distrito and centro and centro.distrito != distrito:
            continue
        d = _mesa_to_dict(db, m, centro)
        if soloDisponibles and d["estado"] != "LIBRE":
            continue
        if estado and not d["estado"].upper().startswith(estado.upper()[:3]):
            continue
        if personeroId and (d.get("personero") or {}).get("idusuario") != personeroId:
            continue
        resultados.append(d)
        if perPage and len(resultados) >= perPage:
            break
    return resultados


@router.get("/api/mesas/disponibles", response_model=List[Dict[str, Any]])
def listar_mesas_disponibles_bridge(
    centroVotacionId: Optional[int] = Query(None),
    centroId: Optional[int] = Query(None),
    provincia: Optional[str] = Query(None),
    distrito: Optional[str] = Query(None),
    db: Session = Depends(get_db),
    current: Usuarios = Depends(get_current_user),
):
    return listar_mesas_bridge(
        centroVotacionId=centroVotacionId, centroId=centroId,
        provincia=provincia, distrito=distrito, soloDisponibles=True,
        perPage=500, db=db, _=current,
    )


@router.get("/api/mesas/{id}", response_model=Dict[str, Any])
def get_mesa_bridge(id: int, db: Session = Depends(get_db), _: Usuarios = Depends(get_current_user)):
    m = db.query(Mesa).filter(Mesa.id_mesa == id).first()
    if not m:
        raise HTTPException(status_code=404, detail="Mesa no encontrada")
    return _mesa_to_dict(db, m)


@router.get("/api/centros-votacion", response_model=List[Dict[str, Any]])
@router.get("/api/centros-votacion/", response_model=List[Dict[str, Any]], include_in_schema=False)
@router.get("/api/centros_votacion", response_model=List[Dict[str, Any]])
@router.get("/api/centros_votacion/", response_model=List[Dict[str, Any]], include_in_schema=False)
def listar_centros_bridge(
    provincia: Optional[str] = Query(None),
    distrito: Optional[str] = Query(None),
    search: Optional[str] = Query(None),
    perPage: Optional[int] = Query(None),
    db: Session = Depends(get_db),
    _: Usuarios = Depends(get_current_user),
):
    q = db.query(CentroVotacion)
    if provincia:
        q = q.filter(CentroVotacion.provincia == provincia)
    if distrito:
        q = q.filter(CentroVotacion.distrito == distrito)
    centros = q.all()
    resultados = []
    for c in centros:
        if search:
            sl = search.lower()
            hay = False
            for campo in [c.nombre_local, c.codigo_local, c.direccion_local, c.distrito, c.provincia, getattr(c, "codi_ubigeo", None) or getattr(c, "ubigeo", "")]:
                if campo and sl in str(campo).lower():
                    hay = True
                    break
            if not hay:
                continue
        resultados.append(_centro_to_dict(c))
        if perPage and len(resultados) >= perPage:
            break
    return resultados
