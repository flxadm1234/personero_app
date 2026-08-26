package com.electoral.personeros.data.models

import com.google.gson.annotations.SerializedName

data class DesgloseItem(
    @SerializedName("provincia") val provincia: String = "",
    @SerializedName("distrito") val distrito: String = "",
    @SerializedName("total_centros") val total_centros: Int = 0,
    @SerializedName("centros_asignados") val centros_asignados: Int = 0,
    @SerializedName("total_mesas") val total_mesas: Int = 0,
    @SerializedName("mesas_asignadas") val mesas_asignadas: Int = 0
) {
    val clave: String get() = "$provincia-$distrito"
    val nombre: String get() = distrito.ifBlank { provincia }
    val asignadas: Int get() = mesas_asignadas
    val total: Int get() = total_mesas
    val porcentaje: Float get() = if (total_mesas > 0) (mesas_asignadas.toFloat() / total_mesas) * 100f else 0f
    val disponibles: Int get() = total_mesas - mesas_asignadas
}
