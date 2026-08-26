package com.electoral.personeros.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.electoral.personeros.data.models.CentroVotacion
import com.electoral.personeros.data.models.Usuario
import com.electoral.personeros.data.repository.CentroVotacionRepository
import com.electoral.personeros.data.repository.UsuarioRepository
import com.electoral.personeros.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SuperAdminUiState(
    val usuarios: List<Usuario> = emptyList(),
    val filteredUsuarios: List<Usuario> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedRolFilter: String? = null,
    val showForm: Boolean = false,
    val isEditing: Boolean = false,
    val usuarioSeleccionado: Usuario? = null,
    val formData: Usuario = Usuario(rol = Constants.ROLE_ADMINISTRADOR),
    val formError: String? = null,
    val formSuccess: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Int? = null,
    val showBuscarCentro: Boolean = false,
    val centroSearchQuery: String = "",
    val centroSearchResults: List<CentroVotacion> = emptyList(),
    val centroSearchLoading: Boolean = false,
    val centroSearchError: String? = null,
    val centroSeleccionado: CentroVotacion? = null
)

class SuperAdminVM(application: Application) : AndroidViewModel(application) {

    private val usuarioRepository = UsuarioRepository()
    private val centroRepository = CentroVotacionRepository()

    private val _uiState = MutableStateFlow(SuperAdminUiState())
    val uiState: StateFlow<SuperAdminUiState> = _uiState.asStateFlow()

    init {
        // NO cargar usuarios automaticamente aqui: RACE CONDITION con token
        // Se carga desde SuperAdminScreen LaunchedEffect despues de login
    }

    fun inicializar() {
        if (_uiState.value.usuarios.isEmpty() && !_uiState.value.isLoading) {
            loadUsuarios()
        }
    }

