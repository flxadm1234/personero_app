package com.electoral.personeros.data.repository

import com.electoral.personeros.data.api.RetrofitInstance
import com.electoral.personeros.data.models.SolicitudPersonero

class SolicitudRepository {

    suspend fun getSolicitudes(
        estado: String? = null,
        provincia: String? = null,
        distrito: String? = null,
        search: String? = null
    ): Result<List<SolicitudPersonero>> {
        return try {
            val response = RetrofitInstance.api.getSolicitudesPersoneros(
                estado = estado,
                provincia = provincia,
                distrito = distrito,
                search = search
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al cargar solicitudes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSolicitudById(id: Int): Result<SolicitudPersonero> {
        return try {
            val response = RetrofitInstance.api.getSolicitudPersoneroById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSolicitud(solicitud: SolicitudPersonero): Result<SolicitudPersonero> {
        return try {
            val body = mutableMapOf<String, Any>(
                "dni" to (solicitud.persona?.dni ?: ""),
                "nombre" to (solicitud.persona?.nombre ?: ""),
                "apellidos" to (solicitud.persona?.apellidos ?: ""),
                "telefono1" to (solicitud.persona?.telefono1 ?: ""),
                "provincia" to (solicitud.persona?.provincia ?: ""),
                "distrito" to (solicitud.persona?.distrito ?: ""),
                "tipo_personero_solicitado" to solicitud.tipo_personero_solicitado,
                "fecha_registro" to solicitud.fecha_registro
            )
            solicitud.persona?.telefono2?.takeIf { it.isNotBlank() }?.let { body["telefono2"] = it }
            solicitud.persona?.direccion?.takeIf { it.isNotBlank() }?.let { body["direccion"] = it }
            solicitud.persona?.correo?.takeIf { it.isNotBlank() }?.let { body["correo"] = it }

            val response = RetrofitInstance.api.createSolicitudPublica(body)
            if (response.isSuccessful && response.body() != null) {
                Result.success(solicitud)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al crear solicitud"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSolicitud(id: Int, solicitud: SolicitudPersonero): Result<SolicitudPersonero> {
        return try {
            val response = RetrofitInstance.api.updateSolicitudPersonero(id, solicitud)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarEstado(id: Int, nuevoEstado: String, comentarios: String? = null): Result<SolicitudPersonero> {
        return try {
            val body = mutableMapOf("estado" to nuevoEstado)
            comentarios?.let { body["comentarios"] = it }
            val response = RetrofitInstance.api.actualizarEstadoSolicitud(id, body)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSolicitud(id: Int): Result<Boolean> {
        return try {
            val response = RetrofitInstance.api.deleteSolicitudPersonero(id)
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
