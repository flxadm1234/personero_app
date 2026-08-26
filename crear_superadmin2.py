import sys, os
_p = r"f:\SISTEMA DE PERSONERO\electoral-personeros-app\backend"
sys.path.insert(0, _p)
sys.path.insert(1, os.path.join(_p, "src"))
os.environ.setdefault("DB_HOST","localhost")
os.environ.setdefault("DB_PORT","5432")
os.environ.setdefault("DB_USER","postgres")
os.environ.setdefault("DB_PASSWORD","Admin123!")
os.environ.setdefault("DB_NAME","electoral_personeros")
os.environ.setdefault("SECRET_KEY","electoral_personeros_secret_key_2026_loreto_peru_muy_segura_XYZ123456789")
os.environ.setdefault("ALGORITHM","HS256")
os.environ.setdefault("ACCESS_TOKEN_EXPIRE_MINUTES","1440")

from src.config.database import SessionLocal
from src.models.persona import Persona
from src.models.usuario import Usuarios
from src.models.usuarios_distritos import UsuariosDistritos
from src.models.centro_votacion import CentroVotacion
from src.models.mesa import Mesa
from src.models.personeros_users import PersonerosUsers
from src.middlewares.auth import pwd_context

NUEVO_USUARIO = "superadmin2"
NUEVA_CLAVE  = "Admin123456"
NUEVO_DNI    = "99999999"
NOMBRE       = "Super"
APELLIDOS    = "Administrador Dos"
TELEFONO     = "900000000"
PROVINCIA    = "Maynas"
DISTRITO     = "Iquitos"
DIRECCION    = "Sede Central Loreto"
CORREO       = "superadmin2@electoral.local"
ROL          = "SUPER_ADMIN"

db = SessionLocal()
try:
    existe_usuario = db.query(Usuarios).filter(Usuarios.usuario == NUEVO_USUARIO).first()
    if existe_usuario:
        print(f"[OK] El usuario '{NUEVO_USUARIO}' YA EXISTE. idusuario={existe_usuario.idusuario} | estado={existe_usuario.estado} | rol={existe_usuario.rol}")
    else:
        existe_persona = db.query(Persona).filter(Persona.dni == NUEVO_DNI).first()
        if existe_persona:
            p = existe_persona
            print(f"[INFO] DNI {NUEVO_DNI} ya existe en persona id_persona={p.id_persona}. Reutilizando.")
        else:
            p = Persona(
                dni=NUEVO_DNI, nombre=NOMBRE, apellidos=APELLIDOS,
                telefono1=TELEFONO, telefono2=None,
                distrito=DISTRITO, provincia=PROVINCIA,
                direccion=DIRECCION, correo=CORREO
            )
            db.add(p)
            db.flush()
            print(f"[OK] Persona NUEVA creada: id_persona={p.id_persona} dni={p.dni}")

        clave_hash = pwd_context.hash(NUEVA_CLAVE)
        u = Usuarios(
            idpersona=p.id_persona,
            usuario=NUEVO_USUARIO,
            clave=clave_hash,
            rol=ROL,
            estado=True
        )
        db.add(u)
        db.commit()
        db.refresh(u)
        print(f"\n[SUCCESS] SUPER ADMIN CREADO EXITOSAMENTE")
        print(f"  URL base API      : http://192.168.1.179:8000 (WiFi) o http://10.0.2.2:8000 (Emulador)")
        print(f"  Usuario           : {NUEVO_USUARIO}")
        print(f"  Contraseña        : {NUEVA_CLAVE}")
        print(f"  Rol               : {ROL}")
        print(f"  idusuario         : {u.idusuario}")
        print(f"  estado            : ACTIVA")
except Exception as e:
    db.rollback()
    import traceback
    traceback.print_exc()
    print(f"[ERROR] Fallo creacion superadmin: {repr(e)}", file=sys.stderr)
    sys.exit(1)
finally:
    db.close()