    fun loadUsuarios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = usuarioRepository.getAllUsuarios()
            result.fold(
                onSuccess = { usuarios ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        usuarios = usuarios,
                        filteredUsuarios = applyFilters(usuarios, _uiState.value.searchQuery, _uiState.value.selectedRolFilter)
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al cargar usuarios"
                    )
                }
            )
        }
    }

    private fun applyFilters(usuarios: List<Usuario>, query: String, rol: String?): List<Usuario> {
        return usuarios.filter { usuario ->
            val matchesQuery = query.isBlank() ||
                usuario.usuario.contains(query, ignoreCase = true) ||
                usuario.nombreCompleto.contains(query, ignoreCase = true) ||
                usuario.dniDisplay.contains(query, ignoreCase = true)
            val matchesRol = rol.isNullOrBlank() || usuario.rol.equals(rol, ignoreCase = true) ||
                (rol == Constants.ROLE_PERSONERO_CENTRO &&
                    (usuario.rol.equals(Constants.ROLE_PERSONERO_CENTRO, ignoreCase = true) ||
                     usuario.rol.equals(Constants.ROLE_PERSONERO_MESA, ignoreCase = true)))
            matchesQuery && matchesRol
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredUsuarios = applyFilters(_uiState.value.usuarios, query, _uiState.value.selectedRolFilter)
        )
    }

    fun updateRolFilter(rol: String?) {
        _uiState.value = _uiState.value.copy(
            selectedRolFilter = rol,
            filteredUsuarios = applyFilters(_uiState.value.usuarios, _uiState.value.searchQuery, rol)
        )
    }

    fun toggleForm(show: Boolean, usuario: Usuario? = null) {
        val formBase = if (usuario == null) {
            Usuario(rol = Constants.ROLE_ADMINISTRADOR)
        } else {
            val p = usuario.persona
            val dni = usuario.dni.ifBlank { p?.dni ?: "" }
            val nombre = usuario.nombre.ifBlank { p?.nombre ?: "" }
            val apellidos = usuario.apellidos.ifBlank { p?.apellidos ?: "" }
            val telefono1 = if (usuario.telefono1.isNullOrBlank()) p?.telefono1 else usuario.telefono1
            val telefono2 = if (usuario.telefono2.isNullOrBlank()) p?.telefono2 else usuario.telefono2
            val direccion = if (usuario.direccion.isNullOrBlank()) p?.direccion else usuario.direccion
            val correo = if (usuario.correo.isNullOrBlank()) p?.correo else usuario.correo
            val provinciaRoot = if (usuario.provincia.isNullOrBlank()) p?.provincia else usuario.provincia
            val distritoRoot = if (usuario.distrito.isNullOrBlank()) p?.distrito else usuario.distrito
            val provAsignada = usuario.provinciaAsignada ?: provinciaRoot
            val distAsignado = usuario.distritoAsignado ?: distritoRoot
            usuario.copy(
                password = null,
                dni = dni,
                nombre = nombre,
                apellidos = apellidos,
                telefono1 = telefono1,
                telefono2 = telefono2,
                direccion = direccion,
                correo = correo,
                provincia = provinciaRoot,
                distrito = distritoRoot,
                provinciaAsignada = provAsignada,
                distritoAsignado = distAsignado
            )
        }
        _uiState.value = _uiState.value.copy(
            showForm = show,
            isEditing = usuario != null,
            usuarioSeleccionado = usuario,
            formData = formBase,
            formError = null,
            formSuccess = false,
            centroSeleccionado = null,
            showBuscarCentro = false,
            centroSearchResults = emptyList()
        )
    }

    fun updateFormData(updated: Usuario) {
        _uiState.value = _uiState.value.copy(formData = updated, formError = null)
    }

    fun abrirBuscarCentro() {
        _uiState.value = _uiState.value.copy(
            showBuscarCentro = true,
            centroSearchQuery = "",
            centroSearchResults = emptyList(),
            centroSearchError = null
        )
    }

    fun cerrarBuscarCentro() {
        _uiState.value = _uiState.value.copy(
            showBuscarCentro = false,
            centroSearchResults = emptyList(),
            centroSearchError = null
        )
    }

    fun actualizarCentroSearchQuery(q: String) {
        _uiState.value = _uiState.value.copy(centroSearchQuery = q)
    }

    fun ejecutarBuscarCentros() {
        val q = _uiState.value.centroSearchQuery.trim()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(centroSearchLoading = true, centroSearchError = null)
            val result = centroRepository.buscarCentros(
                q = q.ifBlank { " " },
                provincia = null,
                distrito = null
            )
            result.fold(
                onSuccess = { list ->
                    _uiState.value = _uiState.value.copy(
                        centroSearchLoading = false,
                        centroSearchResults = list
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        centroSearchLoading = false,
                        centroSearchError = e.message ?: "Error al buscar centros"
                    )
                }
            )
        }
    }

    fun seleccionarCentro(c: CentroVotacion) {
        val currentForm = _uiState.value.formData
        _uiState.value = _uiState.value.copy(
            centroSeleccionado = c,
            showBuscarCentro = false,
            formData = currentForm.copy(
                provincia = c.provincia,
                distrito = c.distrito,
                provinciaAsignada = c.provincia,
                distritoAsignado = c.distrito,
                id_centro_asignado = c.id_centro
            ),
            centroSearchResults = emptyList()
        )
    }

    fun saveUsuario() {
        val state = _uiState.value
        val data = state.formData
        if (data.usuario.isBlank()) {
            _uiState.value = state.copy(formError = "El nombre de usuario es requerido")
            return
        }
        if (!state.isEditing && (data.password == null || data.password!!.isBlank())) {
            _uiState.value = state.copy(formError = "La contraseña es requerida para usuarios nuevos")
            return
        }
        if (data.rol.equals(Constants.ROLE_PERSONERO_CENTRO, ignoreCase = true) &&
            (state.centroSeleccionado == null && data.id_centro_asignado == null)
        ) {
            _uiState.value = state.copy(formError = "Debe seleccionar un Centro de Votación para el rol Personero de Centro")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, formError = null)
            val result = if (state.isEditing && data.idusuario > 0) {
                usuarioRepository.updateUsuario(data.idusuario, data)
            } else {
                usuarioRepository.createUsuario(data)
            }
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        formSuccess = true,
                        showForm = false
                    )
                    loadUsuarios()
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        formError = error.message ?: "Error al guardar"
                    )
                }
            )
        }
    }

    fun deleteUsuario(usuarioId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = usuarioId)
            val result = usuarioRepository.deleteUsuario(usuarioId)
            result.fold(
                onSuccess = { loadUsuarios() },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message ?: "Error al eliminar"
                    )
                }
            )
            _uiState.value = _uiState.value.copy(isDeleting = null)
        }
    }

    fun clearFormSuccess() {
        _uiState.value = _uiState.value.copy(formSuccess = false)
    }
}
