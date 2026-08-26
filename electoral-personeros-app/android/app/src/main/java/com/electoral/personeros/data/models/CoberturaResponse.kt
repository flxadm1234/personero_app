package com.electoral.personeros.data.models

import com.google.gson.annotations.SerializedName

data class CoberturaResponse(
    @SerializedName("total_centros") val total_centros: Int = 0,
    @SerializedName("centros_asignados") val centros_asignados: Int = 0,
    @SerializedName("centros_sin_asignar") val centros_sin_asignar: Int = 0,
    @SerializedName("porcentaje_centros") val porcentaje_centros: Double = 0.0,
    @SerializedName("total_mesas") val total_mesas: Int = 0,
    @SerializedName("mesas_asignadas") val mesas_asignadas: Int = 0,
    @SerializedName("mesas_sin_asignar") val mesas_sin_asignar: Int = 0,
    @SerializedName("porcentaje_mesas") val porcentaje_mesas: Double = 0.0,
    @SerializedName("total_personeros_centro") val total_personeros_centro: Int = 0,
    @SerializedName("total_personeros_mesa") val total_personeros_mesa: Int = 0
) {
    val porcentajeCobertura: Float get() = porcentaje_mesas.toFloat()
    val totalCentros: Int get() = total_centros
    val totalPersoneros: Int get() = total_personeros_centro + total_personeros_mesa
    val totalElectores: Int get() = 0
    val mesasAsignadas: Int get() = mesas_asignadas
    val totalMesas: Int get() = total_mesas
}
