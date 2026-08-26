package com.electoral.personeros.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.electoral.personeros.data.models.CoberturaResponse
import com.electoral.personeros.data.models.DesgloseItem
import com.electoral.personeros.data.models.SolicitudPersonero
import com.electoral.personeros.data.repository.CoberturaRepository
import com.electoral.personeros.data.repository.SolicitudRepository
import com.electoral.personeros.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val provinciaAsignada: String? = null,
    val distritoAsignado: String? = null,
    val coberturaGeneral: CoberturaResponse? = null,
    val coberturaProvincia: CoberturaResponse? = null,
    val coberturaDistrito: CoberturaResponse? = null,
    val desgloseList: List<DesgloseItem> = emptyList(),
    val nivelActual: String = "GENERAL",
    val provinciaSeleccionada: String? = null,
    val distritoSeleccionado: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class SolicitudesUiState(
    val solicitudes: List<SolicitudPersonero> = emptyList(),
    val filteredSolicitudes: List<SolicitudPersonero> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedEstadoFilter: String? = null,
    val selectedProvincia: String? = null,
    val selectedDistrito: String? = null,
    val isProcessing: Int? = null,
    val procesarError: String? = null,
    val procesarSuccess: Boolean = false
)

class AdminVM(application: Application) : AndroidViewModel(application) {

    private val coberturaRepository = CoberturaRepository()
    private val solicitudRepository = SolicitudRepository()

    private val _dashboardState = MutableStateFlow(AdminDashboardUiState())
    val dashboardState: StateFlow<AdminDashboardUiState> = _dashboardState.asStateFlow()

    private val _solicitudesState = MutableStateFlow(SolicitudesUiState())
    val solicitudesState: StateFlow<SolicitudesUiState> = _solicitudesState.asStateFlow()

    fun setProvinciaAsignada(provincia: String?) {
        _dashboardState.value = _dashboardState.value.copy(provinciaAsignada = provincia)
    }

    fun setDistritoAsignado(distrito: String?) {
        _dashboardState.value = _dashboardState.value.copy(distritoAsignado = distrito)
    }

