package com.electoral.personeros.data.models

import com.google.gson.annotations.SerializedName

data class Persona(
    @SerializedName("id_persona") val id_persona: Int = 0,
    @SerializedName("dni") val dni: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("apellidos") val apellidos: String = "",
    @SerializedName("telefono1") val telefono1: String = "",
    @SerializedName("telefono2") val telefono2: String? = null,
    @SerializedName("distrito") val distrito: String = "",
    @SerializedName("provincia") val provincia: String = "",
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("correo") val correo_real: String? = null
) {
    val nombreCompleto: String = "$nombre $apellidos"
    val nombres: String get() = nombre
    val telefono: String? get() = telefono1.ifBlank { null }
    val whatsapp: String? get() = telefono2 ?: telefono1.ifBlank { null }
    val correo: String? get() = correo_real
    val corregimiento: String? get() = null
}
