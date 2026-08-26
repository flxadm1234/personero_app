package com.electoral.personeros.data.repository

import com.electoral.personeros.data.api.RetrofitInstance
import com.electoral.personeros.data.models.CentroVotacion

class CentroVotacionRepository {

    suspend fun getCentrosVotacion(
        provincia: String? = null,
        distrito: String? = null,
        search: String? = null
    ): Result<List<CentroVotacion>> {
        return try {
            val response = RetrofitInstance.api.getCentrosVotacion(
                provincia = provincia,
                distrito = distrito,
                search = search
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al cargar centros"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCentroVotacionById(id: Int): Result<CentroVotacion> {
        return try {
            val response = RetrofitInstance.api.getCentroVotacionById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCentroVotacion(centro: CentroVotacion): Result<CentroVotacion> {
        return try {
            val response = RetrofitInstance.api.createCentroVotacion(centro)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al crear centro"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCentroVotacion(id: Int, centro: CentroVotacion): Result<CentroVotacion> {
        return try {
            val response = RetrofitInstance.api.updateCentroVotacion(id, centro)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCentroVotacion(id: Int): Result<Boolean> {
        return try {
            val response = RetrofitInstance.api.deleteCentroVotacion(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarCentros(
        q: String,
        provincia: String? = null,
        distrito: String? = null
    ): Result<List<CentroVotacion>> {
        return try {
            val response = RetrofitInstance.api.buscarCentrosAdmin(
                q = q,
                provincia = provincia,
                distrito = distrito
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al buscar centros"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminCentros(
        provincia: String? = null,
        distrito: String? = null
    ): Result<List<CentroVotacion>> {
        return try {
            val response = RetrofitInstance.api.getAdminCentros(
                provincia = provincia,
                distrito = distrito
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al cargar centros"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
