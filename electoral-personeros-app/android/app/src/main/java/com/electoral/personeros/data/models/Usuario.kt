package com.electoral.personeros.data.models

import com.google.gson.annotations.SerializedName

data class Usuario(
    @SerializedName("idusuario") val idusuario: Int = 0,
    @SerializedName("usuario") val usuario: String = "",
    @SerializedName("clave") val clave: String? = null,
    @SerializedName("rol") val rol: String = "",
    @SerializedName("fecha_creado") val fecha_creado: String = "",
    @SerializedName("estado") val estado: Boolean = true,
    @SerializedName("idpersona") val idpersona: Int = 0,
    @SerializedName("id_centro_asignado") val id_centro_asignado: Int? = null,
    @SerializedName("id_mesa_asignada") val id_mesa_asignada: Int? = null,
    @SerializedName("nombre_centro_asignado") val nombre_centro_asignado: String? = null,
    @SerializedName("persona") val persona: Persona? = null,

    @SerializedName("dni") val dni: String = "",
    @SerializedName("nombre") val nombre: String = "",
    @SerializedName("apellidos") val apellidos: String = "",
    @SerializedName("telefono1") val telefono1: String? = null,
    @SerializedName("telefono2") val telefono2: String? = null,
    @SerializedName("direccion") val direccion: String? = null,
    @SerializedName("provincia") val provincia: String? = null,
    @SerializedName("distrito") val distrito: String? = null,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("distritos_asignados") val distritos_asignados: List<String>? = null,

    @Transient val password: String? = null,
    @Transient val provinciaAsignada: String? = null,
    @Transient val distritoAsignado: String? = null
) {
    val nombreCompleto: String
        get() {
            if (nombre.isNotBlank() || apellidos.isNotBlank()) {
                return listOf(nombre, apellidos).filter { it.isNotBlank() }.joinToString(" ")
            }
            return persona?.nombreCompleto ?: usuario
        }

    val telefonoDisplay: String
        get() = telefono1 ?: persona?.telefono1 ?: ""

    val dniDisplay: String
        get() = dni.ifBlank { persona?.dni ?: "" }

    val direccionDisplay: String
        get() = (direccion ?: persona?.direccion).orEmpty()

    val provinciaDisplay: String
        get() = (provinciaAsignada ?: provincia ?: persona?.provincia).orEmpty()

    val distritoDisplay: String
        get() = (distritoAsignado ?: distrito ?: persona?.distrito).orEmpty()

    val correoDisplay: String?
        get() = correo ?: persona?.correo

    val distritosAsignadosDisplay: List<String>
        get() = distritos_asignados ?: emptyList()

    val id: Int?
        get() = idusuario

    val username: String
        get() = usuario

    val activo: Boolean
        get() = estado
}

data class UserResponseData(
    @SerializedName("idusuario") val idusuario: Int,
    @SerializedName("usuario") val usuario: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("idpersona") val idpersona: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("dni") val dni: String,
    @SerializedName("telefono1") val telefono1: String? = null,
    @SerializedName("provincia") val provincia: String? = null,
    @SerializedName("distrito") val distrito: String? = null,
    @SerializedName("correo") val correo: String? = null,
    @SerializedName("id_centro_asignado") val id_centro_asignado: Int? = null,
    @SerializedName("id_mesa_asignada") val id_mesa_asignada: Int? = null,
    @SerializedName("nombre_centro_asignado") val nombre_centro_asignado: String? = null,
    @SerializedName("distritos_asignados") val distritos_asignados: List<String>? = null
)
