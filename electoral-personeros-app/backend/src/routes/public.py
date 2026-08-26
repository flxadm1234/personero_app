from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from src.config.database import get_db
from src.models.persona import Persona
from src.models.personeros_users import PersonerosUsers
from src.schemas import SolicitudPersoneroRequest

router = APIRouter(prefix="/api/public", tags=["Público"])


@router.post("/solicitud-personero", status_code=status.HTTP_201_CREATED)
def crear_solicitud_personero(
    solicitud: SolicitudPersoneroRequest,
    db: Session = Depends(get_db)
):
    persona_existente = db.query(Persona).filter(Persona.dni == solicitud.dni).first()

    if persona_existente:
        solicitud_existente = db.query(PersonerosUsers).filter(
            PersonerosUsers.id_persona == persona_existente.id_persona,
            PersonerosUsers.estado_vinculacion == "SOLICITADO"
        ).first()
        if solicitud_existente:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Ya existe una solicitud pendiente para este DNI"
            )
        persona = persona_existente
    else:
        persona = Persona(
            dni=solicitud.dni,
            nombre=solicitud.nombre,
            apellidos=solicitud.apellidos,
            telefono1=solicitud.telefono1,
            telefono2=solicitud.telefono2,
            correo=solicitud.correo,
            distrito=solicitud.distrito,
            provincia=solicitud.provincia,
            direccion=solicitud.direccion
        )
        db.add(persona)
        db.flush()

    nueva_solicitud = PersonerosUsers(
        id_persona=persona.id_persona,
        tipo_personero_solicitado=solicitud.tipo_personero_solicitado,
        estado_vinculacion="SOLICITADO"
    )
    db.add(nueva_solicitud)
    db.commit()
    db.refresh(nueva_solicitud)
    db.refresh(persona)

    return {
        "mensaje": "Solicitud enviada exitosamente. Nos pondremos en contacto pronto.",
        "solicitud": {
            "id_solicitud": nueva_solicitud.id_solicitud,
            "tipo_personero_solicitado": nueva_solicitud.tipo_personero_solicitado,
            "estado_vinculacion": nueva_solicitud.estado_vinculacion,
            "fecha_registro": nueva_solicitud.fecha_registro,
            "persona": {
                "id_persona": persona.id_persona,
                "dni": persona.dni,
                "nombre": persona.nombre,
                "apellidos": persona.apellidos
            }
        }
    }
