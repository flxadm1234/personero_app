from sqlalchemy import Column, Integer, String, Text
from sqlalchemy.orm import relationship

from src.config.database import Base


class Persona(Base):
    __tablename__ = "persona"

    id_persona = Column(Integer, primary_key=True, index=True)
    dni = Column(String(8), unique=True, index=True, nullable=False)
    nombre = Column(String(100), nullable=False)
    apellidos = Column(String(100), nullable=False)
    telefono1 = Column(String(15), nullable=False)
    telefono2 = Column(String(15), nullable=True)
    distrito = Column(String(100), nullable=False)
    provincia = Column(String(100), nullable=False)
    direccion = Column(Text, nullable=True)
    correo = Column(String(150), nullable=True)

    usuarios = relationship("Usuarios", back_populates="persona", cascade="all, delete-orphan")
    personeros = relationship("PersonerosUsers", back_populates="persona")
