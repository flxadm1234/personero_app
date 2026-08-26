package com.electoral.personeros.data.models

import com.google.gson.annotations.SerializedName

data class AsignarMesaRequest(
    @SerializedName("idMesa") val idMesa: Int? = null,
    @SerializedName("id_mesa") val id_mesa: Int? = null,
    @SerializedName("idSolicitud") val idSolicitud: Int? = null,
    @SerializedName("id_solicitud") val id_solicitud: Int? = null,
    @SerializedName("idUsuario") val idUsuario: Int? = null,
    @SerializedName("id_usuario") val id_usuario: Int? = null,
    @SerializedName("idusuario") val idusuario: Int? = null,
    @SerializedName("idCentro") val idCentro: Int? = null,
    @SerializedName("id_centro") val id_centro: Int? = null,
    @SerializedName("id_solicitud_personero_mesa") val id_solicitud_personero_mesa: Int? = null,
    @SerializedName("id_centro_votacion") val id_centro_votacion: Int? = null,
    @SerializedName("observaciones") val observaciones: String? = null
)

data class AsignarCentroRequest(
    @SerializedName("idCentro") val idCentro: Int? = null,
    @SerializedName("id_centro") val id_centro: Int? = null,
    @SerializedName("idSolicitud") val idSolicitud: Int? = null,
    @SerializedName("id_solicitud") val id_solicitud: Int? = null,
    @SerializedName("idUsuario") val idUsuario: Int? = null,
    @SerializedName("id_usuario") val id_usuario: Int? = null,
    @SerializedName("idusuario") val idusuario: Int? = null,
    @SerializedName("observaciones") val observaciones: String? = null
)

data class DesasignarMesaRequest(
    @SerializedName("idMesa") val idMesa: Int? = null,
    @SerializedName("id_mesa") val id_mesa: Int? = null
)

data class RegistrarYAsignarMesaRequest(
    @SerializedName("dni") val dni: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("apellidos") val apellidos: String = "",
    @SerializedName("telefono1") val telefono1: String? = null,
    @SerializedName("telefono2") val telefono2: String? = null,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("provincia") val provincia: String? = null,
    @SerializedName("distrito") val distrito: String? = null,
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("id_centro") val id_centro: Int? = null,
    @SerializedName("id_mesa") val id_mesa: Int = 0,
    @SerializedName("observaciones") val observaciones: String? = null
)

data class PersonaDNILookupResponse(
    @SerializedName("encontrado") val encontrado: Boolean = false,
    @SerializedName("persona") val persona: Persona? = null,
    @SerializedName("id_solicitud_activa") val id_solicitud_activa: Int? = null,
    @SerializedName("id_solicitud_tipo") val id_solicitud_tipo: String? = null,
    @SerializedName("sugerencias") val sugerencias: List<Persona>? = null
)

data class DistritoAsignadoItem(
    @SerializedName("distrito") val distrito: String = "",
    @SerializedName("provincia") val provincia: String? = null
)

data class AdminDistritosUpdateRequest(
    @SerializedName("distritos") val distritos: List<String> = emptyList()
)

data class PerfilUsuarioCompletoResponse(
    @SerializedName("idusuario") val idusuario: Int = 0,
    @SerializedName("usuario") val usuario: String = "",
    @SerializedName("rol") val rol: String = "",
    @SerializedName("estado") val estado: Boolean = true,
    @SerializedName("idpersona") val idpersona: Int = 0,
    @SerializedName("persona") val persona: Persona? = null,
    @SerializedName("id_centro_asignado") val id_centro_asignado: Int? = null,
    @SerializedName("centro_asignado") val centro_asignado: CentroVotacion? = null,
    @SerializedName("id_mesa_asignada") val id_mesa_asignada: Int? = null,
    @SerializedName("mesa_asignada") val mesa_asignada: Mesa? = null,
    @SerializedName("distritos_asignados") val distritos_asignados: List<DistritoAsignadoItem> = emptyList()
)
