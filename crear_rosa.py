import sys, os
sys.path.insert(0, r"f:\SISTEMA DE PERSONERO\electoral-personeros-app\backend")
sys.path.insert(1, os.path.join(r"f:\SISTEMA DE PERSONERO\electoral-personeros-app\backend", "src"))
os.environ.setdefault("DB_HOST","localhost")
os.environ.setdefault("DB_PORT","5432")
os.environ.setdefault("DB_NAME","electoral_personeros")
os.environ.setdefault("DB_USER","postgres")
os.environ.setdefault("DB_PASSWORD","Admin123!")
os.environ.setdefault("SECRET_KEY","electoral_personeros_secret_key_2026_loreto_peru_muy_segura_XYZ123456789")
from src.config.database import SessionLocal
from src.models.entities import Usuarios, PersonerosUsers, SolicitudesPersoneros, UsuariosCredentials
from passlib.context import CryptContext
import datetime
pwd = CryptContext(schemes=["bcrypt"], deprecated="auto")
db = SessionLocal()
try:
    # find user by DNI 998393011 (ROSA PALACIOS - approved, without assignment hopefully)
    rosa = db.query(Usuarios).filter(Usuarios.dni=="998393011").first()
    if not rosa:
        print("Creating Rosa user")
        rosa = Usuarios(dni="998393011", nombres="ROSA", apellidos="PALACIOS QUISPE", email="rosa.palacios@example.com",
                        telefono_movil="965123789", rol="PERSONERO_MESA", fecha_registro=datetime.datetime.now(),
                        provincia="MAYNAS", distrito="IQUITOS", direccion="Av. Quiñones km 2.5",
                        tipo_persona="NATURAL", nivel_educativo="TECNICO")
        db.add(rosa); db.flush()
        cred = UsuariosCredentials(idusuario=rosa.idusuario, nombre_usuario="rosa998", password_hash=pwd.hash("Electoral2026!"),
                                   estado="ACTIVO", fecha_ultimo_ingreso=None, intentos_fallidos=0, requiere_cambio_password=False)
        db.add(cred)
    else:
        print(f"Rosa exists idusuario={rosa.idusuario} rol={rosa.rol}")
    # find if personeros_users entry exists
    pu = db.query(PersonerosUsers).filter(PersonerosUsers.idusuario == rosa.idusuario).first()
    if not pu:
        print("Creating PersonerosUsers entry for Rosa")
        pu = PersonerosUsers(idusuario=rosa.idusuario, id_centro_asignado=None, id_mesa_asignada=None,
                             codigo_postulante="POS-998-011", estado_vinculacion="PENDIENTE",
                             fecha_solicitud=datetime.datetime.now())
        db.add(pu); db.flush()
    else:
        print(f"Rosa PersonerosUsers id={pu.id_personero_user} estado={pu.estado_vinculacion} "
              f"id_centro={pu.id_centro_asignado} id_mesa={pu.id_mesa_asignada}")
    # find solicitud aprobada for Rosa
    sol = db.query(SolicitudesPersoneros).filter(SolicitudesPersoneros.idusuario == rosa.idusuario).first()
    if not sol:
        print("Creating Solicitud Aprobada for Rosa")
        sol = SolicitudesPersoneros(idusuario=rosa.idusuario, tipo_solicitud="ASIGNACION_MESA",
                                    provincia_solicitud="MAYNAS", distrito_solicitud="IQUITOS",
                                    centro_solicitado_id=1,
                                    estado="APROBADA", fecha_solicitud=datetime.datetime.now(),
                                    fecha_revision=datetime.datetime.now(),
                                    id_revisor=1, motivo_revision="Aprobado automaticamente para test E2E")
        db.add(sol); db.flush()
    else:
        print(f"Solicitud exists id={sol.id_solicitud} estado={sol.estado}")
        if sol.estado != "APROBADA":
            sol.estado = "APROBADA"; sol.fecha_revision = datetime.datetime.now(); sol.id_revisor=1
    # Ensure Rosa is not assigned: reset id_mesa and id_centro
    pu.id_mesa_asignada = None
    pu.id_centro_asignado = None
    pu.estado_vinculacion = "PENDIENTE"  # will be updated to ASIGNADO on success
    db.commit()
    print(f"\nOK Rosa ready for Mesa assignment: idusuario={rosa.idusuario} id_solicitud={sol.id_solicitud} id_personero_user={pu.id_personero_user}")
except Exception as e:
    db.rollback(); raise
finally:
    db.close()
