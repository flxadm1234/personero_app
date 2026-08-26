from sqlalchemy import Column, Integer, String, Text
from sqlalchemy.orm import relationship

from src.config.database import Base


class CentroVotacion(Base):
    __tablename__ = "centro_votacion"

    id_centro = Column(Integer, primary_key=True, index=True)
    codigo_local = Column(String(20), unique=True, index=True, nullable=False)
    ubigeo = Column(String(10), nullable=False)
    nombre_local = Column(String(255), nullable=False)
    direccion_local = Column(Text, nullable=True)
    departamento = Column(String(100), nullable=False)
    provincia = Column(String(100), nullable=False)
    distrito = Column(String(100), nullable=False)

    mesas = relationship("Mesa", back_populates="centro_votacion")
    personeros_asignados = relationship("PersonerosUsers", back_populates="centro_asignado")
