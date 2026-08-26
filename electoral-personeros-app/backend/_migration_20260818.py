"""
Migracion 18/08/2026:
1) ALTER TABLE usuarios: id_centro_asignado + id_mesa_asignada FK
2) ALTER TABLE personeros_users: id_usuario_asignador, observaciones, fecha_asignacion
3) UNIQUE INDEX para no duplicar asignaciones MESA/CENTRO
4) SEED: 50 centros de votacion (Loreto real ubigeo 16xxxx) + 200 mesas
5) SEED: 15 solicitudes personeros_users con estado SOLICITADO de ejemplo
"""
import sys, os, random, string
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from src.config.database import SessionLocal, Base, engine
from src.models.centro_votacion import CentroVotacion
from src.models.mesa import Mesa
from src.models.persona import Persona
from src.models.personeros_users import PersonerosUsers
from sqlalchemy import text, inspect

DISTRITOS = {
    "MAYNAS": ["IQUITOS", "BELEN", "SAN JUAN BAUTISTA", "PUNCHANA", "SANTA ROSA"],
    "LORETO": ["NAUTA", "PARINARI", "TIGRE", "URARINAS"],
    "REQUENA": ["REQUENA", "CAPELO", "JENARO HERRERA", "SAQUENA", "VILLA TUNARí"],
    "ALTO AMAZONAS": ["YURIMAGUAS", "BARRANCA", "SANTA CRUZ", "SUCHIPALCA"],
    "UCAYALI": ["CONTAMANA", "INAHUAYA", "PADRE MARQUEZ", "VARGAS GUERRA"],
    "DATEM DEL MARAÑON": ["BARRANCA", "CUYUDAD", "IMAZA", "MANSERICHE", "MORONA", "PASTAZA"],
    "MAZAN": ["MAZAN", "NAPO", "TIERRA BLANCA"],
    "RAMON CASTILLA": ["CABALLOCOCHA", "Pebas", "MARISCAL RAMON CASTILLA", "YAVARI"],
    "RECUENAY": ["ALTO NANAY", "CORONEL PORTILLO", "GENERAL JUAN", "SAN PABLO"],
    "PUTUMAYO": ["PUTUMAYO", "SAENZ PENA", "VALLE DEL GUALLAGA"],
}

NOMBRES_CENTROS = [
    "IE 60071 ANDRES ALFONSO CARDO FRANCO",
    "IE N°60074 CLAUDIANA DEL AGUILA DE PANDURO",
    "60010 VALENTIN PANIAGUA CORAZAO",
    "60022 DIEGO NATAL JUAN",
    "731 CALLE 6 DE OCTUBRE",
    "CETPRO IQUITOS",
    "CETPRO SANTO CRISTO DE BAGAZAN",
    "IE.I N°170 DIVINO NIÑO JESUS",
    "IE 60026 RUY GUZMAN HIDALGO",
    "IE 601534 SAN LUCAS",
    "60192 9 DE OCTUBRE",
    "IE 60993 RAMON CASTILLA Y MARQUESADO",
    "IE 61006 SARA ALICIA SABERBEIN PINEDO",
    'IE N°708 "ARBOLITO DEL SABER"',
    "IE SAGRADA FAMILIA",
    "IEI 314 LOS AMIGUITOS DE JESUS",
    'IEI N°397 "CORAZON DE JESUS"',
    'IFI N°835 "ESTRELLITAS DE JESUS"',
    "I.E. Nº 60100 SANTA CRUZ DEL VALLE",
    "IEI 480 NIÑO JESUS DE PRAGA",
    "60088 MIGUEL GRAU",
    "IE 60044 MERCEDES CABELLO DE CARBONERAS",
    "60123 SAGRADOS CORAZONES",
    "90001 CESAR VALLEJO MENDOZA",
    "IE ALMIRANTE MIGUEL GRAU",
    "514 SAN MARTIN DE PORRES",
    "884 MARISCAL BENAVIDES",
    "IE AUGUSTO CASTRO PINTO",
    "N° 15591 JOFA SANGAMA",
    "IEI CRISTO REY",
    "IE PIO XII",
    "IE NUESTRA SEÑORA DE FATIMA",
    "622 MARIANO MELGAR",
    "IE 60010 - 161",
    "60011 DAMASO LABERGE",
    "60012 317",
    "348 DELIA PEREA TORRES",
    "530 AV DE LA HERRADURA",
    "548 MI MUNDO INFANTIL",
    "62000 ESCUELA MODELO",
]

