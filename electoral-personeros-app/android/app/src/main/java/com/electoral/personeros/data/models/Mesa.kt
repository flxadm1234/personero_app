package com.electoral.personeros.data.models

import com.google.gson.annotations.SerializedName

data class PersoneroMini(
    @SerializedName("idusuario") val idusuario: Int? = null,
    @SerializedName("idusuario_id") val idusuario_id: Int? = null,
    @SerializedName("id_solicitud") val id_solicitud: Int? = null,
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("apellidos") val apellidos: String? = null,
    @SerializedName("nombre_completo") val nombre_completo: String? = null,
    @SerializedName("dni") val dni: String? = null,
    @SerializedName("telefono1") val telefono1: String? = null
) {
    val nombreSeguro: String get() = nombre ?: nombre_completo?.split(" ")?.firstOrNull() ?: ""
    val apellidosSeguro: String get() = apellidos ?: nombre_completo?.split(" ", limit = 2)?.getOrNull(1) ?: ""
    val nombreCompleto: String get() = nombre_completo ?: if (!nombre.isNullOrBlank() || !apellidos.isNullOrBlank()) "$nombreSeguro $apellidosSeguro".trim() else ""
}

data class Mesa(
    @SerializedName("id_mesa") val id_mesa: Int = 0,
    @SerializedName("num_mesa") val num_mesa: String? = null,
    @SerializedName("cod_local") val cod_local: String? = null,
    @SerializedName("codi_ubigeo") val codi_ubigeo: String? = null,
    @SerializedName("nombre_local") val nombre_local: String? = null,
    @SerializedName("direccion_local") val direccion_local: String? = null,
    @SerializedName("departamento") val departamento: String? = null,
    @SerializedName("provincia") val provincia: String? = null,
    @SerializedName("distrito") val distrito: String? = null,
    @SerializedName("estado") val estadoApi: String? = null,
    @SerializedName("personero") val personero: PersoneroMini? = null,
    @SerializedName("id_centro_asignado") val id_centro_asignado: Int? = null
) {
    val id: Int? get() = id_mesa
    val numMesaSeguro: String get() = num_mesa ?: ""
    val codLocalSeguro: String get() = cod_local ?: ""
    val nombreLocalSeguro: String get() = nombre_local ?: ""
    val nombreCompleto: String get() = numMesaSeguro
    val totalElectores: Int get() = 0
    val estado: String
        get() = estadoApi ?: if (personero != null && (personero.idusuario != null || personero.idusuario_id != null || personero.id_solicitud != null)) "OCUPADA" else "LIBRE"
    val estaLibre: Boolean
        get() = estado != "OCUPADA"
    val personeroId: Int? get() = personero?.idusuario ?: personero?.idusuario_id
    val usuario: Usuario? get() = null
}

data class MesaCentroResponse(
    @SerializedName("id_mesa") val id_mesa: Int = 0,
    @SerializedName("num_mesa") val num_mesa: String? = null,
    @SerializedName("cod_local") val cod_local: String? = null,
    @SerializedName("codi_ubigeo") val codi_ubigeo: String? = null,
    @SerializedName("nombre_local") val nombre_local: String? = null,
    @SerializedName("direccion_local") val direccion_local: String? = null,
    @SerializedName("departamento") val departamento: String? = null,
    @SerializedName("provincia") val provincia: String? = null,
    @SerializedName("distrito") val distrito: String? = null,
    @SerializedName("estado") val estado: String? = null,
    @SerializedName("personero") val personero: PersoneroMini? = null
) {
    val estaLibre: Boolean get() = estado != "OCUPADA"
    val estadoSeguro: String get() = estado ?: if (personero != null) "OCUPADA" else "LIBRE"
}
