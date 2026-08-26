package com.electoral.personeros.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.electoral.personeros.data.models.CentroVotacion
import com.electoral.personeros.data.models.Mesa
import com.electoral.personeros.data.models.PersoneroDisponible
import com.electoral.personeros.data.repository.CentroVotacionRepository
import com.electoral.personeros.data.repository.MesaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PersoneroCentroUiState(
    val centros: List<CentroVotacion> = emptyList(),
    val filteredCentros: List<CentroVotacion> = emptyList(),
    val mesasPorCentro: Map<Int, List<Mesa>> = emptyMap(),
    val personerosDisponibles: List<PersoneroDisponible> = emptyList(),
    val mesasDisponibles: List<Mesa> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val provinciaFilter: String? = null,
    val distritoFilter: String? = null,
    val centroExpandido: Int? = null,
    val showAsignarModal: Boolean = false,
    val mesaParaAsignar: Mesa? = null,
    val centroParaAsignar: CentroVotacion? = null,
    val personeroSeleccionado: PersoneroDisponible? = null,
    val observacionesAsignacion: String = "",
    val isAsignando: Boolean = false,
    val asignarError: String? = null,
    val asignarSuccess: Boolean = false,
    val isDesasignando: Int? = null,
    val dniLookup: String = "",
    val dniLookupResult: com.electoral.personeros.data.models.PersonaDNILookupResponse? = null,
    val dniLookupLoading: Boolean = false,
    val showRegistrarNuevo: Boolean = false,
    val isRegistrandoYAsignando: Boolean = false,
    val registrarError: String? = null
)

class PersoneroCentroVM(application: Application) : AndroidViewModel(application) {

    private val centroRepository = CentroVotacionRepository()
    private val mesaRepository = MesaRepository()

    private val _uiState = MutableStateFlow(PersoneroCentroUiState())
    val uiState: StateFlow<PersoneroCentroUiState> = _uiState.asStateFlow()

    fun inicializar(provincia: String? = null, distrito: String? = null) {
        if (_uiState.value.centros.isEmpty() && !_uiState.value.isLoading) {
            loadCentros(provincia, distrito)
        }
    }

