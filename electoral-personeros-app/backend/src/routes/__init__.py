from src.routes import auth as auth_module
from src.routes import public as public_module
from src.routes import superadmin as superadmin_module
from src.routes import admin as admin_module
from src.routes import personero_centro as personero_centro_module

auth = auth_module.router
public = public_module.router
superadmin = superadmin_module.router
admin = admin_module.router
personero_centro = personero_centro_module.router

__all__ = [
    "auth",
    "public",
    "superadmin",
    "admin",
    "personero_centro",
]

