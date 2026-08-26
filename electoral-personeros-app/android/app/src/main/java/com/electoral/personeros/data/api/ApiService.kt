package com.electoral.personeros.data.api

import com.electoral.personeros.data.models.*
import okhttp3.ResponseBody
import org.jetbrains.annotations.Nullable
import retrofit2.Response
import retrofit2.http.*
import kotlin.jvm.JvmSuppressWildcards

interface ApiService {

    // ==================== AUTH ====================
    @FormUrlEncoded
    @POST("/api/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @POST("/api/auth/logout")
    suspend fun logout(): Response<GenericResponse>

    @GET("/api/auth/me")
    suspend fun getCurrentUser(): Response<Usuario>

    @POST("/api/auth/change-password")
    suspend fun changePassword(@Body body: Map<String, String>): Response<GenericResponse>

    @PUT("/api/auth/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): Response<GenericResponse>

    // ==================== PUBLIC ====================
    @POST("/api/public/solicitud-personero")
    suspend fun createSolicitudPublica(@Body body: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Any>): Response<GenericResponse>

    // ==================== USUARIOS (genéricos) ====================
    @GET("/api/usuarios")
    suspend fun getUsuarios(
        @Query("rol") rol: String? = null,
        @Query("activo") activo: Boolean? = null,
        @Query("provincia") provincia: String? = null,
        @Query("search") search: String? = null,
        @Query("perPage") perPage: Int? = null
    ): Response<List<Usuario>>

    @GET("/api/usuarios/{id}")
    suspend fun getUsuarioById(@Path("id") id: Int): Response<Usuario>

    @POST("/api/usuarios")
    suspend fun createUsuario(@Body usuario: Usuario): Response<Usuario>

    @PUT("/api/usuarios/{id}")
    suspend fun updateUsuario(@Path("id") id: Int, @Body usuario: Usuario): Response<Usuario>

    @DELETE("/api/usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Int): Response<GenericResponse>

    @GET("/api/personeros-disponibles")
    suspend fun getPersonerosDisponibles(
        @Query("provincia") provincia: String? = null,
        @Query("distrito") distrito: String? = null,
        @Query("centroVotacionId") centroVotacionId: Int? = null
    ): Response<List<Usuario>>

    // ==================== SUPER_ADMIN ====================
    @GET("/api/superadmin/usuarios")
    suspend fun getSuperAdminUsuarios(): Response<List<Usuario>>

    @POST("/api/superadmin/usuarios")
    suspend fun createSuperAdminUsuario(@Body body: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Any>): Response<Usuario>

    @PUT("/api/superadmin/usuarios/{idusuario}")
    suspend fun updateSuperAdminUsuario(
        @Path("idusuario") idusuario: Int,
        @Body body: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Any>
    ): Response<Usuario>

    @PATCH("/api/superadmin/usuarios/{idusuario}/estado")
    suspend fun updateUsuarioEstado(
        @Path("idusuario") idusuario: Int,
        @Body body: Map<String, Boolean>
    ): Response<GenericResponse>

    @POST("/api/superadmin/usuarios/{idusuario}/reset-password")
    suspend fun resetUsuarioPassword(
        @Path("idusuario") idusuario: Int,
        @Body body: Map<String, String>
    ): Response<GenericResponse>

    // ==================== ADMIN ====================
    @POST("/api/admin/personeros-centro")
    suspend fun createPersoneroCentro(@Body body: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Any>): Response<Usuario>

    @GET("/api/admin/personeros-centro")
    suspend fun getAdminPersonerosCentro(): Response<List<Usuario>>

    @GET("/api/admin/centros-sin-personero")
    suspend fun getCentrosSinPersonero(): Response<List<CentroVotacion>>

    @GET("/api/admin/centros")
    suspend fun getAdminCentros(
        @Query("provincia") provincia: String? = null,
        @Query("distrito") distrito: String? = null
    ): Response<List<CentroVotacion>>

    @GET("/api/admin/centros/buscar")
    suspend fun buscarCentrosAdmin(
        @Query("q") q: String,
        @Query("provincia") provincia: String? = null,
        @Query("distrito") distrito: String? = null
    ): Response<List<CentroVotacion>>

    @GET("/api/admin/mesas")
    suspend fun getAdminMesas(
        @Query("centroId") centroId: Int? = null,
        @Query("provincia") provincia: String? = null,
        @Query("distrito") distrito: String? = null,
        @Query("soloDisponibles") soloDisponibles: Boolean? = null,
        @Query("perPage") perPage: Int? = null
    ): Response<List<MesaCentroResponse>>

    @POST("/api/admin/solicitudes/{id}/aprobar")
    suspend fun aprobarSolicitud(@Path("id") id: Int): Response<GenericResponse>

    @POST("/api/admin/solicitudes/{id}/rechazar")
    suspend fun rechazarSolicitud(@Path("id") id: Int): Response<GenericResponse>

    @POST("/api/admin/asignaciones/personero-centro/asignar-centro")
    suspend fun asignarCentroAPersonero(@Body body: AsignarCentroRequest): Response<GenericResponse>

    @POST("/api/admin/asignaciones/personero-mesa/asignar-mesa")
    suspend fun asignarMesaAPersonero(@Body body: AsignarMesaRequest): Response<GenericResponse>

    @GET("/api/admin/monitoreo/cobertura")
    suspend fun getMonitoreoCobertura(): Response<CoberturaResponse>

    @GET("/api/admin/dashboard/desglose")
    suspend fun getDashboardDesglose(): Response<List<DesgloseItem>>

    @GET("/api/admin/cobertura/provincia/{provincia}")
    suspend fun getCoberturaProvincia(@Path("provincia") provincia: String): Response<CoberturaResponse>

    @GET("/api/admin/cobertura/distrito/{provincia}/{distrito}")
    suspend fun getCoberturaDistrito(
        @Path("provincia") provincia: String,
        @Path("distrito") distrito: String
    ): Response<CoberturaResponse>

    @GET("/api/admin/solicitudes-pendientes")
    suspend fun getSolicitudesPendientes(): Response<List<SolicitudPersonero>>

    @Streaming
    @GET("/api/admin/reportes/excel")
    suspend fun downloadReporteExcel(): Response<ResponseBody>

    // ==================== SOLICITUDES PERSONERO ====================
    @GET("/api/solicitudes-personeros")
    suspend fun getSolicitudesPersoneros(
        @Query("estado") estado: String? = null,
        @Query("provincia") provincia: String? = null,
        @Query("distrito") distrito: String? = null,
        @Query("search") search: String? = null
    ): Response<List<SolicitudPersonero>>

    @GET("/api/solicitudes-personeros/{id}")
    suspend fun getSolicitudPersoneroById(@Path("id") id: Int): Response<SolicitudPersonero>

    @POST("/api/solicitudes-personeros")
    suspend fun createSolicitudPersonero(@Body solicitud: SolicitudPersonero): Response<SolicitudPersonero>

    @PUT("/api/solicitudes-personeros/{id}")
    suspend fun updateSolicitudPersonero(
        @Path("id") id: Int,
        @Body solicitud: SolicitudPersonero
    ): Response<SolicitudPersonero>

    @PATCH("/api/solicitudes-personeros/{id}/estado")
    suspend fun actualizarEstadoSolicitud(
        @Path("id") id: Int,
        @Body body: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards Any>
    ): Response<SolicitudPersonero>

    @DELETE("/api/solicitudes-personeros/{id}")
    suspend fun deleteSolicitudPersonero(@Path("id") id: Int): Response<GenericResponse>

    // ==================== PERSONAS ====================
    @GET("/api/personas")
    suspend fun getPersonas(
        @Query("search") search: String? = null,
        @Query("provincia") provincia: String? = null,
        @Query("distrito") distrito: String? = null
    ): Response<List<Persona>>

    @GET("/api/personas/{id}")
    suspend fun getPersonaById(@Path("id") id: Int): Response<Persona>

    @POST("/api/personas")
    suspend fun createPersona(@Body persona: Persona): Response<Persona>

    @PUT("/api/personas/{id}")
    suspend fun updatePersona(@Path("id") id: Int, @Body persona: Persona): Response<Persona>

    @DELETE("/api/personas/{id}")
    suspend fun deletePersona(@Path("id") id: Int): Response<GenericResponse>

    // ==================== CENTROS DE VOTACION ====================
    @GET("/api/centros-votacion")
    suspend fun getCentrosVotacion(
        @Query("provincia") provincia: String? = null,
        @Query("distrito") distrito: String? = null,
        @Query("search") search: String? = null
    ): Response<List<CentroVotacion>>

    @GET("/api/centros-votacion/{id}")
    suspend fun getCentroVotacionById(@Path("id") id: Int): Response<CentroVotacion>

    @POST("/api/centros-votacion")
    suspend fun createCentroVotacion(@Body centro: CentroVotacion): Response<CentroVotacion>

    @PUT("/api/centros-votacion/{id}")
    suspend fun updateCentroVotacion(
        @Path("id") id: Int,
        @Body centro: CentroVotacion
    ): Response<CentroVotacion>

    @DELETE("/api/centros-votacion/{id}")
    suspend fun deleteCentroVotacion(@Path("id") id: Int): Response<GenericResponse>

    // ==================== MESAS ====================
    @GET("/api/mesas")
    suspend fun getMesas(
        @Query("centroVotacionId") centroVotacionId: Int? = null,
        @Query("estado") estado: String? = null,
        @Query("personeroId") personeroId: Int? = null,
        @Query("provincia") provincia: String? = null,
        @Query("distrito") distrito: String? = null
    ): Response<List<Mesa>>

    @GET("/api/mesas/{id}")
    suspend fun getMesaById(@Path("id") id: Int): Response<Mesa>

    @POST("/api/mesas")
    suspend fun createMesa(@Body mesa: Mesa): Response<Mesa>

    @PUT("/api/mesas/{id}")
    suspend fun updateMesa(@Path("id") id: Int, @Body mesa: Mesa): Response<Mesa>

    @DELETE("/api/mesas/{id}")
    suspend fun deleteMesa(@Path("id") id: Int): Response<GenericResponse>

    @GET("/api/mesas/disponibles")
    suspend fun getMesasDisponibles(
        @Query("centroVotacionId") centroVotacionId: Int? = null,
        @Query("provincia") provincia: String? = null,
        @Query("distrito") distrito: String? = null
    ): Response<List<Mesa>>

    // ==================== PERSONERO_CENTRO ====================
    @GET("/api/personero-centro/solicitudes")
    suspend fun getPersoneroCentroSolicitudes(): Response<List<SolicitudPersonero>>

    @GET("/api/personero-centro/personeros-disponibles")
    suspend fun getPersonerosCentroDisponibles(): Response<List<PersoneroDisponible>>

    @GET("/api/personero-centro/mesas-centro/{id_centro}")
    suspend fun getMesasCentro(@Path("id_centro") id_centro: Int): Response<List<MesaCentroResponse>>

    @POST("/api/personero-centro/asignar-mesa")
    suspend fun asignarMesa(@Body request: AsignarMesaRequest): Response<GenericResponse>

    @POST("/api/personero-centro/desasignar-mesa/{id}")
    suspend fun desasignarMesaPath(@Path("id") id: Int): Response<Mesa>

    @GET("/api/personero-centro/personas-by-dni/{dni}")
    suspend fun getPersoneroCentroPersonaByDni(@Path("dni") dni: String): Response<PersonaDNILookupResponse>

    @POST("/api/personero-centro/asignaciones/desasignar-mesa")
    suspend fun desasignarMesaPersoneroCentro(@Body body: DesasignarMesaRequest): Response<GenericResponse>

    @POST("/api/personero-centro/asignaciones/registrar-y-asignar-mesa")
    suspend fun registrarYAsignarMesaPersoneroCentro(@Body body: RegistrarYAsignarMesaRequest): Response<GenericResponse>

    // ==================== ADMIN - DNI LOOKUP / REGISTRAR Y ASIGNAR / DESASIGNAR ====================
    @GET("/api/admin/personas-by-dni/{dni}")
    suspend fun getAdminPersonaByDni(@Path("dni") dni: String): Response<PersonaDNILookupResponse>

    @POST("/api/admin/asignaciones/personero-mesa/desasignar-mesa")
    suspend fun desasignarMesaAdmin(@Body body: DesasignarMesaRequest): Response<GenericResponse>

    @POST("/api/admin/asignaciones/personero-mesa/registrar-y-asignar-mesa")
    suspend fun registrarYAsignarMesaAdmin(@Body body: RegistrarYAsignarMesaRequest): Response<GenericResponse>

    // ==================== USUARIOS BRIDGE ====================
    @GET("/api/usuarios/persona-by-dni/{dni}")
    suspend fun getUsuarioPersonaByDni(@Path("dni") dni: String): Response<PersonaDNILookupResponse>

    // ==================== SUPER ADMIN - DISTRITOS ====================
    @GET("/api/superadmin/usuarios/{idusuario}/distritos")
    suspend fun getSuperAdminUsuarioDistritos(@Path("idusuario") idusuario: Int): Response<Map<String, @JvmSuppressWildcards Any>>

    @PUT("/api/superadmin/usuarios/{idusuario}/distritos")
    suspend fun updateSuperAdminUsuarioDistritos(
        @Path("idusuario") idusuario: Int,
        @Body body: AdminDistritosUpdateRequest
    ): Response<GenericResponse>

    @GET("/api/superadmin/usuarios/{idusuario}/distritos-asignados")
    suspend fun getSuperAdminUsuarioDistritosAsignados(@Path("idusuario") idusuario: Int): Response<List<DistritoAsignadoItem>>

    // ==================== AUTH ME (perfil completo) ====================
    @GET("/api/auth/me")
    suspend fun getAuthMe(): Response<PerfilUsuarioCompletoResponse>
}
