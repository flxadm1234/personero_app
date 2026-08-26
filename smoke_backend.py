import os, sys, traceback
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

errors = []
for m in [
    "src.schemas",
    "src.config.database",
    "src.config.settings",
    "src.middlewares.auth",
    "src.models.persona",
    "src.models.usuario",
    "src.models.usuarios_distritos",
    "src.models.centro_votacion",
    "src.models.mesa",
    "src.models.personeros_users",
    "src.routes.public",
    "src.routes.auth",
    "src.routes.superadmin",
    "src.routes.admin",
    "src.routes.personero_centro",
    "src.routes.usuarios_bridge",
    "main",
]:
    try:
        __import__(m)
        print("OK", m)
    except Exception as e:
        errors.append((m, traceback.format_exc()))
        print("FAIL", m, ":", type(e).__name__, e)

if errors:
    print("\n=== FAILURES ===")
    for m, tb in errors:
        print(f"\n-- {m} --")
        print(tb)
    sys.exit(1)
else:
    print("\nALL MODULES OK")
