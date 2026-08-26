import os, sys
sys.path.insert(0, os.path.join(os.path.dirname(__file__), "electoral-personeros-app", "backend"))
sys.path.insert(1, os.path.join(os.path.dirname(__file__), "electoral-personeros-app", "backend", "src"))

os.environ.setdefault("DB_HOST","localhost")
os.environ.setdefault("DB_PORT","5432")
os.environ.setdefault("DB_USER","postgres")
os.environ.setdefault("DB_PASSWORD","Admin123!")
os.environ.setdefault("DB_NAME","electoral_personeros")
os.environ.setdefault("SECRET_KEY","electoral_personeros_secret_key_2026_loreto_peru_muy_segura_XYZ123456789")
os.environ.setdefault("ALGORITHM","HS256")
os.environ.setdefault("ACCESS_TOKEN_EXPIRE_MINUTES","1440")

from src.config.database import engine
from sqlalchemy import text

sql_path = os.path.join(os.path.dirname(__file__), "electoral-personeros-app", "backend", "_migration_20260819_distritos_correo.sql")
sql = open(sql_path, encoding="utf-8").read()

with engine.begin() as conn:
    for stmt in [s.strip() for s in sql.split(";") if s.strip()]:
        print("Running:", stmt[:120])
        conn.execute(text(stmt))
print("Migration OK")
