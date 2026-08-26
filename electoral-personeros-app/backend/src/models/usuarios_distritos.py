from sqlalchemy import Column, Integer, String, ForeignKey, UniqueConstraint
from sqlalchemy.orm import relationship

from src.config.database import Base


class UsuariosDistritos(Base):
    __tablename__ = "usuarios_distritos"
    __table_args__ = (
        UniqueConstraint("idusuario", "distrito", name="uq_usuario_distrito"),
    )

    id = Column(Integer, primary_key=True, index=True)
    idusuario = Column(Integer, ForeignKey("usuarios.idusuario", ondelete="CASCADE"), nullable=False, index=True)
    distrito = Column(String(100), nullable=False, index=True)

    usuario = relationship("Usuarios", back_populates="distritos_asignados")
