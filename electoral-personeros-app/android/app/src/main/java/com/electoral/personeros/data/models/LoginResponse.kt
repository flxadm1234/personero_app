package com.electoral.personeros.data.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("access_token") val access_token: String? = null,
    @SerializedName("token_type") val token_type: String? = "Bearer",
    @SerializedName("user") val user: UserResponseData? = null,

    @SerializedName("success") val success: Boolean = true,
    @SerializedName("message") val message: String? = null,
    @SerializedName("accessToken") val accessToken: String? = access_token,
    @SerializedName("tokenType") val tokenType: String? = token_type,
    @SerializedName("refreshToken") val refreshToken: String? = null,
    @SerializedName("expiresIn") val expiresIn: Int? = null,
    @SerializedName("usuario") val usuario: Usuario? = null
)
