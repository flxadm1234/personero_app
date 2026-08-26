from sqlalchemy import Column, Integer, String, DateTime, ForeignKey, Text
from sqlalchemy.orm import relationship
from datetime import datetime

from src.config.database import Base


class PersonerosUsers(Base):
    __tablename__ = "personeros_users"

    id_solicitud = Column(Integer, primary_key=True, index=True)
    id_persona = Column(Integer, ForeignKey("persona.id_persona"), nullable=True)
    tipo_personero_solicitado = Column(String(30), nullable=True)
    id_centro_asignado = Column(Integer, ForeignKey("centro_votacion.id_centro"), nullable=True)
    id_mesa_asignada = Column(Integer, ForeignKey("mesa.id_mesa"), nullable=True)
    id_usuario_asignador = Column(Integer, ForeignKey("usuarios.idusuario"), nullable=True)
    estado_vinculacion = Column(String(20), default="SOLICITADO")
    observaciones = Column(Text, nullable=True)
    fecha_registro = Column(DateTime, default=datetime.utcnow)
    fecha_asignacion = Column(DateTime, nullable=True)

    persona = relationship("Persona", back_populates="personeros")
    centro_asignado = relationship("CentroVotacion", back_populates="personeros_asignados")
    mesa_asignada = relationship("Mesa", back_populates="personeros_asignados")
