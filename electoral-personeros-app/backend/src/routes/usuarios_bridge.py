"""
ENDPOINT PUENTE /api/usuarios
Compatibilidad con Android que espera los paths /api/usuarios GET|POST|PUT|DELETE
con parametros activo/search/provincia/perPage en vez de estado/busqueda.
Mapea internamente a la misma logica que /api/superadmin/usuarios PERO no
requiere SUPER_ADMIN si el usuario ya tiene token ADMINISTRADOR (lectura).

SCOPE JURISDICCIONAL ADMINISTRADOR:
- ADMINISTRADOR solo ve/crea/edita usuarios cuyo Persona.distrito coincida
  con sus distritos_asignados en la tabla N:M usuarios_distritos.
- SUPER_ADMIN no tiene restricciones.
"""
from typing import Optional, List
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session

from src.config.database import get_db
from src.middlewares.auth import (
    RoleChecker,
    get_password_hash,
    get_current_user,
    get_current_user_with_persona,
)
from src.models.usuario import Usuarios
from src.models.persona import Persona
from src.models.personeros_users import PersonerosUsers
from src.models.usuarios_distritos import UsuariosDistritos
from src.schemas import (
    UsuarioCreateRequest,
    UsuarioUpdateRequest,
    UsuarioWithPersonaResponse,
    PersonaDNILookupResponse,
)

router = APIRouter(prefix="/api/usuarios", tags=["Usuarios (Bridge Android)"])


# ============================================================
# HELPERS SCOPE JURISDICCIONAL
# ============================================================
def _get_admin_distritos_filter(current_user: Usuarios, db: Session) -> Optional[List[str]]:
    """
    Si el usuario actual es ADMINISTRADOR devuelve lista de distritos permitidos
    (tabla usuarios_distritos). Si es SUPER_ADMIN devuelve None (sin filtro).
    Para otros roles devuelve lista vacía (sin acceso a nada).
    """
    if current_user.rol == "SUPER_ADMIN":
        return None
    if current_user.rol == "ADMINISTRADOR":
        qs = db.query(UsuariosDistritos).filter(UsuariosDistritos.idusuario == current_user.idusuario).all()
        dists = [ud.distrito for ud in qs if ud.distrito]
        return dists
    return []


def _assert_jurisdiccion(distrito: Optional[str], distritos_filter: Optional[List[str]], accion: str):
    """
    Lanza 403 Forbidden si el distrito objetivo no está dentro de la jurisdicción del admin.
    distritos_filter = None -> SUPER_ADMIN, sin restricciones.
    """
    if distritos_filter is None:
        return
    if not distrito:
        raise HTTPException(
            status_code=403,
            detail=f"No tiene permiso para {accion}: el usuario no tiene distrito asignado y usted tiene jurisdicción limitada."
        )
    if distrito not in distritos_filter:
        raise HTTPException(
            status_code=403,
            detail=(
                f"No tiene permiso para {accion} este usuario. "
                f"Su jurisdicción cubre los distritos: {distritos_filter}. "
                f"El usuario pertenece al distrito: '{distrito}'."
            )
        )


def _serialize(user: Usuarios, persona: Optional[Persona] = None):
    from src.schemas import PersonaResponse
    if not persona:
        persona = Persona()
    return UsuarioWithPersonaResponse(
        idusuario=user.idusuario,
        usuario=user.usuario,
        rol=user.rol,
        fecha_creado=user.fecha_creado,
        estado=user.estado,
        idpersona=user.idpersona,
        persona=PersonaResponse(
            id_persona=persona.id_persona if persona else 0,
            dni=persona.dni if persona else "",
            nombre=persona.nombre if persona else "",
            apellidos=persona.apellidos if persona else "",
            telefono1=persona.telefono1 if persona else "",
            telefono2=getattr(persona, "telefono2", None),
            correo=getattr(persona, "correo", None),
            distrito=persona.distrito if persona else "",
            provincia=persona.provincia if persona else "",
            direccion=getattr(persona, "direccion", None),
        ),
    )


