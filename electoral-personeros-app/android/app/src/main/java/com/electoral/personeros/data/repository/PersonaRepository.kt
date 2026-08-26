package com.electoral.personeros.data.repository

import com.electoral.personeros.data.api.RetrofitInstance
import com.electoral.personeros.data.models.Persona

class PersonaRepository {

    suspend fun getPersonas(
        search: String? = null,
        provincia: String? = null,
        distrito: String? = null
    ): Result<List<Persona>> {
        return try {
            val response = RetrofitInstance.api.getPersonas(
                search = search,
                provincia = provincia,
                distrito = distrito
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al cargar personas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPersonaById(id: Int): Result<Persona> {
        return try {
            val response = RetrofitInstance.api.getPersonaById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createPersona(persona: Persona): Result<Persona> {
        return try {
            val response = RetrofitInstance.api.createPersona(persona)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al crear persona"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePersona(id: Int, persona: Persona): Result<Persona> {
        return try {
            val response = RetrofitInstance.api.updatePersona(id, persona)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePersona(id: Int): Result<Boolean> {
        return try {
            val response = RetrofitInstance.api.deletePersona(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
