from datetime import datetime, timedelta
from typing import Optional, List
from jose import JWTError, jwt
from passlib.context import CryptContext
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from sqlalchemy.orm import Session
from pydantic import BaseModel

from src.config.settings import settings
from src.config.database import get_db
from src.models.usuario import Usuarios
from src.models.persona import Persona


pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/api/auth/login")


class TokenData(BaseModel):
    idusuario: Optional[int] = None
    usuario: Optional[str] = None
    rol: Optional[str] = None


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)


def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)


def create_access_token(data: dict, expires_delta: Optional[timedelta] = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)
    return encoded_jwt


def authenticate_user(db: Session, username: str, password: str):
    user = db.query(Usuarios).filter(Usuarios.usuario == username).first()
    if not user:
        return False
    if not verify_password(password, user.clave):
        return False
    if not user.estado:
        return False
    return user


def get_current_user(
    token: str = Depends(oauth2_scheme),
    db: Session = Depends(get_db)
) -> Usuarios:
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Credenciales inválidas",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
        idusuario: int = payload.get("idusuario")
        usuario: str = payload.get("sub")
        rol: str = payload.get("rol")
        if idusuario is None or usuario is None:
            raise credentials_exception
        token_data = TokenData(idusuario=idusuario, usuario=usuario, rol=rol)
    except JWTError:
        raise credentials_exception
    user = db.query(Usuarios).filter(Usuarios.idusuario == token_data.idusuario).first()
    if user is None:
        raise credentials_exception
    if not user.estado:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Usuario inactivo"
        )
    return user


def get_current_user_with_persona(
    current_user: Usuarios = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    persona = db.query(Persona).filter(Persona.id_persona == current_user.idpersona).first()
    return {
        "usuario": current_user,
        "persona": persona
    }


class RoleChecker:
    def __init__(self, allowed_roles: List[str]):
        self.allowed_roles = allowed_roles

    def __call__(self, current_user: Usuarios = Depends(get_current_user)):
        if current_user.rol not in self.allowed_roles:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="No tiene permisos para realizar esta acción"
            )
        return current_user
