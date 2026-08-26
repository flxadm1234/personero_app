from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import logging

from src.config.database import engine, Base
from src.routes import auth, public, superadmin, admin, personero_centro
from src.routes.usuarios_bridge import router as usuarios_bridge_router
from src.routes.mesas_bridge import router as mesas_bridge_router
from src.routes import web_admin

logger = logging.getLogger("uvicorn.error")

try:
    Base.metadata.create_all(bind=engine)
    logger.info("Tablas SQLAlchemy creadas/verificadas correctamente.")
except Exception as e:
    logger.warning(
        "No se pudieron crear las tablas automaticamente "
        "(ejecuta PASO_1_CONFIGURAR_BD.bat primero): %s",
        str(e)
    )

app = FastAPI(
    title="Sistema Electoral de Personeros",
    description="Backend para la gestion y asignacion de personeros electorales - Region Loreto, Peru",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth)
app.include_router(public)
app.include_router(superadmin)
app.include_router(admin)
app.include_router(personero_centro)
app.include_router(usuarios_bridge_router)
app.include_router(mesas_bridge_router)
app.include_router(web_admin.router)


@app.get("/")
def root():
    return {
        "mensaje": "API Sistema Electoral de Personeros",
        "version": "1.0.0",
        "documentacion": "/docs",
        "health": "/api/health"
    }


@app.get("/api/health")
def health_check():
    return {"estado": "ok", "servicio": "Sistema Electoral de Personeros"}

