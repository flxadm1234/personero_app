package com.electoral.personeros.data.models

import com.google.gson.annotations.SerializedName

data class GenericResponse(
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("extra") val extra: Map<String, Any>? = null
)
