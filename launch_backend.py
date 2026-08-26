import sys, os, traceback
_p = r"f:\SISTEMA DE PERSONERO\electoral-personeros-app\backend"
sys.path.insert(0, _p)
sys.path.insert(1, os.path.join(_p, "src"))
print("DEBUG cwd:", os.getcwd())
print("DEBUG path exists:", os.path.exists(_p))
print("DEBUG listdir backend: ", os.listdir(_p)[:15])
os.environ.setdefault("DB_HOST","localhost")
os.environ.setdefault("DB_PORT","5432")
os.environ.setdefault("DB_USER","postgres")
os.environ.setdefault("DB_PASSWORD","Admin123!")
os.environ.setdefault("DB_NAME","electoral_personeros")
os.environ.setdefault("SECRET_KEY","electoral_personeros_secret_key_2026_loreto_peru_muy_segura_XYZ123456789")
os.environ.setdefault("ALGORITHM","HS256")
os.environ.setdefault("ACCESS_TOKEN_EXPIRE_MINUTES","1440")
try:
    import main as _main
    print("OK main import. app attr exists:", hasattr(_main, "app"))
    _app = _main.app
except Exception as _e:
    traceback.print_exc()
    print("IMPORT MAIN FAILED:", repr(_e), file=sys.stderr)
    sys.exit(2)
from uvicorn import run
run(_app, host="0.0.0.0", port=8000, log_level="info")