CALLES = [
    "CALLE SANTA ROSA SN",
    "AV INDIANA CON DAMAZO LABERGE SN",
    "CALLE DAMASO LABERGE SN",
    "CALLE CORNEJO PORTUGAL 1689",
    "CALLE 20 DE AGOSTO SN",
    "CALLE LOS ROSALES SN",
    "AV PARTICIPACION 584",
    "AV GUARDIA REPUBLICANA 100",
    "CALLE 7 SN",
    "CALLE PRIMAVERA SN MANKO INKA",
    "CALLE UNION 347",
    "CALLE MANUEL IBERICO SN",
    "CALLE PROGRESO 702",
    "AV. MALECO TARAPACA SN",
    "JR. HUALLAGA 234",
    "AV. TACNA 123",
    "CARRETERA A NAUTA KM 4.5",
    "CALLE 9 DE DICIEMBRE 456",
    "AV. GRAU 890",
    "CENTRO POBLADO NUEVO HORIZONTE",
]


def run():
    db = SessionLocal()
    try:
        print("[1/5] Ejecutando ALTER TABLE ...")
        # ---- USUARIOS: agregar col si no existe
        insp = inspect(engine)
        cols_usuarios = {c["name"] for c in insp.get_columns("usuarios")}
        if "id_centro_asignado" not in cols_usuarios:
            db.execute(text("""
                ALTER TABLE usuarios ADD COLUMN id_centro_asignado INTEGER
                REFERENCES centro_votacion(id_centro) ON DELETE SET NULL;
            """))
            print("  + usuarios.id_centro_asignado OK")
        else:
            print("  = usuarios.id_centro_asignado YA EXISTE")
        if "id_mesa_asignada" not in cols_usuarios:
            db.execute(text("""
                ALTER TABLE usuarios ADD COLUMN id_mesa_asignada INTEGER
                REFERENCES mesa(id_mesa) ON DELETE SET NULL;
            """))
            print("  + usuarios.id_mesa_asignada OK")
        else:
            print("  = usuarios.id_mesa_asignada YA EXISTE")

        cols_pu = {c["name"] for c in insp.get_columns("personeros_users")}
        for col, ddl in [
            ("id_usuario_asignador", "ALTER TABLE personeros_users ADD COLUMN id_usuario_asignador INTEGER REFERENCES usuarios(idusuario) ON DELETE SET NULL;"),
            ("observaciones", "ALTER TABLE personeros_users ADD COLUMN observaciones TEXT;"),
            ("fecha_asignacion", "ALTER TABLE personeros_users ADD COLUMN fecha_asignacion TIMESTAMP;"),
        ]:
            if col not in cols_pu:
                db.execute(text(ddl))
                print(f"  + personeros_users.{col} OK")
            else:
                print(f"  = personeros_users.{col} YA EXISTE")

        try:
            db.execute(text("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_mesa_asignada_activa
                ON personeros_users(id_mesa_asignada)
                WHERE estado_vinculacion = 'ASIGNADO' AND id_mesa_asignada IS NOT NULL;
            """))
            db.execute(text("""
                CREATE UNIQUE INDEX IF NOT EXISTS ux_centro_asignado_activo
                ON personeros_users(id_centro_asignado)
                WHERE estado_vinculacion = 'ASIGNADO' AND id_centro_asignado IS NOT NULL;
            """))
            print("  + UNIQUE INDEX asignaciones OK")
        except Exception as e:
            print(f"  (idx warn: {e})")

        db.commit()

        # ====================================================
        print("\n[2/5] Insertando 50 centros de votacion Loreto (ubigeo 16xxxx) ...")
        existing_count = db.query(CentroVotacion).count()
        print(f"  centros actuales: {existing_count}")
        if existing_count < 30:
            random.seed(42)
            centros = []
            idx = 1
            for provincia, distritos in sorted(DISTRITOS.items()):
                for distrito in distritos:
                    if len(centros) >= 50:
                        break
                    ubigeo_prov = list(DISTRITOS.keys()).index(provincia) + 1
                    ubigeo_dist = distritos.index(distrito) + 1
                    ubigeo = f"16{ubigeo_prov:02d}{ubigeo_dist:02d}"
                    for i in range(random.randint(1, 3)):
                        if len(centros) >= 50:
                            break
                        base_nombre = random.choice(NOMBRES_CENTROS)
                        nombre_local = f"{base_nombre} - {distrito[:3].upper()}{idx:03d}"
                        codigo_local = f"L{idx:04d}"
                        while db.query(CentroVotacion).filter(CentroVotacion.codigo_local == codigo_local).first():
                            idx += 1
                            codigo_local = f"L{idx:04d}"
                        c = CentroVotacion(
                            codigo_local=codigo_local,
                            ubigeo=ubigeo,
                            nombre_local=nombre_local,
                            direccion_local=random.choice(CALLES),
                            departamento="LORETO",
                            provincia=provincia,
                            distrito=distrito,
                        )
                        centros.append(c); idx += 1
            for c in centros:
                db.add(c)
            db.commit()
            print(f"  + INSERTADOS {len(centros)} centros. Total ahora: {db.query(CentroVotacion).count()}")
        else:
            print(f"  = Skip insercion de centros (ya hay {existing_count})")

        # ====================================================
        print("\n[3/5] Insertando mesas asociadas a los centros ...")
        total_mesas = db.query(Mesa).count()
        print(f"  mesas actuales: {total_mesas}")
        if total_mesas < 100:
            mesas = []
            all_centros = db.query(CentroVotacion).order_by(CentroVotacion.id_centro).all()
            num_mesa_counter = 60000 + len(mesas)
            for cv in all_centros:
                n_mesas = random.randint(3, 8)
                for i in range(n_mesas):
                    num_mesa_counter += 1
                    m = Mesa(
                        num_mesa=str(num_mesa_counter),
                        cod_local=cv.codigo_local,
                        codi_ubigeo=cv.ubigeo,
                        nombre_local=cv.nombre_local,
                        direccion_local=cv.direccion_local,
                        departamento=cv.departamento,
                        provincia=cv.provincia,
                        distrito=cv.distrito,
                    )
                    mesas.append(m)
            for m in mesas:
                db.add(m)
            db.commit()
            print(f"  + INSERTADAS {len(mesas)} mesas. Total ahora: {db.query(Mesa).count()}")
        else:
            print(f"  = Skip insercion mesas (ya hay {total_mesas})")

        # ====================================================
        print("\n[4/5] Insertando 15 solicitudes de postulantes SOLICITADO ...")
        total_pu = db.query(PersonerosUsers).count()
        print(f"  solicitudes actuales: {total_pu}")
        if total_pu < 10:
            from datetime import datetime
            nombres = ["LUIS","MARIA","JOSE","ANA","PEDRO","SOFIA","CARLOS","VALERIA","MIGUEL","PAOLA","ANDRES","CAMILA","DIEGO","LUCIA","JESUS","ROSA","JUAN","FABIOLA","GABRIEL","MIRIAM"]
            apellidos = ["TORRES","RUIZ","FLORES","DIAZ","PEREZ","GARCIA","GONZALES","RODRIGUEZ","MENDOZA","SANCHEZ","SILVA","CARRASCO","VASQUEZ","TAPIA","ROMERO","RIVAS","PANDURO","RAMIREZ","CASTRO","HIDALGO"]
            provincias_combos = [("MAYNAS","IQUITOS"),("MAYNAS","BELEN"),("MAYNAS","SAN JUAN BAUTISTA"),("LORETO","NAUTA"),("REQUENA","REQUENA"),("ALTO AMAZONAS","YURIMAGUAS")]
            soli = 0
            dni_counter = 10000000
            for i in range(15):
                dni = str(70000000 + i + 1)
                while db.query(Persona).filter(Persona.dni == dni).first():
                    dni = str(71000000 + i * 137 + random.randint(1, 999))[:8]
                provincia, distrito = random.choice(provincias_combos)
                nom = random.choice(nombres)
                ape1, ape2 = random.choice(apellidos), random.choice(apellidos)
                apellidos_full = f"{ape1} {ape2}"
                tel = "9" + "".join([str(random.randint(0,9)) for _ in range(8)])
                p = Persona(
                    dni=dni, nombre=nom, apellidos=apellidos_full,
                    telefono1=tel, telefono2=None,
                    distrito=distrito, provincia=provincia,
                    direccion=random.choice(CALLES),
                )
                db.add(p); db.flush()
                tipo = "MESA" if soli % 3 != 0 else "CENTRO"
                pu = PersonerosUsers(
                    id_persona=p.id_persona,
                    tipo_personero_solicitado=tipo,
                    estado_vinculacion="SOLICITADO",
                    fecha_registro=datetime.utcnow(),
                )
                db.add(pu); soli += 1
            db.commit()
            print(f"  + INSERTADAS {soli} solicitudes. Total: {db.query(PersonerosUsers).count()}")
        else:
            print(f"  = Skip solicitudes (ya hay {total_pu})")

        # ====================================================
        print("\n[5/5] Resumen general de la BD:")
        for cls, label in [
            (Persona, "persona"),
            (CentroVotacion, "centro_votacion"),
            (Mesa, "mesa"),
            (PersonerosUsers, "personeros_users (solicitudes)"),
        ]:
            print(f"  {label:<35} -> {db.query(cls).count():>5} filas")
        from src.models.usuario import Usuarios
        print(f"  {'usuarios (usuarios del sistema)':<35} -> {db.query(Usuarios).count():>5} filas")
        roles = db.query(Usuarios.rol, db.func.count(Usuarios.idusuario)).group_by(Usuarios.rol).all()
        print("  roles: ", roles)

        print("\n✅ MIGRACION Y SEED EXITOSOS!")
    except Exception as e:
        db.rollback()
        print(f"[ERROR ROLLBACK]: {e}")
        import traceback; traceback.print_exc()
        raise
    finally:
        db.close()


if __name__ == "__main__":
    run()
