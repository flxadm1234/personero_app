package com.electoral.personeros.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.electoral.personeros.data.api.RetrofitInstance
import com.electoral.personeros.data.models.LoginResponse
import com.electoral.personeros.data.models.SolicitudPersonero
import com.electoral.personeros.data.models.TokenData
import com.electoral.personeros.data.models.Usuario
import com.electoral.personeros.data.repository.AuthRepository
import com.electoral.personeros.data.repository.SolicitudRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false,
    val currentUser: Usuario? = null,
    val tokenData: TokenData? = null,
    val showRegistroModal: Boolean = false,
    val solicitudLoading: Boolean = false,
    val solicitudSuccess: Boolean = false,
    val solicitudError: String? = null
)

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application)
    private val solicitudRepository = SolicitudRepository()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.tokenData.collect { tokenData ->
                _uiState.value = _uiState.value.copy(
                    tokenData = tokenData
                )
                tokenData.accessToken?.let {
                    RetrofitInstance.setAuthToken(it)
                    authRepository.restoreSession(it)
                }
            }
        }
    }

    fun updateUsername(value: String) {
        _uiState.value = _uiState.value.copy(username = value, errorMessage = null)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun login() {
        val state = _uiState.value
        if (state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Ingrese usuario y contraseña")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            val result = authRepository.login(state.username.trim(), state.password)
            result.fold(
                onSuccess = { response: LoginResponse ->
                    if (response.success) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            loginSuccess = true,
                            currentUser = response.usuario,
                            errorMessage = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = response.message ?: "Credenciales inválidas"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error de conexión"
                    )
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = LoginUiState()
        }
    }

    fun showRegistroModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showRegistroModal = show,
            solicitudError = null,
            solicitudSuccess = false
        )
    }

    fun submitSolicitud(solicitud: SolicitudPersonero) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(solicitudLoading = true, solicitudError = null)
            val result = solicitudRepository.createSolicitud(solicitud)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        solicitudLoading = false,
                        solicitudSuccess = true,
                        showRegistroModal = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        solicitudLoading = false,
                        solicitudError = error.message ?: "Error al enviar solicitud"
                    )
                }
            )
        }
    }

    fun clearSolicitudSuccess() {
        _uiState.value = _uiState.value.copy(solicitudSuccess = false)
    }
}