    fun loadCentros(provincia: String? = null, distrito: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                provinciaFilter = provincia,
                distritoFilter = distrito
            )
            val result = centroRepository.getCentrosVotacion(
                provincia = provincia,
                distrito = distrito,
                search = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
            )
            result.fold(
                onSuccess = { centros ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        centros = centros,
                        filteredCentros = applyFilters(centros, _uiState.value.searchQuery)
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar centros"
                    )
                }
            )
        }
    }

    private fun applyFilters(centros: List<CentroVotacion>, query: String): List<CentroVotacion> {
        return if (query.isBlank()) centros else centros.filter {
            it.nombre_local.contains(query, ignoreCase = true) ||
                it.codigo_local.contains(query, ignoreCase = true) ||
                it.distrito.contains(query, ignoreCase = true)
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredCentros = applyFilters(_uiState.value.centros, query)
        )
    }

    fun toggleCentroExpandido(centroId: Int) {
        val state = _uiState.value
        val nuevoExpandido = if (state.centroExpandido == centroId) null else centroId
        _uiState.value = state.copy(centroExpandido = nuevoExpandido)
        if (nuevoExpandido != null && !state.mesasPorCentro.containsKey(centroId)) {
            loadMesasForCentro(centroId)
        }
    }

    private fun loadMesasForCentro(centroId: Int) {
        viewModelScope.launch {
            val result = mesaRepository.getMesas(centroVotacionId = centroId)
            result.fold(
                onSuccess = { mesas ->
                    val currentMap = _uiState.value.mesasPorCentro.toMutableMap()
                    currentMap[centroId] = mesas
                    _uiState.value = _uiState.value.copy(mesasPorCentro = currentMap)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Error al cargar mesas"
                    )
                }
            )
        }
    }

    fun openAsignarMesa(mesa: Mesa, centro: CentroVotacion) {
        _uiState.value = _uiState.value.copy(
            showAsignarModal = true,
            mesaParaAsignar = mesa,
            centroParaAsignar = centro,
            personeroSeleccionado = null,
            observacionesAsignacion = "",
            asignarError = null,
            asignarSuccess = false
        )
        loadMesasDisponibles(centro.id_centro)
    }

    private fun loadPersonerosDisponibles(provincia: String, distrito: String, centroId: Int?) {
        viewModelScope.launch {
            try {
                val response = com.electoral.personeros.data.api.RetrofitInstance.api.getPersonerosCentroDisponibles()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = _uiState.value.copy(personerosDisponibles = response.body()!!)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(personerosDisponibles = emptyList())
            }
        }
    }

    private fun loadMesasDisponibles(centroId: Int?) {
        viewModelScope.launch {
            val state = _uiState.value
            val prov = state.centroParaAsignar?.provincia
            val dist = state.centroParaAsignar?.distrito
            val result = mesaRepository.getMesasDisponibles(
                centroVotacionId = centroId,
                provincia = prov,
                distrito = dist
            )
            result.fold(
                onSuccess = { mesas ->
                    _uiState.value = _uiState.value.copy(mesasDisponibles = mesas)
                    state.centroParaAsignar?.let { centro ->
                        loadPersonerosDisponibles(centro.provincia, centro.distrito, centroId)
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Error al cargar mesas disponibles"
                    )
                }
            )
        }
    }

    fun selectPersonero(personero: PersoneroDisponible) {
        _uiState.value = _uiState.value.copy(personeroSeleccionado = personero, asignarError = null)
    }

    fun updateObservaciones(text: String) {
        _uiState.value = _uiState.value.copy(observacionesAsignacion = text)
    }

    fun closeAsignarModal() {
        _uiState.value = _uiState.value.copy(showAsignarModal = false)
    }

    fun confirmarAsignacion() {
        val state = _uiState.value
        val mesa = state.mesaParaAsignar
        val personero = state.personeroSeleccionado
        if (mesa == null || personero == null) {
            _uiState.value = state.copy(asignarError = "Debe seleccionar un personero")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isAsignando = true, asignarError = null)
            val result = mesaRepository.asignarMesa(
                mesaId = mesa.id_mesa,
                personeroId = personero.id_solicitud,
                centroVotacionId = state.centroParaAsignar?.id_centro,
                observaciones = state.observacionesAsignacion.takeIf { it.isNotBlank() },
                idUsuarioReal = personero.idUsuarioSeguro
            )
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isAsignando = false,
                        asignarSuccess = true,
                        showAsignarModal = false
                    )
                    state.centroParaAsignar?.id_centro?.let { loadMesasForCentro(it) }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isAsignando = false,
                        asignarError = error.message ?: "Error al asignar"
                    )
                }
            )
        }
    }

    fun desasignarMesa(mesaId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDesasignando = mesaId)
            val result = mesaRepository.desasignarMesa(mesaId)
            result.fold(
                onSuccess = {
                    val cid = _uiState.value.centroExpandido
                    if (cid != null) loadMesasForCentro(cid)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Error al desasignar"
                    )
                }
            )
            _uiState.value = _uiState.value.copy(isDesasignando = null)
        }
    }

    fun clearAsignarSuccess() {
        _uiState.value = _uiState.value.copy(asignarSuccess = false)
    }

    fun refreshCentro(centroId: Int) {
        loadMesasForCentro(centroId)
    }

    fun updateDniLookup(text: String) {
        _uiState.value = _uiState.value.copy(
            dniLookup = text,
            dniLookupResult = if (text.isBlank()) null else _uiState.value.dniLookupResult,
            registrarError = null
        )
    }

    fun buscarPersonaPorDni() {
        val dni = _uiState.value.dniLookup.trim()
        if (dni.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(dniLookupLoading = true, dniLookupResult = null, registrarError = null)
            val result = mesaRepository.buscarPersonaPorDni(dni)
            result.fold(
                onSuccess = { resp ->
                    _uiState.value = _uiState.value.copy(dniLookupLoading = false, dniLookupResult = resp)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        dniLookupLoading = false,
                        dniLookupResult = com.electoral.personeros.data.models.PersonaDNILookupResponse(encontrado = false),
                        registrarError = err.message
                    )
                }
            )
        }
    }

    fun abrirRegistrarNuevo() {
        _uiState.value = _uiState.value.copy(showRegistrarNuevo = true, registrarError = null)
    }

    fun cerrarRegistrarNuevo() {
        _uiState.value = _uiState.value.copy(showRegistrarNuevo = false, registrarError = null)
    }

    fun confirmarRegistrarYAsignar(
        dni: String,
        nombre: String,
        apellidos: String,
        telefono1: String?,
        whatsapp: String?,
        correo: String?,
        provincia: String?,
        distrito: String?,
        direccion: String?
    ) {
        val state = _uiState.value
        val mesa = state.mesaParaAsignar
        val centro = state.centroParaAsignar
        if (mesa == null || centro == null) return
        viewModelScope.launch {
            _uiState.value = state.copy(isRegistrandoYAsignando = true, registrarError = null)
            val req = com.electoral.personeros.data.models.RegistrarYAsignarMesaRequest(
                dni = dni.trim(),
                nombre = nombre.trim(),
                apellidos = apellidos.trim(),
                telefono1 = telefono1?.takeIf { it.isNotBlank() },
                telefono2 = whatsapp?.takeIf { it.isNotBlank() },
                correo = correo?.takeIf { it.isNotBlank() },
                provincia = provincia ?: centro.provincia,
                distrito = distrito ?: centro.distrito,
                direccion = direccion?.takeIf { it.isNotBlank() },
                id_centro = centro.id_centro,
                id_mesa = mesa.id_mesa,
                observaciones = state.observacionesAsignacion.takeIf { it.isNotBlank() }
            )
            val result = mesaRepository.registrarYAsignarMesaDirecto(req)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isRegistrandoYAsignando = false,
                        showRegistrarNuevo = false,
                        showAsignarModal = false,
                        asignarSuccess = true
                    )
                    loadMesasForCentro(centro.id_centro)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isRegistrandoYAsignando = false,
                        registrarError = err.message ?: "Error al registrar personero"
                    )
                }
            )
        }
    }
}
