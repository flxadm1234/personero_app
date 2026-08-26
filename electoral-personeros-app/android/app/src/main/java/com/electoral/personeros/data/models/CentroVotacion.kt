package com.electoral.personeros.data.models

import com.google.gson.annotations.SerializedName

data class CentroVotacion(
    @SerializedName("id_centro") val id_centro: Int = 0,
    @SerializedName("codigo_local") val codigo_local: String = "",
    @SerializedName("ubigeo") val ubigeo: String = "",
    @SerializedName("nombre_local") val nombre_local: String = "",
    @SerializedName("direccion_local") val direccion_local: String = "",
    @SerializedName("departamento") val departamento: String = "",
    @SerializedName("provincia") val provincia: String = "",
    @SerializedName("distrito") val distrito: String = "",
    @SerializedName("total_mesas") val total_mesas_api: Int? = null,
    @SerializedName("mesas_asignadas") val mesas_asignadas_api: Int? = null
) {
    val id: Int? get() = id_centro
    val nombre: String get() = nombre_local
    val codigo: String get() = codigo_local
    val totalMesas: Int get() = total_mesas_api ?: 0
    val mesasAsignadas: Int get() = mesas_asignadas_api ?: 0
    val personerosAsignados: Int get() = 0
    val totalElectores: Int get() = 0
}
