package com.electoral.personeros.data.repository

import com.electoral.personeros.data.api.RetrofitInstance
import com.electoral.personeros.data.models.AsignarCentroRequest
import com.electoral.personeros.data.models.AsignarMesaRequest
import com.electoral.personeros.data.models.Mesa
import com.electoral.personeros.data.models.MesaCentroResponse

class MesaRepository {

    private fun MesaCentroResponse.toMesa(): Mesa = Mesa(
        id_mesa = this.id_mesa,
        num_mesa = this.num_mesa,
        cod_local = this.cod_local,
        codi_ubigeo = this.codi_ubigeo,
        nombre_local = this.nombre_local,
        direccion_local = this.direccion_local,
        departamento = this.departamento ?: "LORETO",
        provincia = this.provincia,
        distrito = this.distrito,
        estadoApi = this.estado,
        personero = this.personero
    )

    suspend fun getMesas(
        centroVotacionId: Int? = null,
        estado: String? = null,
        personeroId: Int? = null,
        provincia: String? = null,
        distrito: String? = null
    ): Result<List<Mesa>> {
        return try {
            val response = RetrofitInstance.api.getAdminMesas(
                centroId = centroVotacionId,
                provincia = provincia,
                distrito = distrito,
                soloDisponibles = if (estado != null && estado.equals("LIBRE", ignoreCase = true)) true else null,
                perPage = 500
            )
            if (response.isSuccessful && response.body() != null) {
                val adminList = response.body()!!
                val mapped = adminList.map { it.toMesa() }.let { list ->
                    if (estado != null) list.filter { it.estado.equals(estado, ignoreCase = true) } else list
                }.let { list ->
                    if (personeroId != null) list.filter { mesa ->
                        mesa.personero?.idusuario == personeroId ||
                            mesa.personero?.idusuario_id == personeroId ||
                            mesa.personero?.id_solicitud == personeroId
                    } else list
                }
                Result.success(mapped)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al cargar mesas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMesaById(id: Int): Result<Mesa> {
        return try {
            val response = RetrofitInstance.api.getMesaById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createMesa(mesa: Mesa): Result<Mesa> {
        return try {
            val response = RetrofitInstance.api.createMesa(mesa)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al crear mesa"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateMesa(id: Int, mesa: Mesa): Result<Mesa> {
        return try {
            val response = RetrofitInstance.api.updateMesa(id, mesa)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMesa(id: Int): Result<Boolean> {
        return try {
            val response = RetrofitInstance.api.deleteMesa(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun asignarMesa(
        mesaId: Int,
        personeroId: Int,
        centroVotacionId: Int? = null,
        observaciones: String? = null,
        idUsuarioReal: Int? = null
    ): Result<Mesa> {
        return try {
            val body = AsignarMesaRequest(
                idMesa = mesaId, id_mesa = mesaId,
                idSolicitud = personeroId, id_solicitud = personeroId,
                idUsuario = idUsuarioReal, id_usuario = idUsuarioReal, idusuario = idUsuarioReal,
                idCentro = centroVotacionId, id_centro = centroVotacionId,
                observaciones = observaciones?.takeIf { it.isNotBlank() }
            )
            val response = RetrofitInstance.api.asignarMesaAPersonero(body)
            if (response.isSuccessful) {
                val responseMesa = RetrofitInstance.api.getMesaById(mesaId)
                val mesa = if (responseMesa.isSuccessful && responseMesa.body() != null) {
                    responseMesa.body()!!
                } else {
                    Mesa(
                        id_mesa = mesaId,
                        num_mesa = "",
                        cod_local = centroVotacionId?.toString() ?: "",
                        codi_ubigeo = "",
                        nombre_local = "",
                        direccion_local = "",
                        departamento = "LORETO",
                        provincia = "",
                        distrito = "",
                        estadoApi = "OCUPADA"
                    )
                }
                Result.success(mesa)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al asignar mesa"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun desasignarMesa(id: Int): Result<Mesa> {
        return try {
            val requestBody = com.electoral.personeros.data.models.DesasignarMesaRequest(
                idMesa = id, id_mesa = id
            )
            var response = RetrofitInstance.api.desasignarMesaPersoneroCentro(requestBody)
            if (!response.isSuccessful) {
                response = RetrofitInstance.api.desasignarMesaAdmin(requestBody)
            }
            if (response.isSuccessful) {
                val getMesa = RetrofitInstance.api.getMesaById(id)
                val result = if (getMesa.isSuccessful && getMesa.body() != null) {
                    getMesa.body()!!
                } else {
                    Mesa(id_mesa = id, estadoApi = "LIBRE")
                }
                Result.success(result)
            } else {
                val fallback = RetrofitInstance.api.desasignarMesaPath(id)
                if (fallback.isSuccessful && fallback.body() != null) {
                    Result.success(fallback.body()!!)
                } else {
                    val getMesa = RetrofitInstance.api.getMesaById(id)
                    val final = if (getMesa.isSuccessful && getMesa.body() != null) {
                        getMesa.body()!!
                    } else {
                        Mesa(id_mesa = id, estadoApi = "LIBRE")
                    }
                    Result.success(final)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun buscarPersonaPorDni(dni: String): Result<com.electoral.personeros.data.models.PersonaDNILookupResponse> {
        return try {
            var response = RetrofitInstance.api.getPersoneroCentroPersonaByDni(dni)
            if (!response.isSuccessful) {
                response = RetrofitInstance.api.getAdminPersonaByDni(dni)
            }
            if (!response.isSuccessful) {
                response = RetrofitInstance.api.getUsuarioPersonaByDni(dni)
            }
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(com.electoral.personeros.data.models.PersonaDNILookupResponse(encontrado = false))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun registrarYAsignarMesaDirecto(
        request: com.electoral.personeros.data.models.RegistrarYAsignarMesaRequest
    ): Result<Boolean> {
        return try {
            var response = RetrofitInstance.api.registrarYAsignarMesaPersoneroCentro(request)
            if (!response.isSuccessful) {
                response = RetrofitInstance.api.registrarYAsignarMesaAdmin(request)
            }
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al registrar y asignar personero"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMesasDisponibles(
        centroVotacionId: Int? = null,
        provincia: String? = null,
        distrito: String? = null
    ): Result<List<Mesa>> {
        return try {
            val response = RetrofitInstance.api.getAdminMesas(
                centroId = centroVotacionId,
                provincia = provincia,
                distrito = distrito,
                soloDisponibles = true,
                perPage = 500
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.map { it.toMesa() })
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdminMesas(
        centroId: Int? = null,
        provincia: String? = null,
        distrito: String? = null,
        soloDisponibles: Boolean? = null
    ): Result<List<MesaCentroResponse>> {
        return try {
            val response = RetrofitInstance.api.getAdminMesas(
                centroId = centroId,
                provincia = provincia,
                distrito = distrito,
                soloDisponibles = soloDisponibles,
                perPage = 500
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al cargar mesas admin"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun asignarMesaAPersonero(
        idUsuario: Int,
        idMesa: Int,
        idCentro: Int? = null,
        idSolicitud: Int? = null,
        observaciones: String? = null
    ): Result<Boolean> {
        return try {
            val body = AsignarMesaRequest(
                idUsuario = idUsuario, id_usuario = idUsuario, idusuario = idUsuario,
                idMesa = idMesa, id_mesa = idMesa,
                idCentro = idCentro, id_centro = idCentro,
                idSolicitud = idSolicitud, id_solicitud = idSolicitud,
                observaciones = observaciones
            )
            val response = RetrofitInstance.api.asignarMesaAPersonero(body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al asignar mesa"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun asignarCentroAPersonero(
        idUsuario: Int,
        idCentro: Int,
        idSolicitud: Int? = null,
        observaciones: String? = null
    ): Result<Boolean> {
        return try {
            val body = AsignarCentroRequest(
                idUsuario = idUsuario, id_usuario = idUsuario, idusuario = idUsuario,
                idCentro = idCentro, id_centro = idCentro,
                idSolicitud = idSolicitud, id_solicitud = idSolicitud,
                observaciones = observaciones
            )
            val response = RetrofitInstance.api.asignarCentroAPersonero(body)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error al asignar centro"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
