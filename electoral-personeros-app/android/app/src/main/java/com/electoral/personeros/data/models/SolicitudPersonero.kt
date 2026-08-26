package com.electoral.personeros.data.models

import com.google.gson.annotations.SerializedName

data class SolicitudPersonero(
    @SerializedName("id_solicitud") val id_solicitud: Int = 0,
    @SerializedName("id_persona") val id_persona: Int = 0,
    @SerializedName("tipo_personero_solicitado") val tipo_personero_solicitado: String = "MESA",
    @SerializedName("id_centro_asignado") val id_centro_asignado: Int? = null,
    @SerializedName("id_mesa_asignada") val id_mesa_asignada: Int? = null,
    @SerializedName("estado_vinculacion") val estado_vinculacion: String = "SOLICITADO",
    @SerializedName("fecha_registro") val fecha_registro: String = "",
    @SerializedName("persona") val persona: Persona? = null
) {
    val id: Int? get() = id_solicitud
    val estado: String get() = estado_vinculacion
    val provinciaPreferida: String? get() = persona?.provincia
    val distritoPreferido: String? get() = persona?.distrito
    val experiencia: String? get() = null
    val disponibilidad: String? get() = null
    val estadoEnum: String get() = estado_vinculacion
}
