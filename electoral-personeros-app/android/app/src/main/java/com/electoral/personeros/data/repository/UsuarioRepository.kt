package com.electoral.personeros.data.repository

import com.electoral.personeros.data.api.RetrofitInstance
import com.electoral.personeros.data.models.Usuario
import com.electoral.personeros.utils.Constants

class UsuarioRepository {

    suspend fun getAllUsuarios(
        rol: String? = null,
        activo: Boolean? = null,
        provincia: String? = null,
        search: String? = null
    ): Result<List<Usuario>> {
        return try {
            val response = RetrofitInstance.api.getUsuarios(
                rol = rol,
                activo = activo,
                provincia = provincia,
                search = search,
                perPage = Constants.PAGE_SIZE
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Error al cargar usuarios"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsuarioById(id: Int): Result<Usuario> {
        return try {
            val response = RetrofitInstance.api.getUsuarioById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUsuario(usuario: Usuario): Result<Usuario> {
        return try {
            val usuarioConClave = if (!usuario.password.isNullOrBlank()) usuario.copy(clave = usuario.password) else usuario
            val response = RetrofitInstance.api.createUsuario(usuarioConClave)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al crear usuario"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUsuario(id: Int, usuario: Usuario): Result<Usuario> {
        return try {
            val usuarioConClave = if (!usuario.password.isNullOrBlank()) usuario.copy(clave = usuario.password) else usuario
            val response = RetrofitInstance.api.updateUsuario(id, usuarioConClave)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al actualizar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUsuario(id: Int): Result<Boolean> {
        return try {
            val response = RetrofitInstance.api.deleteUsuario(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al eliminar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPersonerosDisponibles(
        provincia: String? = null,
        distrito: String? = null,
        centroVotacionId: Int? = null
    ): Result<List<Usuario>> {
        return try {
            val response = RetrofitInstance.api.getPersonerosDisponibles(
                provincia = provincia,
                distrito = distrito,
                centroVotacionId = centroVotacionId
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSuperAdmins(): Result<List<Usuario>> = getAllUsuarios(rol = Constants.ROLE_SUPER_ADMIN)
    suspend fun getAdminsProvinciales(): Result<List<Usuario>> = getAllUsuarios(rol = Constants.ROLE_ADMINISTRADOR)
    suspend fun getPersoneros(): Result<List<Usuario>> = getAllUsuarios(rol = Constants.ROLE_PERSONERO_CENTRO)
}