    fun loadCoberturaGeneral() {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true, errorMessage = null)
            val result = coberturaRepository.getCoberturaGeneral()
            result.fold(
                onSuccess = { cobertura ->
                    _dashboardState.value = _dashboardState.value.copy(
                        isLoading = false,
                        coberturaGeneral = cobertura,
                        nivelActual = "GENERAL"
                    )
                    cargarDesglose()
                },
                onFailure = { error ->
                    _dashboardState.value = _dashboardState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar cobertura"
                    )
                }
            )
        }
    }

    private fun cargarDesglose() {
        viewModelScope.launch {
            val result = coberturaRepository.getDesglose()
            result.fold(
                onSuccess = { lista ->
                    _dashboardState.value = _dashboardState.value.copy(desgloseList = lista)
                },
                onFailure = { }
            )
        }
    }

    fun drillDownProvincia(provincia: String) {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true, errorMessage = null)
            val result = coberturaRepository.getCoberturaProvincia(provincia)
            result.fold(
                onSuccess = { cobertura ->
                    _dashboardState.value = _dashboardState.value.copy(
                        isLoading = false,
                        coberturaProvincia = cobertura,
                        provinciaSeleccionada = provincia,
                        nivelActual = "PROVINCIA"
                    )
                },
                onFailure = { error ->
                    _dashboardState.value = _dashboardState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error"
                    )
                }
            )
        }
    }

    fun drillDownDistrito(provincia: String, distrito: String) {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true, errorMessage = null)
            val result = coberturaRepository.getCoberturaDistrito(provincia, distrito)
            result.fold(
                onSuccess = { cobertura ->
                    _dashboardState.value = _dashboardState.value.copy(
                        isLoading = false,
                        coberturaDistrito = cobertura,
                        provinciaSeleccionada = provincia,
                        distritoSeleccionado = distrito,
                        nivelActual = "DISTRITO"
                    )
                },
                onFailure = { error ->
                    _dashboardState.value = _dashboardState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error"
                    )
                }
            )
        }
    }

    fun goBack() {
        val state = _dashboardState.value
        when (state.nivelActual) {
            "DISTRITO" -> {
                _dashboardState.value = state.copy(
                    nivelActual = "PROVINCIA",
                    distritoSeleccionado = null,
                    coberturaDistrito = null
                )
            }
            "PROVINCIA" -> {
                _dashboardState.value = state.copy(
                    nivelActual = "GENERAL",
                    provinciaSeleccionada = null,
                    coberturaProvincia = null
                )
            }
        }
    }

    fun loadSolicitudes() {
        val state = _solicitudesState.value
        viewModelScope.launch {
            _solicitudesState.value = state.copy(isLoading = true, errorMessage = null)
            val result = solicitudRepository.getSolicitudes(
                estado = state.selectedEstadoFilter,
                provincia = state.selectedProvincia,
                distrito = state.selectedDistrito,
                search = state.searchQuery.takeIf { it.isNotBlank() }
            )
            result.fold(
                onSuccess = { solicitudes ->
                    _solicitudesState.value = _solicitudesState.value.copy(
                        isLoading = false,
                        solicitudes = solicitudes,
                        filteredSolicitudes = applySolicitudFilters(solicitudes, state.searchQuery)
                    )
                },
                onFailure = { error ->
                    _solicitudesState.value = _solicitudesState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar solicitudes"
                    )
                }
            )
        }
    }

    private fun applySolicitudFilters(solicitudes: List<SolicitudPersonero>, query: String): List<SolicitudPersonero> {
        return if (query.isBlank()) {
            solicitudes
        } else {
            solicitudes.filter {
                (it.persona?.nombreCompleto?.contains(query, ignoreCase = true) == true) ||
                    (it.persona?.dni?.contains(query, ignoreCase = true) == true)
            }
        }
    }

    fun updateSolicitudesSearch(query: String) {
        _solicitudesState.value = _solicitudesState.value.copy(
            searchQuery = query,
            filteredSolicitudes = applySolicitudFilters(_solicitudesState.value.solicitudes, query)
        )
    }

    fun updateSolicitudesFiltroEstado(estado: String?) {
        _solicitudesState.value = _solicitudesState.value.copy(selectedEstadoFilter = estado)
        loadSolicitudes()
    }

    fun aprobarSolicitud(id: Int, comentarios: String? = null) {
        procesarEstado(id, Constants.ESTADO_ASIGNADO, comentarios)
    }

    fun rechazarSolicitud(id: Int, comentarios: String? = null) {
        procesarEstado(id, Constants.ESTADO_RECHAZADO, comentarios)
    }

    private fun procesarEstado(id: Int, estado: String, comentarios: String? = null) {
        viewModelScope.launch {
            _solicitudesState.value = _solicitudesState.value.copy(isProcessing = id, procesarError = null)
            val result = solicitudRepository.actualizarEstado(id, estado, comentarios)
            result.fold(
                onSuccess = {
                    _solicitudesState.value = _solicitudesState.value.copy(
                        isProcessing = null,
                        procesarSuccess = true
                    )
                    loadSolicitudes()
                },
                onFailure = { error ->
                    _solicitudesState.value = _solicitudesState.value.copy(
                        isProcessing = null,
                        procesarError = error.message ?: "Error al procesar"
                    )
                }
            )
        }
    }

    fun clearProcesarSuccess() {
        _solicitudesState.value = _solicitudesState.value.copy(procesarSuccess = false)
    }

    fun getCurrentCobertura(): CoberturaResponse? {
        val state = _dashboardState.value
        return when (state.nivelActual) {
            "DISTRITO" -> state.coberturaDistrito
            "PROVINCIA" -> state.coberturaProvincia
            else -> state.coberturaGeneral
        }
    }

    fun getDesgloseList(): List<DesgloseItem> {
        return _dashboardState.value.desgloseList
    }
}
