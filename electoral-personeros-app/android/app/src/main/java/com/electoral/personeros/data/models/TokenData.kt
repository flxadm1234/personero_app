package com.electoral.personeros.data.models

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "electoral_personeros_prefs")

data class TokenData(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val tokenType: String = "Bearer",
    val expiresIn: Int? = null,
    val usuarioId: Int? = null,
    val username: String? = null,
    val rol: String? = null,
    val provinciaAsignada: String? = null,
    val distritoAsignado: String? = null,
    val isLoggedIn: Boolean = false
) {
    fun getAuthorizationHeader(): String? {
        return accessToken?.let { "$tokenType $it" }
    }
}

class TokenDataStore(private val context: Context) {

    private object PreferencesKeys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val TOKEN_TYPE = stringPreferencesKey("token_type")
        val EXPIRES_IN = intPreferencesKey("expires_in")
        val USUARIO_ID = intPreferencesKey("usuario_id")
        val USERNAME = stringPreferencesKey("username")
        val ROL = stringPreferencesKey("rol")
        val PROVINCIA_ASIGNADA = stringPreferencesKey("provincia_asignada")
        val DISTRITO_ASIGNADO = stringPreferencesKey("distrito_asignado")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val tokenData: Flow<TokenData> = context.dataStore.data.map { preferences ->
        TokenData(
            accessToken = preferences[PreferencesKeys.ACCESS_TOKEN],
            refreshToken = preferences[PreferencesKeys.REFRESH_TOKEN],
            tokenType = preferences[PreferencesKeys.TOKEN_TYPE] ?: "Bearer",
            expiresIn = preferences[PreferencesKeys.EXPIRES_IN],
            usuarioId = preferences[PreferencesKeys.USUARIO_ID],
            username = preferences[PreferencesKeys.USERNAME],
            rol = preferences[PreferencesKeys.ROL],
            provinciaAsignada = preferences[PreferencesKeys.PROVINCIA_ASIGNADA],
            distritoAsignado = preferences[PreferencesKeys.DISTRITO_ASIGNADO],
            isLoggedIn = preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        )
    }

    suspend fun saveTokenData(data: TokenData) {
        context.dataStore.edit { preferences ->
            data.accessToken?.let { preferences[PreferencesKeys.ACCESS_TOKEN] = it }
            data.refreshToken?.let { preferences[PreferencesKeys.REFRESH_TOKEN] = it }
            preferences[PreferencesKeys.TOKEN_TYPE] = data.tokenType
            data.expiresIn?.let { preferences[PreferencesKeys.EXPIRES_IN] = it }
            data.usuarioId?.let { preferences[PreferencesKeys.USUARIO_ID] = it }
            data.username?.let { preferences[PreferencesKeys.USERNAME] = it }
            data.rol?.let { preferences[PreferencesKeys.ROL] = it }
            data.provinciaAsignada?.let { preferences[PreferencesKeys.PROVINCIA_ASIGNADA] = it }
            data.distritoAsignado?.let { preferences[PreferencesKeys.DISTRITO_ASIGNADO] = it }
            preferences[PreferencesKeys.IS_LOGGED_IN] = data.isLoggedIn
        }
    }

    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
