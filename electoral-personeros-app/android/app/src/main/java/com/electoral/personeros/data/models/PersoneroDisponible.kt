package com.electoral.personeros.data.models

import com.google.gson.annotations.SerializedName

data class PersoneroDisponible(
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("apellidos") val apellidos: String = "",
    @SerializedName("dni") val dni: String = "",
    @SerializedName("telefono1") val telefono1: String = "",
    @SerializedName("id_solicitud") val id_solicitud: Int = 0,
    @SerializedName("idusuario") val idusuario: Int? = null,
    @SerializedName("id_usuario") val id_usuario: Int? = null,
    @SerializedName("idUsuario") val idUsuario: Int? = null,
    @SerializedName("id_persona") val id_persona: Int? = null,
    @SerializedName("tipo_personero") val tipo_personero: String? = null
) {
    val nombreCompleto: String get() = "$nombre $apellidos"
    val username: String get() = dni
    val id: Int? get() = id_solicitud.takeIf { it > 0 } ?: idusuario ?: id_usuario ?: idUsuario
    val idUsuarioSeguro: Int? get() = idusuario ?: id_usuario ?: idUsuario
    val telefono: String? get() = telefono1.ifBlank { null }
    val whatsapp: String? get() = telefono1.ifBlank { null }
    val provinciaAsignada: String? get() = null
    val distritoAsignado: String? get() = null
    val persona: Persona? get() = Persona(
        dni = dni,
        nombre = nombre,
        apellidos = apellidos,
        telefono1 = telefono1,
        distrito = "",
        provincia = ""
    )
}