# ============================================================
# GET /api/usuarios  (lista usuarios. Parametros Android:
#   rol, activo (en vez de estado), provincia, search (en vez de busqueda), perPage)
# CON FILTRO JURISDICCIONAL: ADMINISTRADOR solo ve usuarios de sus distritos_asignados.
# ============================================================
@router.get("", response_model=List[UsuarioWithPersonaResponse])
def listar_usuarios_bridge(
    rol: Optional[str] = Query(None),
    activo: Optional[bool] = Query(None),
    provincia: Optional[str] = Query(None),
    search: Optional[str] = Query(None),
    perPage: Optional[int] = Query(None),
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    distritos_filter = _get_admin_distritos_filter(current_user, db)

    query = db.query(Usuarios)
    if rol:
        query = query.filter(Usuarios.rol == rol)
    if activo is not None:
        query = query.filter(Usuarios.estado == activo)

    usuarios = query.all()
    resultados: List[UsuarioWithPersonaResponse] = []
    for user in usuarios:
        p = db.query(Persona).filter(Persona.id_persona == user.idpersona).first()

        # SCOPE: si usuario actual es ADMINISTRADOR, solo incluir si distrito está en su lista
        if distritos_filter is not None and p and p.distrito and p.distrito not in distritos_filter:
            continue
        # Caso especial: ADMINISTRADOR no puede ver usuarios sin distrito (no pertenece a nadie)
        if distritos_filter is not None and (not p or not p.distrito):
            continue

        if p and provincia and p.provincia != provincia:
            continue
        if search and p:
            search_l = search.lower()
            hay_match = (
                (user.usuario and search_l in user.usuario.lower())
                or (p.nombre and search_l in p.nombre.lower())
                or (p.apellidos and search_l in p.apellidos.lower())
                or (p.dni and search_l in p.dni.lower())
            )
            if not hay_match:
                continue
        resultados.append(_serialize(user, p))
        if perPage and len(resultados) >= perPage:
            break
    return resultados


# ============================================================
# GET /api/usuarios/{id}
# CON FILTRO JURISDICCIONAL: ADMINISTRADOR solo puede ver si el usuario pertenece a sus distritos.
# ============================================================
@router.get("/{id}", response_model=UsuarioWithPersonaResponse)
def get_usuario_bridge(
    id: int,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(get_current_user),
):
    user = db.query(Usuarios).filter(Usuarios.idusuario == id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    p = db.query(Persona).filter(Persona.id_persona == user.idpersona).first()

    distritos_filter = _get_admin_distritos_filter(current_user, db)
    _assert_jurisdiccion(p.distrito if p else None, distritos_filter, "ver")

    return _serialize(user, p)


# ============================================================
# POST /api/usuarios   -> crear usuario + persona (igual que superadmin)
# Acepta body con los campos de UsuarioCreateRequest O campos mapeados
#   desde el Usuario Kotlin Android (usuario, password, rol, dni, nombre,
#   apellidos, telefono1, telefono2, distrito, provincia, direccion, estado)
# CON FILTRO JURISDICCIONAL: ADMINISTRADOR solo puede crear usuarios en sus distritos.
# ============================================================
@router.post("", status_code=201, response_model=UsuarioWithPersonaResponse)
def crear_usuario_bridge(
    body: dict,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(RoleChecker(["SUPER_ADMIN", "ADMINISTRADOR"])),
):
    # Normalize keys: password -> clave, phone -> telefono1, etc.
    dni = body.get("dni") or (body.get("persona") or {}).get("dni") or ""
    nombre = body.get("nombre") or (body.get("persona") or {}).get("nombre") or ""
    apellidos = body.get("apellidos") or (body.get("persona") or {}).get("apellidos") or ""
    telefono1 = body.get("telefono1") or body.get("telefono") or (body.get("persona") or {}).get("telefono1") or ""
    telefono2 = body.get("telefono2") or body.get("whatsapp") or (body.get("persona") or {}).get("telefono2") or None
    distrito = body.get("distrito") or body.get("distritoAsignado") or (body.get("persona") or {}).get("distrito") or ""
    provincia = body.get("provincia") or body.get("provinciaAsignada") or (body.get("persona") or {}).get("provincia") or ""
    direccion = body.get("direccion") or (body.get("persona") or {}).get("direccion") or None
    usuario = body.get("usuario") or ""
    clave = body.get("clave") or body.get("password") or ""
    rol = body.get("rol") or "ADMINISTRADOR"

    # SCOPE JURISDICCIONAL: validar distrito antes de insertar
    distritos_filter = _get_admin_distritos_filter(current_user, db)
    _assert_jurisdiccion(distrito if distrito else None, distritos_filter, "crear")

    if not usuario or not clave:
        raise HTTPException(status_code=400, detail="Usuario y contraseña son requeridos")
    if not dni or not nombre or not apellidos or not distrito or not provincia:
        raise HTTPException(status_code=400, detail="DNI, nombre, apellidos, provincia y distrito son requeridos")

    if db.query(Usuarios).filter(Usuarios.usuario == usuario).first():
        raise HTTPException(status_code=400, detail="Usuario ya existe")
    if db.query(Persona).filter(Persona.dni == dni).first():
        raise HTTPException(status_code=400, detail="DNI ya registrado")

    p = Persona(dni=dni, nombre=nombre, apellidos=apellidos,
                telefono1=telefono1, telefono2=telefono2,
                distrito=distrito, provincia=provincia, direccion=direccion)
    db.add(p); db.flush()
    user = Usuarios(idpersona=p.id_persona, usuario=usuario,
                    clave=get_password_hash(clave), rol=rol,
                    estado=body.get("estado", True) if body.get("estado") is not None else True)
    db.add(user); db.commit(); db.refresh(user); db.refresh(p)
    return _serialize(user, p)


# ============================================================
# PUT /api/usuarios/{id}
# CON FILTRO JURISDICCIONAL:
#   - ADMINISTRADOR solo puede editar usuarios en sus distritos.
#   - Si cambia distrito, el NUEVO distrito también debe estar en sus distritos.
# ============================================================
@router.put("/{id}", response_model=UsuarioWithPersonaResponse)
def actualizar_usuario_bridge(
    id: int,
    body: dict,
    db: Session = Depends(get_db),
    current_user: Usuarios = Depends(RoleChecker(["SUPER_ADMIN", "ADMINISTRADOR"])),
):
    user = db.query(Usuarios).filter(Usuarios.idusuario == id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")

    distritos_filter = _get_admin_distritos_filter(current_user, db)

    # Obtener distrito actual del usuario para validar jurisdicción ORIGEN
    p_actual = db.query(Persona).filter(Persona.id_persona == user.idpersona).first()
    distrito_actual = p_actual.distrito if p_actual else None
    _assert_jurisdiccion(distrito_actual, distritos_filter, "editar")

    new_usuario = body.get("usuario")
    new_clave = body.get("clave") or body.get("password")
    new_rol = body.get("rol")
    new_estado = body.get("estado") if "estado" in body else (body.get("activo") if "activo" in body else None)

    # Determinar distrito NUEVO (si cambia) y validar jurisdicción DESTINO
    nuevo_distrito = body.get("distrito") or body.get("distritoAsignado") or (body.get("persona") or {}).get("distrito")
    if nuevo_distrito and nuevo_distrito != distrito_actual:
        _assert_jurisdiccion(nuevo_distrito, distritos_filter, "cambiar el distrito de")

    if new_usuario and new_usuario != user.usuario:
        if db.query(Usuarios).filter(Usuarios.usuario == new_usuario).first():
            raise HTTPException(status_code=400, detail="Usuario ya existe")
        user.usuario = new_usuario
    if new_clave:
        user.clave = get_password_hash(new_clave)
    if new_rol:
        user.rol = new_rol
    if new_estado is not None:
        user.estado = bool(new_estado)

    p = db.query(Persona).filter(Persona.id_persona == user.idpersona).first()
    if p:
        for (src, dst) in [
            ((body.get("dni") or (body.get("persona") or {}).get("dni")), "dni"),
            ((body.get("nombre") or (body.get("persona") or {}).get("nombre")), "nombre"),
            ((body.get("apellidos") or (body.get("persona") or {}).get("apellidos")), "apellidos"),
            ((body.get("telefono1") or body.get("telefono") or (body.get("persona") or {}).get("telefono1")), "telefono1"),
            ((body.get("telefono2") or body.get("whatsapp") or (body.get("persona") or {}).get("telefono2")), "telefono2"),
            ((body.get("distrito") or body.get("distritoAsignado") or (body.get("persona") or {}).get("distrito")), "distrito"),
            ((body.get("provincia") or body.get("provinciaAsignada") or (body.get("persona") or {}).get("provincia")), "provincia"),
            ((body.get("direccion") or (body.get("persona") or {}).get("direccion")), "direccion"),
        ]:
            if src is not None:
                setattr(p, dst, src)

    db.commit(); db.refresh(user)
    if p: db.refresh(p)
    return _serialize(user, p)


# ============================================================
# DELETE /api/usuarios/{id}
# ============================================================
@router.delete("/{id}")
def eliminar_usuario_bridge(
    id: int,
    db: Session = Depends(get_db),
    _: Usuarios = Depends(RoleChecker(["SUPER_ADMIN"])),
):
    user = db.query(Usuarios).filter(Usuarios.idusuario == id).first()
    if not user:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    idpersona = user.idpersona
    db.delete(user); db.commit()
    # borramos persona si no tiene mas usuarios ni solicitudes
    if (
        not db.query(Usuarios).filter(Usuarios.idpersona == idpersona).first()
        and not db.query(PersonerosUsers).filter(PersonerosUsers.id_persona == idpersona).first()
    ):
        p = db.query(Persona).filter(Persona.id_persona == idpersona).first()
        if p: db.delete(p); db.commit()
    return {"mensaje": "Usuario eliminado", "eliminado": True}


# ============================================================
# GET /api/usuarios/persona-by-dni/{dni}
# ============================================================
@router.get("/persona-by-dni/{dni}", response_model=PersonaDNILookupResponse)
def get_persona_by_dni(
    dni: str,
    db: Session = Depends(get_db),
    _: Usuarios = Depends(get_current_user),
):
    from src.schemas import PersonaResponse

    persona = db.query(Persona).filter(Persona.dni == dni).first()
    if not persona:
        return PersonaDNILookupResponse(
            encontrado=False,
            persona=None,
            id_solicitud_activa=None,
            id_solicitud_tipo=None,
            sugerencia_nombre=None,
            sugerencia_apellidos=None,
        )

    solicitudes = db.query(PersonerosUsers).filter(
        PersonerosUsers.id_persona == persona.id_persona,
        PersonerosUsers.estado_vinculacion.in_(["SOLICITADO", "ASIGNADO"])
    ).order_by(PersonerosUsers.fecha_registro.desc()).all()

    id_solicitud_activa = None
    id_solicitud_tipo = None
    if solicitudes:
        ultima = solicitudes[0]
        id_solicitud_activa = ultima.id_solicitud
        id_solicitud_tipo = ultima.tipo_personero_solicitado

    return PersonaDNILookupResponse(
        encontrado=True,
        persona=PersonaResponse(
            id_persona=persona.id_persona,
            dni=persona.dni,
            nombre=persona.nombre,
            apellidos=persona.apellidos,
            telefono1=persona.telefono1,
            telefono2=getattr(persona, "telefono2", None),
            correo=getattr(persona, "correo", None),
            distrito=persona.distrito,
            provincia=persona.provincia,
            direccion=getattr(persona, "direccion", None),
        ),
        id_solicitud_activa=id_solicitud_activa,
        id_solicitud_tipo=id_solicitud_tipo,
        sugerencia_nombre=None,
        sugerencia_apellidos=None,
    )
