package com.electoral.personeros.data.repository

import android.content.Context
import com.electoral.personeros.data.api.RetrofitInstance
import com.electoral.personeros.data.models.LoginResponse
import com.electoral.personeros.data.models.TokenData
import com.electoral.personeros.data.models.TokenDataStore
import com.electoral.personeros.data.models.Usuario
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val context: Context) {

    private val tokenDataStore = TokenDataStore(context)

    val tokenData: Flow<TokenData> = tokenDataStore.tokenData

    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = RetrofitInstance.api.login(username.trim(), password)
            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                val token = loginResponse.accessToken ?: loginResponse.access_token
                val successFlag = loginResponse.success || (token != null && response.isSuccessful)
                if (successFlag && token != null) {
                    RetrofitInstance.setAuthToken(token)
                    val usr = loginResponse.usuario
                    val ud = loginResponse.user
                    val tokenData = TokenData(
                        accessToken = token,
                        refreshToken = loginResponse.refreshToken,
                        tokenType = (loginResponse.tokenType ?: loginResponse.token_type) ?: "Bearer",
                        expiresIn = loginResponse.expiresIn,
                        usuarioId = usr?.idusuario ?: ud?.idusuario,
                        username = usr?.usuario ?: ud?.usuario,
                        rol = usr?.rol ?: ud?.rol,
                        provinciaAsignada = usr?.provinciaAsignada,
                        distritoAsignado = usr?.distritoAsignado,
                        isLoggedIn = true
                    )
                    tokenDataStore.saveTokenData(tokenData)
                }
                Result.success(loginResponse)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Boolean> {
        return try {
            runCatching { RetrofitInstance.api.logout() }
            RetrofitInstance.setAuthToken(null)
            tokenDataStore.clearTokens()
            Result.success(true)
        } catch (e: Exception) {
            tokenDataStore.clearTokens()
            RetrofitInstance.setAuthToken(null)
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<Usuario> {
        return try {
            val response = RetrofitInstance.api.getCurrentUser()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al obtener usuario"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreSession(token: String) {
        RetrofitInstance.setAuthToken(token)
    }

    suspend fun clearAllData() {
        RetrofitInstance.setAuthToken(null)
        tokenDataStore.clearTokens()
    }
}
