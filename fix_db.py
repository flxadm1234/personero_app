import sys, os
sys.path.insert(0, r"f:\SISTEMA DE PERSONERO\electoral-personeros-app\backend\src")
from config.database import engine
from sqlalchemy import text
with engine.connect() as c:
    res = c.execute(text("""UPDATE "PersonerosUsers" SET id_centro_asignado = NULL WHERE tipo_personero_solicitado = 'MESA' AND id_centro_asignado IS NOT NULL"""))
    print("UPDATE rows:", res.rowcount)
    c.commit()
    rows = c.execute(text("""SELECT id_solicitud, id_persona, tipo_personero_solicitado, id_mesa_asignada, id_centro_asignado, estado_vinculacion FROM "PersonerosUsers" WHERE tipo_personero_solicitado = 'MESA' ORDER BY id_solicitud""")).fetchall()
    for r in rows: print(r)
