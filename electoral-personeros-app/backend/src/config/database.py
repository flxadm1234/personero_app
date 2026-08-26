from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
import urllib.parse

from src.config.settings import settings

DB_PASSWORD_ENCODED = urllib.parse.quote_plus(settings.DB_PASSWORD)
DATABASE_URL = (
    f"postgresql://{settings.DB_USER}:{DB_PASSWORD_ENCODED}"
    f"@{settings.DB_HOST}:{settings.DB_PORT}/{settings.DB_NAME}"
    f"?client_encoding=utf8"
)

engine = create_engine(
    DATABASE_URL,
    pool_pre_ping=True,
    connect_args={
        "options": "-c client_encoding=UTF8"
    }
)

SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

