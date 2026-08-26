import sys, os
sys.path.insert(0, r"f:\SISTEMA DE PERSONERO\electoral-personeros-app\backend")
sys.path.insert(1, os.path.join(r"f:\SISTEMA DE PERSONERO\electoral-personeros-app\backend", "src"))
os.environ.setdefault("DB_HOST","localhost")
os.environ.setdefault("DB_PORT","5432")
os.environ.setdefault("DB_NAME","electoral_personeros")
os.environ.setdefault("DB_USER","postgres")
os.environ.setdefault("DB_PASSWORD","Admin123!")
from src.config.database import SessionLocal
from src.models.personeros_users import PersonerosUsers
from src.models.persona import Persona
from sqlalchemy import func

db = SessionLocal()
try:
    q = (db.query(PersonerosUsers, Persona)
         .outerjoin(Persona, PersonerosUsers.id_persona == Persona.id_persona))
    print(f"Total rows personeros_users: {q.count()}")
    print("\nAll rows:")
    for pu, p in q.all():
        nom = f"{p.nombre} {p.apellidos}" if p else f"id_persona={pu.id_persona}"
        dni = p.dni if p else "?"
        print(f"  id_solicitud={pu.id_solicitud:3d}  tipo={pu.tipo_personero_solicitado!s:8s}  estado={pu.estado_vinculacion!s:12s}  "
              f"id_centro={pu.id_centro_asignado!s:6s}  id_mesa={pu.id_mesa_asignada!s:6s}  "
              f"{nom:40s} dni={dni}")
    print("\nCounts by estado:")
    for estado, cnt in (db.query(PersonerosUsers.estado_vinculacion, func.count())
                        .group_by(PersonerosUsers.estado_vinculacion).all()):
        print(f"  {estado!r} = {cnt}")
finally:
    db.close()
