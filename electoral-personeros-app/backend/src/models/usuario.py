from sqlalchemy import Column, Integer, String, Boolean, DateTime, ForeignKey
from sqlalchemy.orm import relationship
from datetime import datetime

from src.config.database import Base


class Usuarios(Base):
    __tablename__ = "usuarios"

    idusuario = Column(Integer, primary_key=True, index=True)
    idpersona = Column(Integer, ForeignKey("persona.id_persona", ondelete="CASCADE"), nullable=False)
    usuario = Column(String(50), unique=True, index=True, nullable=False)
    clave = Column(String(255), nullable=False)
    rol = Column(String(30), nullable=False)
    fecha_creado = Column(DateTime, default=datetime.utcnow)
    estado = Column(Boolean, default=True)
    id_centro_asignado = Column(Integer, ForeignKey("centro_votacion.id_centro", ondelete="SET NULL"), nullable=True)
    id_mesa_asignada = Column(Integer, ForeignKey("mesa.id_mesa", ondelete="SET NULL"), nullable=True)

    persona = relationship("Persona", back_populates="usuarios")
    centro_asignado = relationship("CentroVotacion", foreign_keys=[id_centro_asignado])
    mesa_asignada = relationship("Mesa", foreign_keys=[id_mesa_asignada])
    distritos_asignados = relationship("UsuariosDistritos", back_populates="usuario", cascade="all, delete-orphan")
