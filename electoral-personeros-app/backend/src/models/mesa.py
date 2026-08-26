from sqlalchemy import Column, Integer, String, Text, ForeignKey
from sqlalchemy.orm import relationship

from src.config.database import Base


class Mesa(Base):
    __tablename__ = "mesa"

    id_mesa = Column(Integer, primary_key=True, index=True)
    num_mesa = Column(String(10), unique=True, index=True, nullable=False)
    cod_local = Column(String(20), ForeignKey("centro_votacion.codigo_local"), nullable=True)
    codi_ubigeo = Column(String(10), nullable=True)
    nombre_local = Column(String(255), nullable=True)
    direccion_local = Column(Text, nullable=True)
    departamento = Column(String(100), nullable=True)
    provincia = Column(String(100), nullable=True)
    distrito = Column(String(100), nullable=True)

    centro_votacion = relationship("CentroVotacion", back_populates="mesas")
    personeros_asignados = relationship("PersonerosUsers", back_populates="mesa_asignada")
